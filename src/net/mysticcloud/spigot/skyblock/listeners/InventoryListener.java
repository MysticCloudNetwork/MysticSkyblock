package net.mysticcloud.spigot.skyblock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.metadata.FixedMetadataValue;

import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.Utils;
import net.mysticcloud.spigot.skyblock.utils.islands.Island;
import net.mysticcloud.spigot.skyblock.utils.islands.IslandManager;
import net.mysticcloud.spigot.skyblock.utils.islands.IslandType;

public class InventoryListener implements Listener {

	public InventoryListener(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getPlayer().hasMetadata("islandmenu") || e.getPlayer().hasMetadata("islandselector") || e.getPlayer().hasMetadata("islandtypeselector")) {
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {

				@Override
				public void run() {
					if(e.getPlayer().getOpenInventory() == null) {
						e.getPlayer().removeMetadata("islandmenu", Main.getPlugin());
						e.getPlayer().removeMetadata("islandselector", Main.getPlugin());
						e.getPlayer().removeMetadata("islandtypeselector", Main.getPlugin());
					}
				}
				
			}, 3*20);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {

		if (e.getCurrentItem() == null)
			return;

		if (e.getWhoClicked().hasMetadata("islandselector")) {
			for (IslandType type : IslandManager.types) {
				if (e.getCurrentItem().getType().equals(type.getGUIItem().getType())) {
					e.getWhoClicked().removeMetadata("islandselector", Main.getPlugin());
					e.getWhoClicked().setMetadata("islandmenu", new FixedMetadataValue(Main.getPlugin(),
							(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()))));
					e.getWhoClicked().openInventory(IslandManager.getIslandMenuGUI((Player) e.getWhoClicked(),
							Integer.parseInt(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()))));

					return;
				}
			}

		}
		if (e.getWhoClicked().hasMetadata("islandtypeselector")) {

			for (IslandType itype : IslandManager.types) {
				if (e.getCurrentItem().getType().equals(itype.getGUIItem().getType())) {
					
					Island is = IslandManager.nextIsland((Player) e.getWhoClicked(), itype);
					Utils.getSkyblockPlayer(e.getWhoClicked().getUniqueId()).addIsland(is.getID());
					is.join((Player)e.getWhoClicked());
					e.getWhoClicked().removeMetadata("islandtypeselector", Main.getPlugin());
					break;
				}
			}

			e.setCancelled(true);

		}
		if (e.getWhoClicked().hasMetadata("islandmenu")) {
			Island is = IslandManager
					.getIsland(Integer.parseInt("" + e.getWhoClicked().getMetadata("islandmenu").get(0).value()));

			if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
				is.leave((Player)e.getWhoClicked());
				IslandManager.destroyIsland(is.getID());
				Utils.getSkyblockPlayer(e.getWhoClicked().getUniqueId()).removeIsland(is.getID());
				e.getWhoClicked().teleport(Utils.getSpawnWorld().getSpawnLocation());
				e.getWhoClicked().removeMetadata("islandmenu", Main.getPlugin());
				return;
			}

			if (e.getCurrentItem().getType().equals(Material.GRASS_BLOCK)) {
				is.destroy().build().join((Player)e.getWhoClicked());
				e.getWhoClicked().removeMetadata("islandmenu", Main.getPlugin());

			}

			if (e.getCurrentItem().getType().equals(Material.WHEAT_SEEDS)) {
				e.getWhoClicked().setMetadata("islandtypeselector", new FixedMetadataValue(Main.getPlugin(), "true"));
				e.getWhoClicked().openInventory(IslandManager.getIslandTypeSelectorGUI((Player) e.getWhoClicked()));
				e.getWhoClicked().removeMetadata("islandmenu", Main.getPlugin());

			}

			if (e.getCurrentItem().getType().equals(Material.RED_BED)) {
				if (is.getSpawnLocation() == null) {
				} else {
					is.join(((Player)e.getWhoClicked()));
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().removeMetadata("islandmenu", Main.getPlugin());
				}

			}
			e.setCancelled(true);

		}
	}
}
