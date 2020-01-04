package net.mysticcloud.spigot.skyblock.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import net.mysticcloud.spigot.skyblock.Main;

public class BlockListener implements Listener {

	public BlockListener(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBlockGenerate(BlockPlaceEvent e) {
		
		
//		Bukkit.broadcastMessage(e.getBlock().getType() + " : " + e.get);
//		if (e.getBlock().getType().equals(Material.LAVA) && e.getToBlock().getType().equals(Material.AIR)) {
//			
//			
//			int chance = (int) new Random().nextDouble() * 10;
//			for (Entry<String, Integer> entry : Utils.rarities.entrySet()) {
//				if (entry.getValue() < chance) {
//					e.getBlock().setType(Material.valueOf(entry.getKey()));
//				}
//			}
//		}
	}
}
