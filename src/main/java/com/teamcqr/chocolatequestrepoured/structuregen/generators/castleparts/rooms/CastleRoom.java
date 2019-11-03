package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms;

import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.addons.CastleAddonDoor;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.segments.DoorPlacement;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.segments.RoomWallBuilder;
import com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.segments.RoomWalls;
import com.teamcqr.chocolatequestrepoured.util.BlockPlacement;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWoodSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public abstract class CastleRoom
{
    public enum RoomType
    {
        NONE(0, "None", false),
        HALLWAY(1, "Hallway", false),
        KITCHEN(2, "Kitchen", false),
        STAIRCASE_DIRECTED(3, "Directed Stairs", true),
        STAIRCASE_SPIRAL(4, "Spiral Stairs", true),
        LANDING_DIRECTED(5, "Directed Landing", true),
        LANDING_SPIRAL(6, "Spiral Landng", true),
        TOWER_SQUARE(7, "Square Tower", false);

        private final int index;
        private final String name;
        private final boolean partOfStairs;

        RoomType(int indexIn, String nameIn, boolean partOfStairsIn)
        {
            this.index = indexIn;
            this.name = nameIn;
            this.partOfStairs = partOfStairsIn;
        }

        public boolean isPartOfStairs()
        {
            return this.partOfStairs;
        }
    }

    BlockPos startPos;
    protected int height;
    protected int sideLength;

    //The following variables are used for rooms that build blocks in a smaller area than the
    //actual room occupies (such as towers). For most room types they will be not be changed from
    //the values set in the default constructor.
    protected int buildLength; //actual length of constructed part of room
    protected int offsetX; //x offset from startPos that actual room starts
    protected int offsetZ; //z offset from startPos that actual room starts

    //The counts represent how many roomSizes this room uses in a given direction
    //so for example if countX was 2, the actual x size would be x*roomSize
    protected int countX;
    protected int countY;
    protected int countZ;

    protected int maxSlotsUsed = 1; //Max number of contiguous room grid slots this can occupy

    protected ArrayList<CastleAddonDoor> doors;
    protected RoomType roomType = RoomType.NONE;
    protected boolean defaultCeiling = false;
    protected Random random = new Random();

    protected HashSet<EnumFacing> roofEdges;

    protected RoomWalls walls;

    public CastleRoom(BlockPos startPos, int sideLength, int height)
    {
        this.startPos = startPos;
        this.sideLength = sideLength;
        this.offsetX = 0;
        this.offsetZ = 0;
        this.buildLength = this.sideLength;
        this.height = height;
        this.doors = new ArrayList<>();
        this.roofEdges = new HashSet<>();
        this.walls = new RoomWalls();
    }

    public void generate(ArrayList<BlockPlacement> blocks)
    {
        generateRoom(blocks);
        generateWalls(blocks);
        generateRoofEdges(blocks);

        if (defaultCeiling)
        {
            generateDefaultCeiling(blocks);
        }
    }

    public abstract void generateRoom(ArrayList<BlockPlacement> blocks);
    public abstract String getNameShortened();

    protected void generateWalls(ArrayList<BlockPlacement> blocks)
    {
        for (EnumFacing side : EnumFacing.HORIZONTALS)
        {
            if (walls.hasWallOnSide(side))
            {
                BlockPos buildPos = getbuildPosition();
                RoomWallBuilder builder = new RoomWallBuilder(buildPos, height, buildLength, walls.getOptionsForSide(side), side);
                builder.generate(blocks);
            }
        }
    }

    public void addRoofEdge(EnumFacing side)
    {
        roofEdges.add(side);
    }

    public boolean canBuildDoorOnSide(EnumFacing side)
    {
        return true;
    }

    public boolean canBuildInnerWallOnSide(EnumFacing side) { return true; }

    public boolean reachableFromSide(EnumFacing side)
    {
        return true;
    }

    public boolean hasRoofEdgeOnSide(EnumFacing side)
    {
        return (roofEdges.contains(side));
    }

    public boolean isTower()
    {
        return false;
    }

    protected void generateDefaultCeiling(ArrayList<BlockPlacement> blocks)
    {
        for (int z = 0; z < buildLength - 1; z++)
        {
            for (int x = 0; x < buildLength - 1; x++)
            {
                blocks.add(new BlockPlacement(startPos.add( x, height - 1, z), Blocks.STONEBRICK.getDefaultState()));
            }
        }
    }

    protected void generateRoofEdges(ArrayList<BlockPlacement> blocks)
    {
        IBlockState wallBlock = Blocks.STONEBRICK.getDefaultState();
        int len = buildLength;

        if (hasRoofEdgeOnSide(EnumFacing.NORTH))
        {
            for (int x = 0; x < len; x++)
            {
                BlockPos pos = startPos.add(x + offsetX, height, offsetZ);
                blocks.add(new BlockPlacement(pos, wallBlock));
                if (shouldBuildCrenellation(len, x))
                {
                    blocks.add(new BlockPlacement(pos.up(), wallBlock));
                }
            }
        }
        if (hasRoofEdgeOnSide(EnumFacing.SOUTH))
        {
            for (int x = 0; x < len; x++)
            {
                BlockPos pos = startPos.add(x + offsetX, height, offsetZ + buildLength - 1);
                blocks.add(new BlockPlacement(pos, wallBlock));
                if (shouldBuildCrenellation(len, x))
                {
                    blocks.add(new BlockPlacement(pos.up(), wallBlock));
                }
            }
        }
        if (hasRoofEdgeOnSide(EnumFacing.WEST))
        {
            for (int z = 0; z < len; z++)
            {
                BlockPos pos = startPos.add(offsetX, height, z + offsetZ);
                blocks.add(new BlockPlacement(pos, wallBlock));
                if (shouldBuildCrenellation(len, z))
                {
                    blocks.add(new BlockPlacement(pos.up(), wallBlock));
                }
            }
        }
        if (hasRoofEdgeOnSide(EnumFacing.EAST))
        {
            for (int z = 0; z < len; z++)
            {
                BlockPos pos = startPos.add(offsetX + buildLength - 1, height, z + offsetZ);
                blocks.add(new BlockPlacement(pos, wallBlock));
                if (shouldBuildCrenellation(len, z))
                {
                    blocks.add(new BlockPlacement(pos.up(), wallBlock));
                }
            }
        }
    }

    private boolean shouldBuildCrenellation(int wallLength, int index)
    {
        return (index == 0 || index == wallLength - 1 || index % 2 == 0);
    }

    public RoomType getRoomType()
    {
        return roomType;
    }

    private void roomDecoTable(BlockPos pos, ArrayList<BlockPlacement> blocks)
    {
        IBlockState legBlock = Blocks.OAK_FENCE.getDefaultState();
        IBlockState topBlock = Blocks.WOODEN_SLAB.getDefaultState();
        topBlock = topBlock.withProperty(BlockWoodSlab.VARIANT, BlockPlanks.EnumType.OAK);
        blocks.add(new BlockPlacement(pos, legBlock));
        blocks.add(new BlockPlacement(pos.add(1, 0, 0), legBlock));
        blocks.add(new BlockPlacement(pos.add(0, 0, 1), legBlock));
        blocks.add(new BlockPlacement(pos.add(1, 0, 1), legBlock));

        blocks.add(new BlockPlacement(pos.add(0, 1, 0), topBlock));
        blocks.add(new BlockPlacement(pos.add(1, 1, 0), topBlock));
        blocks.add(new BlockPlacement(pos.add(0, 1, 1), topBlock));
        blocks.add(new BlockPlacement(pos.add(1, 1, 1), topBlock));
    }

    public BlockPos getRoofStartPosition()
    {
        return startPos.add(offsetX, height, offsetZ);
    }

    protected BlockPos getRotatedPlacement(int x, int y, int z, EnumFacing rotation)
    {
        switch (rotation)
        {
            case EAST:
                return startPos.add(z, y, sideLength - 2 - x);
            case WEST:
                return startPos.add(sideLength - 2 - z, y, x);
            case NORTH:
                return startPos.add(sideLength - 2 - x, y, sideLength - 2 - z);
            case SOUTH:
            default:
                return startPos.add(x, y, z);
        }
    }

    protected int getNumYRotationsFromStartToEndFacing(EnumFacing start, EnumFacing end)
    {
        int rotations = 0;
        if (start.getAxis().isHorizontal() && end.getAxis().isHorizontal())
        {
            while (start != end)
            {
                start = start.rotateY();
                rotations++;
            }
        }
        return rotations;
    }

    protected EnumFacing rotateFacingNTimesAboutY(EnumFacing facing, int n)
    {
        for (int i = 0; i < n; i++)
        {
            facing = facing.rotateY();
        }
        return facing;
    }

    private BlockPos getbuildPosition()
    {
        return startPos.add(offsetX, 0, offsetZ);
    }

    public boolean hasWallOnSide(EnumFacing side) { return walls.hasWallOnSide(side); }

    public boolean hasDoorOnSide(EnumFacing side)
    {
        return walls.hasDoorOnSide(side);
    }

    public DoorPlacement addDoorOnSideCentered(EnumFacing side)
    {
        return walls.addCenteredDoor(buildLength, side);
    }
    public DoorPlacement addDoorOnSideRandom(Random random, EnumFacing side)
    {
        return walls.addRandomDoor(random, buildLength, side);
    }

    public void addOuterWall(EnumFacing side)
    {
        walls.addOuter(side);
    }

    public void addInnerWall(EnumFacing side)
    {
        walls.addInner(side);
    }

    public void removeWall(EnumFacing side)
    {
        walls.removeWall(side);
    }

    public void registerAdjacentRoomDoor(EnumFacing side, DoorPlacement door)
    {
        walls.registerAdjacentDoor(side, door);
    }

    /*
    * Get a list of blocks that make up the decoratable edge of the room.
    * Decoratable positions are adjacent to a wall but not in front of a door.
     */
    protected ArrayList<BlockPos> getDecorationEdge()
    {
        //First get all blocks that are not occupied by walls
        ArrayList<BlockPos> result = getDecorationArea();
        if (!result.isEmpty())
        {
            BlockPos topLeft = result.get(0); //Top left is the first one added
            int xStart = topLeft.getX();
            int zStart = topLeft.getZ();
            int xEnd = xStart + (getDecorationLengthX() - 1);
            int zEnd = zStart + (getDecorationLengthZ() - 1);

            //Now remove all the blocks that aren't at the edges of the decoratable area
            result.removeIf(p -> !(p.getX() == xStart || p.getZ() == zStart || p.getX() == xEnd || p.getZ() == zEnd));

            for (EnumFacing side : EnumFacing.HORIZONTALS)
            {
                if (walls.hasDoorOnSide(side))
                {
                    DoorPlacement placement = walls.getDoorOnSide(side);

                    removePositionsAdjacentToDoor(result, xStart, zStart, xEnd, zEnd, side, placement);
                }
                else if (walls.adjacentRoomHasDoorOnSide(side))
                {
                    DoorPlacement placement = walls.getAdjacentDoor(side);

                    removePositionsAdjacentToDoor(result, xStart, zStart, xEnd, zEnd, side, placement);
                }
            }
        }

        return result;
    }

    private void removePositionsAdjacentToDoor(ArrayList<BlockPos> result, int xStart, int zStart, int xEnd, int zEnd, EnumFacing side, DoorPlacement placement)
    {
        final int doorStart;
        final int doorEnd;

        if (side.getAxis() == EnumFacing.Axis.Z)
        {
            doorStart = startPos.getX() + placement.getOffset();
            doorEnd = doorStart + placement.getWidth() - 1;

            if (side == EnumFacing.NORTH)
            {
                result.removeIf(p -> p.getZ() == zStart && p.getX() >= doorStart && p.getX() <= doorEnd);
            }
            else //SOUTH
            {
                result.removeIf(p -> p.getZ() == zEnd && p.getX() >= doorStart && p.getX() <= doorEnd);
            }
        }
        else
        {
            doorStart = startPos.getZ() + placement.getOffset();
            doorEnd = doorStart + placement.getWidth() - 1;

            if (side == EnumFacing.EAST)
            {
                result.removeIf(p -> p.getX() == xEnd && p.getZ() >= doorStart && p.getZ() <= doorEnd);
            }
            else //WEST
            {
                result.removeIf(p -> p.getX() == xStart && p.getZ() >= doorStart && p.getZ() <= doorEnd);
            }
        }
    }


    /*
    * Get a 1-height square of block positions that represents the lowest y position
    * of a room that can be decorated. In other words, the layer just above the floor
    * that is not already occupied by walls.
     */
    protected ArrayList<BlockPos> getDecorationArea()
    {
        ArrayList<BlockPos> result = new ArrayList<>();
        BlockPos start = getDecorationStartPos().up(); //move up 1 to skip the floor level

        for (int x = 0; x < getDecorationLengthX(); x++)
        {
            for (int z = 0; z < getDecorationLengthZ(); z++)
            {
                result.add(start.add(x, 0, z));
            }
        }

        return result;
    }

    public BlockPos getDecorationStartPos()
    {
        BlockPos result = startPos;

        if (walls.hasWallOnSide(EnumFacing.NORTH))
        {
            result = result.south();
        }
        if (walls.hasWallOnSide(EnumFacing.WEST))
        {
            result = result.east();
        }

        return result;
    }

    public int getDecorationLengthX()
    {
        int result = buildLength;

        if (walls.hasWallOnSide(EnumFacing.WEST))
        {
            --result;
        }
        if (walls.hasWallOnSide(EnumFacing.EAST))
        {
            --result;
        }

        return result;
    }

    public int getDecorationLengthZ()
    {
        int result = buildLength;

        if (walls.hasWallOnSide(EnumFacing.NORTH))
        {
            --result;
        }
        if (walls.hasWallOnSide(EnumFacing.SOUTH))
        {
            --result;
        }

        return result;
    }

    @Override
    public String toString()
    {
        return roomType.name;
    }
}
