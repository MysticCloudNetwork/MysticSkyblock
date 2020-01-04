package net.mysticcloud.spigot.skyblock.utils;

import org.bukkit.Material;

public class SkyBlockData {

	Material mat;
	String data;

	public SkyBlockData(Material mat, String data) {
		this.mat = mat;
		this.data = data;
	}
	
	public Material getMaterial() {
		return mat;
	}
	
	public String getData() {
		return data;
	}
}