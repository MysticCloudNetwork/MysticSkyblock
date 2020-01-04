package net.mysticcloud.spigot.skyblock.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

public class SkyblockPlayer {
	
	private int maxislands = 0;
	
	List<String> islands = new ArrayList<>();
	String cisland = "";
	
	UUID uid;
	
	protected SkyblockPlayer (UUID uid, int maxislands) {
		this.maxislands = maxislands;
		this.uid = uid;

	}
	
	public void setIslands(List<String> islands) {
		this.islands = islands;
	}
	public void addIsland(int id) {
		if(!islands.contains(id+"")) {
			islands.add(id+"");
		}
		save();
		
	}
	
	private void save() {
		Utils.saveSkyblockPlayer(this);
	}
	public void setIsland(String island) {
		cisland = island;
		save();
	}
	public String getIsland() {
		return cisland;
	}

	public UUID getUUID() {
		return uid;
	}

	public int getMaxIslands() {
		return maxislands;
	}
	public List<String> getIslands(){
		return islands;
	}

	public void removeIsland(int pid) {
		islands.remove(pid+"");
	}

}
