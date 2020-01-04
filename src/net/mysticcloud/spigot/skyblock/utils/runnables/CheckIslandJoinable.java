package net.mysticcloud.spigot.skyblock.utils.runnables;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.mysticcloud.spigot.skyblock.Main;
import net.mysticcloud.spigot.skyblock.utils.islands.Island;

public class CheckIslandJoinable implements Runnable {
	
	UUID uid;
	Island is;
	
	public CheckIslandJoinable(UUID uid, Island is) {
		this.uid = uid;
		this.is =is;
	}

	@Override
	public void run() {
		if(Bukkit.getPlayer(uid) == null) return;
		
		if(!is.isActive()) {
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new CheckIslandJoinable(uid,is), 40);
		} else {
			is.join(Bukkit.getPlayer(uid));
		}
		
	}

}
