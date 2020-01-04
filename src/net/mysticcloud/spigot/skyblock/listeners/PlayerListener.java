package net.mysticcloud.spigot.skyblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.Utils;

public class PlayerListener implements Listener {

	public PlayerListener(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().teleport(Utils.getSpawnWorld().getSpawnLocation());
	}
}
