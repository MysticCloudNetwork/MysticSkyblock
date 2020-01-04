package net.mysticcloud.spigot.skyblock.utils.islands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.mysticcloud.spigot.core.utils.CoreUtils;
import net.mysticcloud.spigot.core.utils.InventoryCreator;
import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.Coord;
import net.mysticcloud.spigot.skyblock.utils.SkyblockPlayer;
import net.mysticcloud.spigot.skyblock.utils.Utils;

public class IslandManager {

	static Map<String, Island> islands = new HashMap<>();
	public static List<IslandType> types = new ArrayList<>();
	public static char[] alphebet = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y', 'Z', 'a', 'b' };

	private static int HEIGHT = 30;
	public static int PLOT_SIZE = 15;
	
	public static Set<String> getIslands(){
		return islands.keySet();
	}

	public static void registerIslands() {

		if (Main.getPlugin().getConfig().isSet("PLOT_SIZE"))
			PLOT_SIZE = Main.getPlugin().getConfig().getInt("PLOT_SIZE");
		if (Main.getPlugin().getConfig().isSet("HEIGHT"))
			HEIGHT = Main.getPlugin().getConfig().getInt("HEIGHT");

		try {

			for (File file : getAllTypes()) {
				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
				IslandType t = new IslandType(fc.getString("Schematic"));
				t.setName(fc.getString("Name"));
				ItemStack is = new ItemStack(Material.valueOf(fc.getString("GUI_Item")));
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(CoreUtils.colorize(fc.getString("Name")));
				if(fc.isSet("Description")) {
					im.setLore(CoreUtils.colorizeStringList(fc.getStringList("Description")));
				}
				is.setItemMeta(im);
				t.setItemStack(is);

				types.add(t);

				Bukkit.getConsoleSender().sendMessage(
						CoreUtils.colorize("&e&lSkyblock &f>&7 Registered Island Type &f" + t.getName() + "&7."));

			}
		} catch (NullPointerException e) {
			createDemoType();
		}

		try {
			for (File file : getAllIslands()) {

				FileConfiguration fc = YamlConfiguration.loadConfiguration(file);

				int id = Integer.parseInt(file.getName().replace(".yml", ""));

				Coord pos = getIslandLocation(id);
				String sc = fc.getString("Type");
				IslandType type = null;
				for (IslandType t : types) {
					if (t.getFileName().equals(sc)) {
						type = t;
						break;
					}
				}
				if (type == null)
					continue;
				Island island;
				if (!fc.getString("Owner").equals("null")) {
					island = new Island(id, pos.getX() * PLOT_SIZE, pos.getY() * PLOT_SIZE, type,
							UUID.fromString(fc.getString("Owner")));
					Bukkit.broadcastMessage("Registering player...");
					Utils.getSkyblockPlayer(UUID.fromString(fc.getString("Owner"))).addIsland(id);
					Bukkit.getConsoleSender().sendMessage(CoreUtils.colorize("Registered to player: " + id));
				} else {
					island = new Island(id, pos.getX() * PLOT_SIZE, pos.getY() * PLOT_SIZE, type, (Player) null);
				}
				island.setSpawnLocation(fc.getString("Spawn"));
				
				if(fc.isSet("Players")) {
					for(String uid : fc.getStringList("Players")) {
						for(String item : fc.getStringList("Inventories." + uid)) {
							island.getInventories().get(UUID.fromString(uid)).add(CoreUtils.decryptItemStack(item));
						}
					}
				}

				islands.put(id + "", island);

				Bukkit.getConsoleSender()
						.sendMessage(CoreUtils.colorize("&e&lSkyblock &f>&7 Registered Island &f" + island.getID() + "&7."));

			}
		} catch (NullPointerException ex) {
			new File(Main.getPlugin().getDataFolder() + "/islands/").mkdir();
			Bukkit.getConsoleSender()
			.sendMessage(CoreUtils.colorize("&e&lSkyblock &f>&7 Creating island directitory."));
		}

	}
	


	public static void destroyIsland(Island is) {
		is.deactivate();
		saveIsland(is);
	}

	public static void destroyIsland(int is) {
		destroyIsland(islands.get("" + is));
	}

	public static void saveIsland(Island is) {

		File file = new File(Main.getPlugin().getDataFolder() + "/islands/" + is.getID() + ".yml");
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.set("Type", is.getType().schemfile + "");
		fc.set("Owner", is.getOwner() + "");
		if (is.getSpawnLocation() == null) {
			fc.set("Spawn", "Na");
		} else
			fc.set("Spawn", is.getSpawnLocation().getX() + ":" + is.getSpawnLocation().getY() + ":" + is.getSpawnLocation().getZ());
		
		List<String> uids = new ArrayList<>();
		for(Entry<UUID,List<ItemStack>> e : is.getInventories().entrySet()) {
			uids.add(e.getKey()+"");
			List<String> items = new ArrayList<>();
			for(ItemStack i : e.getValue()) {
				items.add(CoreUtils.encryptItemStack(i));
			}
			fc.set("Players", uids);
			fc.set("Inventories." + e.getKey(), items);
		}
		try {
			fc.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static File[] getAllTypes() {
		return new File(Main.getPlugin().getDataFolder() + "/islandtypes").listFiles();
	}

	private static File[] getAllIslands() {
		return new File(Main.getPlugin().getDataFolder() + "/islands").listFiles();
	}

	public static void createFiles() {
		new File(Main.getPlugin().getDataFolder() + "/islandtypes").mkdir();
	}

	public static void createDemoType() {
		createFiles();
		File demo = new File(Main.getPlugin().getDataFolder() + "/islandtypes/classic.yml");

		try {
			demo.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(demo);
		fc.set("Name", "Classic");
		fc.set("Description", "&fDefault descrition.");
		fc.set("Schematic", "file.schematic");
		fc.set("GUI_Item", "GRASS_BLOCK");

		try {
			fc.save(demo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Bukkit.getConsoleSender().sendMessage(CoreUtils.colorize("&e&lSkyblock &f>&7 Created Demo Island Type."));

		registerIslands();

	}

	public static Island nextIsland(Player player, IslandType type) {
		if (islands.size() != 0) {
			for (Island island : islands.values()) {
				if (!island.isActive()) {
					island.reActivate(player, type);
					return island;
				}
			}
		}

		Coord loc = getIslandLocation(islands.size() + 1);

		Island island = new Island(islands.size() + 1, (int) loc.getX() * PLOT_SIZE, (int) loc.getY() * PLOT_SIZE, type,
				player);
		island.build();
		SkyblockPlayer spl = Utils.getSkyblockPlayer(player.getUniqueId());

		Utils.saveSkyblockPlayer(spl);
		islands.put((islands.size() + 1) + "", island);
		return island;

	}

	public static Inventory getIslandTypeSelectorGUI(Player player) {

		InventoryCreator inv = new InventoryCreator("Islands", (player), ((types.size() / 9) + 1) * 9);
		inv.addItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), "&eComing Soon", 'X', (String[]) null);
		ArrayList<Character> c = new ArrayList<Character>();
		for (int i = 0; i != (((int) (types.size() / 9)) + 1) * 9; i++) {
			if (i < types.size()) {
				if (player.hasPermission("mysticcloud.island.type." + ChatColor.stripColor(types.get(i).getName()))) {
					inv.addItem(new ItemStack(types.get(i).getGUIItem()), types.get(i).getName(), (char) i,
							(String[]) null, types.get(i).getGUIItem().getDurability(), false);
				} else {
					inv.addItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), types.get(i).getName(), (char) i,
							new String[] { "&cLocked..." }, (short) 14, false);
				}
				c.add((char) i);
			} else {
				c.add('X');
			}

		}
		inv.setConfiguration(c);
		c.clear();
		c = null;
		return inv.getInventory();

	}

	public static Inventory getIslandMenuGUI(Player player, int i) {

		SkyblockPlayer pl = Utils.getSkyblockPlayer(player.getUniqueId());

		InventoryCreator inv = new InventoryCreator("&e&lSkyblock Menu", player, 27);
		inv.addItem(new ItemStack(Material.RED_BED), CoreUtils.colorize("&a&lHome"), 'A',
				new String[] { "&7Click to teleport to your home." });
		inv.addItem(new ItemStack(Material.GRASS_BLOCK), CoreUtils.colorize("&e&lRegen Island"), 'B',
				new String[] { "&7Click to regenerate.", "&7your island." });

		inv.addItem(new ItemStack(Material.BARRIER), "&4Delete Island", 'C',
				new String[] { "&7Click to delete this island" });

		if (pl.getMaxIslands() > 1 && pl.getMaxIslands() > pl.getIslands().size()) {
			inv.addItem(new ItemStack(Material.WHEAT_SEEDS), CoreUtils.colorize("&b&lCreate New Island"), 'D',
					new String[] { "&7Click to create another island." });

		} else {
			inv.addItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), CoreUtils.colorize("&4&lCreate New Island"),
					'D', new String[] { "Sorry, you currently can", "&7not access this option." });

		}
		inv.addItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), "&7Click an option", 'X', (String[]) null);

		inv.setConfiguration(new char[] { 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'A', 'X', 'B', 'X', 'C',
				'X', 'D', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' });

		return inv.getInventory();

	}

	public static Inventory getIslandSelectorGUI(Player player, List<String> islands2) {

		SkyblockPlayer pl = Utils.getSkyblockPlayer(player.getUniqueId());

		InventoryCreator inv = new InventoryCreator("&e&lSkyblock Menu", player, 27);
		inv.addItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), "&7Click an option", 'X', (String[]) null);

		ArrayList<Character> chars = new ArrayList<>();
		for (int s = 0; s != 11; s++) {
			chars.add('X');
		}

		int a = 0;
		for (String i : islands2) {
			// TODO
			inv.addItem(new ItemStack(islands.get(i).getType().getGUIItem().getType()), i + "", alphebet[a],
					new String[] { "&7Click to manage this island." });
			chars.add(alphebet[a]);
			a = a + 1;
		}
		if (a < 5) {
			inv.addItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), "&cLocked...", alphebet[a], (String[]) null);
			for (int f = a; f != 5; f++) {

				chars.add(alphebet[a]);
			}
		}

		for (int s = 0; s != 11; s++) {
			chars.add('X');
		}

		inv.setConfiguration(chars);

		return inv.getInventory();

	}

	public static Coord getIslandLocation(int i) {
		if (i > 0) {

			/**
			 * Remember that even though we accounted for the first plot (0,0) we didnt
			 * account for the changes yet... so some of the values are preset to initialize
			 * the change so future changes can run properly.
			 */

			boolean h = false; // to tell if we're going horizontal or verticle
								// (important determining to add or subtract
								// values from x OR y)
			boolean pos = true; // tells if we should add or subtract variables
								// to x or y

			int changes = 1; // indicates changes in direction so we can
								// determine if we are going positive or negative
								// (starts at 1 initially because we account for 0
								// -> 1 as a change)

			int x = 0; // starting off at 0,0
			int y = 0; // ^

			int max = 0; // represents how far we go excluding 0. (starting at
							// 1)
			int mc = 0; // our counter for until we reach max for direction
			int c = 0; // our counter until we reach i

			while (true) {

				// increase our counters
				c++;
				mc++;

				/**
				 * Configuring direction determining values
				 */
				if (mc >= max) { // if we reach our max range in a direction, we
									// need to reconfigure our determining values.
					mc = 0; // reset our max counter

					h = !h; // change our direction

					changes++; // indicate we've made a change in direction. (to
								// determine later if positive or negative)

					if (changes == 2) { // if we have changed direction twice,
										// we are now going opposite of positive
										// boolean.
						changes = 0;
						pos = !pos;
					}

					if (h) { // update our max range, once we go horizontal
						max++;
					}
				}

				// once we have configured our values if needed, then we must
				// iterate our x OR y values based off the direction determining
				// values.

				/**
				 * Updating values based off of direction determining values
				 */
				if (h) { // if we are going horizontal, we must change the X

					x = pos ? x + 1 : x - 1; // if going positive, increase
												// directional value. if going
												// negative, decrease directional
												// value

				} else { // if not going horizontal, change Y

					y = pos ? y + 1 : y - 1; // if going positive, increase
												// directional value. if going
												// negative, decrease directional
												// value

				}

				/**
				 * Once all our changes are made and determined, we can return the original
				 * value.
				 */
				if (c == i)
					return new Coord(x, y);
			}
		}
		return new Coord(0, 0);
	}

	public static int getHeight() {
		return HEIGHT;
	}

	public static Island getIsland(int i) {
		return islands.get(i + "");
	}

}
