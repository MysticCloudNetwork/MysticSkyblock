package net.mysticcloud.spigot.skyblock.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Schematic {
	
	short width = 0;
	short height = 0;
	short length = 0;
	
	List<Integer> blocks = new ArrayList<>();
	Map<Integer,SkyBlockData> data = new HashMap<>();
	
	List entities = null;
	List tileentities = null;
	
	public Schematic(short width, short height, short length, List<Integer> blocks, Map<Integer, SkyBlockData> data) {
		this.width = width;
		this.height = height;
		this.length = length;
		
		this.blocks = blocks;
		this.data = data;
	}

	public short getWidth() {
		return width;
	}
	public short getHeight() {
		return height;
	}
	public short getLength() {
		return length;
	}
	public List<Integer> getBlocks() {
		return blocks;
	}
	public Map<Integer,SkyBlockData> getData() {
		return data;
	}
	public List getEntities() {
		return entities;
	}
	public List getTileEntities() {
		return tileentities;
	}

}
