package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.dungeons.CastleDungeon;

import com.teamcqr.chocolatequestrepoured.util.BlockStateGenArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CastleRoomWalkableRoofTower extends CastleRoomWalkableRoof {
	public CastleRoomWalkableRoofTower(BlockPos startOffset, int sideLength, int height, CastleRoomTowerSquare tower, int floor) {
		super(startOffset, sideLength, height, floor);
		this.roomType = EnumRoomType.WALKABLE_TOWER_ROOF;
		this.pathable = false;
		this.offsetX = tower.getOffsetX();
		this.offsetZ = tower.getOffsetZ();
		this.buildLengthX = tower.getBuildLengthX();
		this.buildLengthZ = tower.getBuildLengthZ();

		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			this.walls.addOuter(side);
		}
	}

	@Override
	public void generateRoom(World world, BlockStateGenArray genArray, CastleDungeon dungeon) {
		super.generateRoom(world, genArray, dungeon);
	}
}
