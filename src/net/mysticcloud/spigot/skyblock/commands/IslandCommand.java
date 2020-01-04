package net.mysticcloud.spigot.skyblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import net.mysticcloud.spigot.core.utils.CoreUtils;
import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.SkyblockPlayer;
import net.mysticcloud.spigot.skyblock.utils.Utils;
import net.mysticcloud.spigot.skyblock.utils.islands.IslandManager;

public class IslandCommand implements CommandExecutor {

	public IslandCommand(String cmd, Main plugin) {
		plugin.getCommand(cmd).setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = ((Player) sender);
			SkyblockPlayer pl = Utils.getSkyblockPlayer(player.getUniqueId());
			if (args.length == 0) {
				if (pl.getIslands().size() == 0) {
					player.setMetadata("islandtypeselector", new FixedMetadataValue(Main.getPlugin(), "true"));
					player.openInventory(IslandManager.getIslandTypeSelectorGUI(player));
				}
				if (pl.getIslands().size() == 1) {
					player.setMetadata("islandmenu",
							new FixedMetadataValue(Main.getPlugin(), pl.getIslands().toArray()[0]));
					player.openInventory(IslandManager.getIslandMenuGUI(player,
							Integer.parseInt(pl.getIslands().toArray()[0] + "")));
				}
				if (pl.getIslands().size() > 1) {
					player.setMetadata("islandselector", new FixedMetadataValue(Main.getPlugin(), "true"));
					player.openInventory(IslandManager.getIslandSelectorGUI(player, pl.getIslands()));
				}

			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("create")) {
					player.setMetadata("islandtypeselector", new FixedMetadataValue(Main.getPlugin(), "true"));
					player.openInventory(IslandManager.getIslandTypeSelectorGUI(player));
				}
				if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
					String is = args.length == 2 ? args[1] : pl.getIsland();
					if (is == "") {
						player.sendMessage(CoreUtils.colorize(
								"&eSkyblock &7>&f You must be on an island or specify an island to use this command."));
						return true;
					}
					if (player.hasPermission("mysticcloud.skyblock.cmd.admin.delete")) {
						if (!pl.getIslands().contains(is))
							player.sendMessage(CoreUtils
									.colorize("&eSkyblock &7>&f You don't own this island. Using admin override."));
						IslandManager.destroyIsland(Integer.parseInt(is));
					} else {
						if (pl.getIslands().contains(is))

							IslandManager.destroyIsland(Integer.parseInt(is));
						else
							player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Sorry you don't own that island."));
					}

				}
				if (args[0].equalsIgnoreCase("regen") || args[0].equalsIgnoreCase("regenerate")) {
					String is = args.length == 2 ? args[1] : pl.getIsland();
					if (is == "") {
						player.sendMessage(CoreUtils.colorize(
								"&eSkyblock &7>&f You must be on an island or specify an island to use this command."));
						return true;
					}
					if (player.hasPermission("mysticcloud.skyblock.cmd.admin.regen")) {
						if (!pl.getIslands().contains(is))
							player.sendMessage(CoreUtils
									.colorize("&eSkyblock &7>&f You don't own this island. Using admin override."));
						IslandManager.getIsland(Integer.parseInt(is)).regen();
					} else {
						if (pl.getIslands().contains(is))
							IslandManager.getIsland(Integer.parseInt(is)).regen();
						else
							player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Sorry you don't own that island."));
					}

				}
				if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Below is a list of skyblock commands:"));
					if (player.hasPermission("mysticcloud.skyblock.cmd.admin"))
						player.sendMessage(CoreUtils.colorize("&aKey: &c���&a: Admin, &e���&a: Other"));

					player.sendMessage(CoreUtils.colorize("&e - /is -> Opens GUI menu."));
					player.sendMessage(CoreUtils.colorize("&e - /is help -> Shows these help messages."));
					player.sendMessage(CoreUtils.colorize(
							"&e - /is home [islandID] -> Teleports you to the spawn point of the island you are currently on, or the island you specify with the optional [islandID] argument.."));
					if (player.hasPermission("mysticcloud.skyblock.cmd.admin.is.reset.metadata"))
						player.sendMessage(CoreUtils.colorize(
								"&c - /is reset metadata [player] -> Resets you metadata or the metadata of the player specified with [player]"));
					player.sendMessage(CoreUtils.colorize(
							"&e - /is regen|regenerate [islandID] -> Regenerates current or specified island."));
					player.sendMessage(CoreUtils
							.colorize("&e - /is delete|remove [islandID] -> Deletes current or specified island."));
				}
				if (args[0].equalsIgnoreCase("home")) {
					String home = args.length == 2 ? args[1] : pl.getIsland();
					if (home.equals("") && pl.getIslands().size() == 0) {
						player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Sorry you must join an island first."));
						return true;
					}
					if (home.equals("") && pl.getIslands().size() != 0) {
						home = pl.getIslands().toArray()[0] + "";
					}
					Bukkit.broadcastMessage(home);
					Bukkit.broadcastMessage(""+IslandManager.getIslands().size());
					for(String is : IslandManager.getIslands()) {
						Bukkit.broadcastMessage(is + " -> " + ((IslandManager.getIsland(Integer.parseInt(is)) == null) ? "Null" : "Not null"));
					}
					if (player.hasPermission("mysticcloud.skyblock.cmd.admin.home")) {
						IslandManager.getIsland(Integer.parseInt(home)).join(player);
					} else {
						if (pl.getIslands().contains(home)) {
							IslandManager.getIsland(Integer.parseInt(home)).join(player);
						} else {
							player.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Sorry you don't own that island."));
						}
					}

				}
				if (args[0].equalsIgnoreCase("reset")) {
					if (args.length >= 2) {
						if (args[1].equalsIgnoreCase("metadata")) {
							String plr = player.getName();
							if (args.length == 3) {
								plr = args[2];
								if (Bukkit.getPlayer(plr) == null) {
									player.sendMessage(CoreUtils.colorize(
											"&eSkyblock &7>&f A player must me online to reset their metadata"));
									return true;
								}
							}
							Bukkit.getPlayer(plr).removeMetadata("islandmenu", Main.getPlugin());
							Bukkit.getPlayer(plr).removeMetadata("islandtypeselector", Main.getPlugin());
							Bukkit.getPlayer(plr).removeMetadata("islandselector", Main.getPlugin());
						}
					} else {
						player.sendMessage(CoreUtils.colorize(
								"&c - /is reset metadata [player] -> Resets you metadata or the metadata of the player specified with [player]."));
					}

				}
			}

//			IslandManager.nextIsland(player, IslandManager.types.get(0)).build();

		} else {
			if (args.length == 0) {
				sender.sendMessage(CoreUtils.colorize("&eSkyblock &7>&f Below is a list of admin commands:"));
				sender.sendMessage(CoreUtils.colorize(
						"&e - /is reset metadata <player> -> Resets you metadata or the metadata of the player specified with [player]"));
				sender.sendMessage(
						CoreUtils.colorize("&e - /is regen|regenerate <islandID> -> Regenerates specified island."));
			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("regen") || args[0].equalsIgnoreCase("regenerate")) {

					if (args.length == 2)
						IslandManager.getIsland(Integer.parseInt(args[1])).regen();
					else
						sender.sendMessage(CoreUtils
								.colorize("&e - /is regen|regenerate <islandID> -> Regenerates specified island."));
				}
				if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
					if (args.length == 2)
						IslandManager.destroyIsland(Integer.parseInt(args[1]));
					else
						sender.sendMessage(
								CoreUtils.colorize("&e - /is delete|remove <islandID> -> Deletes specified island."));
				}
			}
		}
		return true;
	}
}
