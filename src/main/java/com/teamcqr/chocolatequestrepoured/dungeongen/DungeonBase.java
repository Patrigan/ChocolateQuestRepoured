package com.teamcqr.chocolatequestrepoured.dungeongen;

import java.util.Properties;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class DungeonBase {
	
	protected IDungeonGenerator generator;
	protected String name;
	private Item placeItem;
	private int underGroundOffset = 0;
	protected int chance;
	protected int[] allowedDims = {0};
	protected boolean unique = false;
	private Block supportBlock;
	private Block supportTopBlock;
	
	protected void generate(int x, int z, World world, Chunk chunk) {
		
	}
	
	public DungeonBase() {
	}
	
	public DungeonBase load(Properties configFile) {
		return this;
	}
	public IDungeonGenerator getGenerator() {
		return this.generator;
	}
	public Item getDungeonPlaceItem() {
		return this.placeItem;
	}
	public String getDungeonName() {
		return this.name;
	}
	public int getSpawnChance() {
		return this.chance;
	}
	public int[] getAllowedDimensions() {
		return this.allowedDims;
	}
	public boolean isUnique() {
		return this.unique;
	}

	public Block getSupportTopBlock() {
		return supportTopBlock;
	}

	public Block getSupportBlock() {
		return supportBlock;
	}

	public int getUnderGroundOffset() {
		return underGroundOffset;
	}

}
