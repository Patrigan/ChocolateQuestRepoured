package com.teamcqr.chocolatequestrepoured.structuregen.generators.castleparts.rooms.decoration;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class RoomDecorCauldron extends RoomDecorBase
{
    public RoomDecorCauldron()
    {
        super();
    }

    @Override
    protected void makeSchematic()
    {
        IBlockState blockType = Blocks.CAULDRON.getDefaultState();
        schematic.add(new DecoPlacement(0, 0, 0, blockType));
    }
}
