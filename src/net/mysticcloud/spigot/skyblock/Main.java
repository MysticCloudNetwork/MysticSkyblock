package net.mysticcloud.spigot.skyblock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import net.mysticcloud.spigot.skyblock.commands.IslandCommand;
import net.mysticcloud.spigot.skyblock.listeners.BlockListener;
import net.mysticcloud.spigot.skyblock.listeners.InventoryListener;
import net.mysticcloud.spigot.skyblock.listeners.PlayerListener;
import net.mysticcloud.spigot.skyblock.utils.Utils;
import net.mysticcloud.spigot.skyblock.utils.islands.IslandManager;


public class Main extends JavaPlugin {
	
	private static Main plugin;
	
	
	
	int attempt = 1;
	int maxattempts = 3;

	public void onEnable() {
		
		if(!getConfig().isSet("PLOT_SIZE")) getConfig().set("PLOT_SIZE", 20);
		if(!getConfig().isSet("HEIGHT")) getConfig().set("HEIGHT", 50);
		if(!getConfig().isSet("SPAWN_WORLD")) getConfig().set("SPAWN_WORLD", "Spawn");
		if(!getConfig().isSet("SKYBLOCK_WORLD")) getConfig().set("SKYBLOCK_WORLD", "Skyblock");
		
		//Register block rarities
		if(!getConfig().isSet("Options.Generator.STONE")) getConfig()       .set("Options.Generator.STONE",        "50");
		if(!getConfig().isSet("Options.Generator.GRAVEL")) getConfig()      .set("Options.Generator.GRAVEL",       "30");
		if(!getConfig().isSet("Options.Generator.DIAMOND_ORE")) getConfig() .set("Options.Generator.DIAMOND_ORE",  "10");
		if(!getConfig().isSet("Options.Generator.COAL_ORE")) getConfig()    .set("Options.Generator.COAL_ORE",     "30");
		if(!getConfig().isSet("Options.Generator.IRON_ORE")) getConfig()    .set("Options.Generator.IRON_ORE",     "30");
		if(!getConfig().isSet("Options.Generator.GOLD_ORE")) getConfig()    .set("Options.Generator.GOLD_ORE",     "20");
		if(!getConfig().isSet("Options.Generator.LAPIS_ORE")) getConfig()   .set("Options.Generator.LAPIS_ORE",    "25");
		if(!getConfig().isSet("Options.Generator.QUARTZ_ORE")) getConfig()  .set("Options.Generator.QUARTZ_ORE",   "20");
		if(!getConfig().isSet("Options.Generator.EMERALD_ORE")) getConfig() .set("Options.Generator.EMERALD_ORE",  "5");
		if(!getConfig().isSet("Options.Generator.REDSTONE_ORE")) getConfig().set("Options.Generator.REDSTONE_ORE", "25");
		
		saveConfig();
		
		
		Utils.setRarity("STONE", getConfig().getInt("Options.Generator.STONE"));
		Utils.setRarity("GRAVEL", getConfig().getInt("Options.Generator.GRAVEL"));
		Utils.setRarity("DIAMOND_ORE", getConfig().getInt("Options.Generator.DIAMOND_ORE"));
		Utils.setRarity("COAL_ORE", getConfig().getInt("Options.Generator.COAL_ORE"));
		Utils.setRarity("IRON_ORE", getConfig().getInt("Options.Generator.IRON_ORE"));
		Utils.setRarity("LAPIS_ORE", getConfig().getInt("Options.Generator.LAPIS_ORE"));
		Utils.setRarity("QUARTZ_ORE", getConfig().getInt("Options.Generator.QUARTZ_ORE"));
		Utils.setRarity("GOLD_ORE", getConfig().getInt("Options.Generator.GOLD_ORE"));
		Utils.setRarity("REDSTONE_ORE", getConfig().getInt("Options.Generator.REDSTONE_ORE"));
		Utils.setRarity("EMERALD_ORE", getConfig().getInt("Options.Generator.EMERALD_ORE"));
		
		
		
		
		
		
		
		if(!getServer().getPluginManager().isPluginEnabled("MysticCore") || !getServer().getPluginManager().isPluginEnabled("VoidGenerator") || !getServer().getPluginManager().isPluginEnabled("MysticWorldBorderAPI")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l" + getDescription().getName() + " &f>&7 Can't find MysticCore. Trying again.. Attempt " + attempt + " out of " + maxattempts));
			attempt+=1;
			if(attempt==maxattempts+1){
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l" + getDescription().getName() + " &f>&7 Couldn't find MysticCore after " + maxattempts + " tries. Plugin will not load."));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			Bukkit.getScheduler().runTaskLater(this, new Runnable(){

				@Override
				public void run() {
					onEnable();
				}
				
			}, 20*3);
			return;
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l" + getDescription().getName() + " &f>&7 Found MysticCore! Loading plugin.."));
		}
		plugin = this;
		
		IslandManager.registerIslands();
		
		
		new InventoryListener(this);
		new PlayerListener(this);
		new BlockListener(this);
		new IslandCommand("island", this);
		
		
		
		
		
		
		
	}
	
	
	
	public static Main getPlugin(){
		return plugin;
	}
}