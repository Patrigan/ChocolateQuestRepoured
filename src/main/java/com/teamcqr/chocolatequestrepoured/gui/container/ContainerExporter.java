package com.teamcqr.chocolatequestrepoured.gui.container;

/*
 * Copyright (c) 29.08.2019
 * Developed by MrMarnic
 * GitHub: https://github.com/MrMarnic
 */

import com.teamcqr.chocolatequestrepoured.tileentity.TileEntityExporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

public class ContainerExporter extends Container {

    private TileEntityExporter entityExporter;

    public ContainerExporter(TileEntityExporter entityExporter) {
        this.entityExporter = entityExporter;
    }


    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
