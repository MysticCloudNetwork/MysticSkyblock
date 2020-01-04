package net.mysticcloud.spigot.skyblock.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.github.yannicklamprecht.worldborder.api.BorderAPI;
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;

import me.hx64.voidgenerator.VoidGenerator;
import net.mysticcloud.spigot.core.utils.CoreUtils;
import net.mysticcloud.spigot.skyblock.Main;

public class Utils {

	static Map<UUID, SkyblockPlayer> sbplayers = new HashMap<>();
	public static Map<String, Integer> rarities = new HashMap<>();
	static World spawnWorld = null;
	static World skyblockWorld = null;
	static WorldBorderApi wbapi = null;

	public static World getSkyblockWorld() {
		if (skyblockWorld == null) {
			if (Bukkit.getWorld(Main.getPlugin().getConfig().getString("SKYBLOCK_WORLD")) == null) {
				VoidGenerator vg = (VoidGenerator) Bukkit.getPluginManager().getPlugin("VoidGenerator");
				WorldCreator c = new WorldCreator(Main.getPlugin().getConfig().getString("SKYBLOCK_WORLD"));
				c.generator(vg.getDefaultWorldGenerator(Main.getPlugin().getConfig().getString("SKYBLOCK_WORLD"),
						"PLAINS"));
				skyblockWorld = c.createWorld();
			} else {
				skyblockWorld = Bukkit.getWorld(Main.getPlugin().getConfig().getString("SKYBLOCK_WORLD"));
			}

		}
		return skyblockWorld;
	}

	public static WorldBorderApi getWorldBorderAPI() {
		if (wbapi == null)
			wbapi = BorderAPI.getApi();

		return wbapi;

	}

	public static World getSpawnWorld() {
		if (spawnWorld == null) {
			if (Bukkit.getWorld(Main.getPlugin().getConfig().getString("SPAWN_WORLD")) == null) {
				VoidGenerator vg = (VoidGenerator) Bukkit.getPluginManager().getPlugin("VoidGenerator");
				WorldCreator c = new WorldCreator(Main.getPlugin().getConfig().getString("SPAWN_WORLD"));
				c.generator(
						vg.getDefaultWorldGenerator(Main.getPlugin().getConfig().getString("SPAWN_WORLD"), "PLAINS"));
				spawnWorld = c.createWorld();
			} else {
				spawnWorld = Bukkit.getWorld(Main.getPlugin().getConfig().getString("SPAWN_WORLD"));
			}

		}
		return spawnWorld;
	}

	public static SkyblockPlayer getSkyblockPlayer(UUID uid) {
		if (sbplayers.containsKey(uid)) {
			return sbplayers.get(uid);
		}
		try {
			for (File file : new File(Main.getPlugin().getDataFolder() + "/Players").listFiles()) {
				if (file.getName().replace(".yml", "").equals(uid + "")) {
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					SkyblockPlayer player = new SkyblockPlayer(uid, Integer.parseInt(fc.getString("Max_Islands")));
					for (String s : fc.getStringList("Islands")) {
						player.addIsland(Integer.parseInt(s));
					}
					sbplayers.put(uid, player);
					CoreUtils.debug("Registered player: " + uid);
					return player;

				}
			}

		} catch (NullPointerException ex) {
			new File(Main.getPlugin().getDataFolder() + "/Players").mkdir();

		}
		if (Bukkit.getPlayer(uid) == null) {
			Bukkit.getConsoleSender().sendMessage(CoreUtils
					.colorize("&cA new SkyblockPlayer was requested while the associated player was offline."));
			return null;
		}

		Player pl = Bukkit.getPlayer(uid);
		int mi = 0;
		if (pl.hasPermission("mysticcloud.skyblock.islands.1")) {
			mi = 1;
		}
		if (pl.hasPermission("mysticcloud.skyblock.islands.2")) {
			mi = 2;
		}
		if (pl.hasPermission("mysticcloud.skyblock.islands.3")) {
			mi = 3;
		}
		if (pl.hasPermission("mysticcloud.skyblock.islands.4")) {
			mi = 4;
		}
		if (pl.hasPermission("mysticcloud.skyblock.islands.5")) {
			mi = 5;
		}
		if (mi == 0)
			mi = 1;

		SkyblockPlayer player = new SkyblockPlayer(uid, mi);

		sbplayers.put(uid, player);

		saveSkyblockPlayer(player);

		return player;

	}

	
	
	public static void saveSkyblockPlayer(Player player) {
		saveSkyblockPlayer(getSkyblockPlayer(player.getUniqueId()));
	}
	public static void saveSkyblockPlayer(UUID player) {
		saveSkyblockPlayer(getSkyblockPlayer(player));
	}

	public static void saveSkyblockPlayer(SkyblockPlayer player) {
		File file;
		try {
			file = new File(Main.getPlugin().getDataFolder() + "/Players/" + player.getUUID() + ".yml");
		} catch (NullPointerException ex) {
			// Create a file for this player
			new File(Main.getPlugin().getDataFolder() + "/Players").mkdir();
			file = new File(Main.getPlugin().getDataFolder() + "/Players/" + player.getUUID() + ".yml");
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		fc.set("Max_Islands", player.getMaxIslands() + "");
		fc.set("Islands", player.getIslands());

		try {
			fc.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Schematic loadSchematic(String fn) throws NullPointerException {
		try {
			File f = new File(Main.getPlugin().getDataFolder().getPath() + "/" + fn);
			FileInputStream fis = new FileInputStream(f);
			NBTInputStream input = new NBTInputStream(fis);

			Tag tag = input.readTag();

			CompoundMap compound = ((CompoundTag) tag).getValue();
			Tag palette = compound.get("Palette");
			Tag width = compound.get("Width");
			Tag height = compound.get("Height");
			Tag length = compound.get("Length");
			Map<Integer, SkyBlockData> data = new HashMap<>();
			List<Integer> blocks = new ArrayList<>();
			Tag blocktag = compound.get("BlockData");
			for (byte b : ((byte[]) blocktag.getValue()))
				blocks.add(Integer.parseInt(b + ""));

			for (int i = 0; i != Integer.parseInt(compound.get("PaletteMax").getValue() + ""); i++) {
				String key = (((CompoundMap) palette.getValue()).values().toArray()[i]) + "";
				key = key.replace("TAG_Int(\"minecraft:", "");
				key = key.replace("\")", "");
				key = key.replace(" ", "");
				key = key.replace("[", " ");
				key = key.replace("]", "");
				String matdat = "";
				String matna = "";
				if (key.contains(" ")) {
					matdat = key.split(":")[0].split(" ")[1];
					matna = key.split(":")[0].split(" ")[0];
				}
				if (matna == "")
					matna = key.split(":")[0];
				if(matna.equalsIgnoreCase("__RESERVED__")) continue;
				
				data.put(Integer.parseInt(key.split(":")[1]),
						new SkyBlockData(Material.valueOf(matna.toUpperCase()), matdat));

			}

			input.close();
			fis.close();

			return new Schematic(((short) width.getValue()), ((short) height.getValue()), ((short) length.getValue()),
					blocks, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setRarity(String material, int percent) {
		rarities.put(material,percent);
	}

}
