package net.mysticcloud.spigot.skyblock.utils.islands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Snowable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.mysticcloud.spigot.core.utils.CoreUtils;
import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.Schematic;
import net.mysticcloud.spigot.skyblock.utils.SkyBlockData;
import net.mysticcloud.spigot.skyblock.utils.SkyblockPlayer;
import net.mysticcloud.spigot.skyblock.utils.Utils;
import net.mysticcloud.spigot.skyblock.utils.runnables.CheckIslandJoinable;

public class Island {
	private int x = 0;
	private int z = 0;
	private int id = 0;
	private IslandType type;
	private boolean active = false;

	private UUID owner = null;
	private Location lgloc = null;
	private List<ItemStack> startingItems = new ArrayList<>();
	private File file;
	private Location spawnLoc = null;
	private Map<UUID, List<ItemStack>> inventories = new HashMap<>();

	public Island(int id, int x, int z, IslandType type, Player owner) {
		this(id, x, z, type, owner.getUniqueId());
//		this.id = id;
//		this.x = x;
//		this.z = z;
//		this.type = type;
//		// TODO DO NOT DO THIS IN ANY RELEASE
//		lgloc = new Location(Utils.getSkyblockWorld(), x, 30, z);
//		active = true;
//		if (owner != null) {
//			
//			this.owner = owner.getUniqueId();
//			Utils.getSkyblockPlayer(this.owner).addIsland(getID());
//		}
//		else
//			active = false;
//		this.file = new File(Main.getPlugin().getDataFolder() + "/islands/" + id + ".yml");
//		if (!file.exists()) {
//			createFiles();
//			createDemoFile();
//		}
//		registerIsland();
	}

	public Island(int id, int x, int z, IslandType type, UUID owner) {
		this.id = id;
		this.x = x;
		this.z = z;
		this.type = type;
		// TODO DO NOT DO THIS IN ANY RELEASE
		lgloc = new Location(Utils.getSkyblockWorld(), x, 30, z);
		this.owner = owner;
		this.file = new File(Main.getPlugin().getDataFolder() + "/islands/" + id + ".yml");
		if (!file.exists()) {
			createFiles();
			createDemoFile();
		}
		registerIsland();
	}

	public Island leave(Player player) {
		inventories.get(player.getUniqueId()).clear();
		player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f You have left Island " + id));
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null)
				continue;
			if (!item.getType().equals(Material.AIR)) {
				inventories.get(player.getUniqueId()).add(item);
			}
		}
		CoreUtils.debug("Saved inv on leave. id: " + id + " Size: " + inventories.get(player.getUniqueId()).size());
		save();
		return this;
	}

	public void join(Player player) {
		if (!active) {
			player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f You island is being generated..."));
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new CheckIslandJoinable(player.getUniqueId(), this),
					40);
			return;
		}
		if (!inventories.containsKey(player.getUniqueId()))
			inventories.put(player.getUniqueId(), new ArrayList<ItemStack>());
		SkyblockPlayer pl = Utils.getSkyblockPlayer(player.getUniqueId());

		if (!pl.getIsland().equals(id + "")) {

			if (!pl.getIsland().equals(""))
				IslandManager.getIsland(Integer.parseInt(pl.getIsland())).leave(player);
			player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Joining Island " + id));
			player.getInventory().clear();

			if (inventories.get(player.getUniqueId()).size() == 0) {
				givePlayerStartingItems(player);

			} else {
				returnPlayerItems(player);
			}

			pl.setIsland(getID() + "");

		}
		Utils.getWorldBorderAPI().setBorder(player, IslandManager.PLOT_SIZE,
				getLocation_LG().clone().add((IslandManager.PLOT_SIZE / 2), 0, (IslandManager.PLOT_SIZE / 2)));
		player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Teleporting to Island: " + id));
		player.teleport(spawnLoc);

	}

	private void returnPlayerItems(Player player) {
		for (ItemStack i : inventories.get(player.getUniqueId())) {
			if (i == null) {
				continue;
			}
			player.getInventory().addItem(i.clone());
		}

	}

	private void givePlayerStartingItems(Player player) {
		player.sendMessage(CoreUtils.colorize("&aHere, you look like you could use a little help."));
		for (ItemStack i : startingItems) {
			CoreUtils.debug("2-1");
			if (i == null)
				continue;
			player.getInventory().addItem(i.clone());
		}
	}

	public UUID getOwner() {
		return owner;
	}

	public void registerIsland() {
		try {
			FileConfiguration fc = YamlConfiguration.loadConfiguration(this.file);
			if (fc.isSet("Items")) {
				for (String s : fc.getStringList("Items")) {
					if (s.contains("CustomItem")) {

						startingItems.add(CoreUtils.getItem(s.split(":")[1]));
					} else {
						startingItems.add(CoreUtils.decryptItemStack(s));
					}
				}
			}

		} catch (NullPointerException e) {
			createDemoFile();
		}
		IslandManager.saveIsland(this);

	}

	public void createFiles() {
		new File(Main.getPlugin().getDataFolder() + "/islands").mkdir();
	}

	public void createDemoFile() {
		createFiles();
		File demo = new File(Main.getPlugin().getDataFolder() + "/islands/" + id + ".yml");

		try {
			demo.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(demo);
		List<String> sl = new ArrayList<>();
		sl.add("ICE:0:0");
		sl.add("LAVA_BUCKET:0:0");
		sl.add("BUCKET:0:0");
//		sl.add("WHEAT_SEEDS");
		sl.add("CARROT:0:0");
		sl.add("POTATO:0:0");
		sl.add("SUGAR_CANE:0:0");

		fc.set("Items", sl);

		for (String s : sl) {
			if (s.contains("CustomItem")) {

				startingItems.add(CoreUtils.getItem(s.split(":")[1]));
			} else {
				startingItems.add(CoreUtils.decryptItemStack(s));
			}
		}

		try {
			fc.save(demo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public IslandType getType() {
		return type;
	}

	public Location getLocation_LG() {
		return lgloc;
	}

	public boolean isActive() {
		return active;
	}

	public Island regen() {
		destroy().build();
		for (UUID uid : inventories.keySet()) {
			if (Bukkit.getPlayer(uid) == null)
				continue;
			if (Utils.getSkyblockPlayer(uid).getIsland().equals(id + "")) {
				join(Bukkit.getPlayer(uid));
			}
		}
		return this;
	}

	protected Island deactivate() {
		Utils.getSkyblockPlayer(owner).removeIsland(id);
		Utils.saveSkyblockPlayer(owner);
		active = false;

		for (UUID uid : inventories.keySet()) {
			if (Bukkit.getPlayer(uid) == null)
				continue;
			if (Utils.getSkyblockPlayer(uid).getIsland().equals(id + "")) {
				leave(Bukkit.getPlayer(uid));
				Bukkit.getPlayer(uid).teleport(Utils.getSpawnWorld().getSpawnLocation());
			}
		}
		owner = null;

		IslandManager.saveIsland(this);
		destroy();
		return this;
	}

	protected Island reActivate(Player owner, IslandType type) {
		this.type = type;
		this.owner = owner.getUniqueId();
		IslandManager.saveIsland(this);
		Utils.getSkyblockPlayer(owner.getUniqueId()).addIsland(getID());
		build();
		return this;
	}

	public Island destroy() {
		for (Entry<UUID, List<ItemStack>> entry : inventories.entrySet()) {
			entry.getValue().clear();

		}
		Schematic schem = Utils.loadSchematic(type.getFileName());

		for (int x = 0; x < schem.getWidth(); ++x) {
			for (int y = 0; y < schem.getHeight(); ++y) {
				for (int z = 0; z < schem.getLength(); ++z) {
					Block block = new Location(Utils.getSkyblockWorld(), this.x + x, y + IslandManager.getHeight(),
							this.z + z).getBlock();
					block.setType(Material.AIR);
				}
			}
		}
		return this;
	}

	public Island build() {

//		getLocation_LG().getBlock().setType(Material.RED_WOOL);

		Schematic schem = Utils.loadSchematic(type.getFileName());

		for (int x = 0; x < schem.getWidth(); ++x) {
			for (int y = 0; y < schem.getHeight(); ++y) {
				for (int z = 0; z < schem.getLength(); ++z) {
					int index = (y * schem.getLength() + z) * schem.getWidth() + x;
					Block block = new Location(Utils.getSkyblockWorld(), this.x + x, y + IslandManager.getHeight(),
							this.z + z).getBlock();
					try {
						if (((SkyBlockData) (schem.getData().get(schem.getBlocks().toArray()[index]))).getMaterial()
								.equals(Material.AIR))
							continue;
						if (((SkyBlockData) (schem.getData().get(schem.getBlocks().toArray()[index]))).getMaterial()
								.equals(Material.RED_WOOL)) {
							this.spawnLoc = block.getLocation();
							continue;
						}
						block.setType(((SkyBlockData) (schem.getData().get(schem.getBlocks().toArray()[index])))
								.getMaterial());
						block.getState().update();
						setBlockData(schem.getData().get(schem.getBlocks().toArray()[index]).getData(), block);

					} catch (NullPointerException ex) {
						continue;
					}

				}
			}
		}
		active = true;
		save();

		return this;

	}

	private void setBlockData(String data, Block block) {
		if (data.equals(""))
			return;
		if (data.contains(","))
			for (String d : data.split(",")) {
				f(d, block);

			}
		else f(data,block);

	}

	private void f(String data, Block block) {
		String key = data.split("=")[0];
		String value = data.split("=")[1];
		Bukkit.broadcastMessage(block.getType() + "");
		Bukkit.broadcastMessage(data);
		BlockData bd = block.getBlockData();
		switch (key) {
		case "half":
			if(bd instanceof Stairs) {
				((Stairs)bd).setHalf(Half.valueOf(value.toUpperCase()));
			}
			if(bd instanceof Door) {
				if(value.equalsIgnoreCase("Upper")) 
					((Door)bd).setHalf(Half.TOP);
				if(value.equalsIgnoreCase("Lower")) 
					((Door)bd).setHalf(Half.BOTTOM);
			}
			break;
//		case "hinge":
//			((Door) bd).setHinge(Hinge.valueOf(value.toUpperCase()));
//			break;
		case "part":
			((Bed)bd).setPart(Part.valueOf(value.toUpperCase()));
			break;
		case "type":
			if(value.equalsIgnoreCase("single") || value.equalsIgnoreCase("double")) {
				//This means it's a chest, and we don't really give a shit because Minecraft will do all the hard stuff for us
			}
			if(value.equalsIgnoreCase("top") || value.equalsIgnoreCase("upper") || value.equalsIgnoreCase("bottom")) {
				((Slab)bd).setType(Type.valueOf(value.toUpperCase()));
			}
			break;
		
		case "distance":
			((Leaves) bd).setDistance(Integer.parseInt(value));
			
			break;
		case "persistent":
			((Leaves) bd).setPersistent(Boolean.parseBoolean(value));
			block.setBlockData(bd);
			break;
		case "snowy":
			((Snowable) bd).setSnowy(Boolean.parseBoolean(value));
			break;
		case "waterlogged":
			((Waterlogged) bd).setWaterlogged(Boolean.parseBoolean(value));
			break;
		case "facing":
			a(block,block.getType(), BlockFace.valueOf(value.toUpperCase()));
			break;
		case "axis":

			if (value.equalsIgnoreCase("X"))
				a(block,block.getType(), BlockFace.EAST);
			if (value.equalsIgnoreCase("Y"))
				a(block,block.getType(), BlockFace.UP);
			if (value.equalsIgnoreCase("Z"))
				a(block,block.getType(), BlockFace.NORTH);
			break;
		}
		block.setBlockData(bd);
	}
	
	private static void a(Block block, Material material, BlockFace blockFace) {
        block.setType(material);
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Directional) {
            ((Directional) blockData).setFacing(blockFace);
            block.setBlockData(blockData);
        }
        if (blockData instanceof Orientable) {
            ((Orientable) blockData).setAxis(convertBlockFaceToAxis(blockFace));
            block.setBlockData(blockData);
        }
        if (blockData instanceof Rotatable) {
            ((Rotatable) blockData).setRotation(blockFace);
            block.setBlockData(blockData);
        }
    }
	private static Axis convertBlockFaceToAxis(BlockFace face) {
        switch (face) {
            case NORTH:
            case SOUTH:
                return Axis.Z;
            case EAST:
            case WEST:
                return Axis.X;
            case UP:
            case DOWN:
                return Axis.Y;
                default:
                    return Axis.X;
        }
    }

	private void save() {
		IslandManager.saveIsland(this);
	}

	public Location getSpawnLocation() {
		return spawnLoc;
	}

	public int getID() {
		return id;
	}

	public Island destroy_SOFT() {
		Schematic schem = Utils.loadSchematic(type.getFileName());

		for (int x = 0; x < schem.getWidth(); ++x) {
			for (int y = 0; y < schem.getHeight(); ++y) {
				for (int z = 0; z < schem.getLength(); ++z) {
					Block block = new Location(Utils.getSkyblockWorld(), this.x + x, y + IslandManager.getHeight(),
							this.z + z).getBlock();
					block.setType(Material.AIR);
				}
			}
		}
		return this;
	}

	public void setSpawnLocation(String loc) {
		setSpawnLocation(new Location(Utils.getSkyblockWorld(), Float.parseFloat(loc.split(":")[0]),
				Float.parseFloat(loc.split(":")[1]), Float.parseFloat(loc.split(":")[2])));
	}

	public void setSpawnLocation(Location loc) {
		spawnLoc = loc;
		IslandManager.saveIsland(this);
	}

	public Map<UUID, List<ItemStack>> getInventories() {
		return inventories;
	}

//	public void addInventory(UUID uid, Inventory inv) {
//		inventories.put(uid, inv);
//	}

}
