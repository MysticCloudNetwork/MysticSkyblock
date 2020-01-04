package net.mysticcloud.spigot.skyblock.utils.islands;

import org.bukkit.inventory.ItemStack;

public class IslandType {
	
	String schemfile;
	String name;
	ItemStack guiItem;
	
	IslandType(String schematicFile){
		schemfile = schematicFile;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	String getFileName() {
		return schemfile;
	}
	
	public void setItemStack(ItemStack i) {
		guiItem = i;
	}

	public ItemStack getGUIItem() {
		// TODO Auto-generated method stub
		return guiItem;
	}
}
