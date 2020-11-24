package com.teamcqr.chocolatequestrepoured.network.client.handler;

import com.teamcqr.chocolatequestrepoured.CQRMain;
import com.teamcqr.chocolatequestrepoured.client.structureprot.ProtectedRegionClientEventHandler;
import com.teamcqr.chocolatequestrepoured.network.server.packet.SPacketAddOrResetProtectedRegionIndicator;
import com.teamcqr.chocolatequestrepoured.structureprot.ProtectedRegion;
import com.teamcqr.chocolatequestrepoured.structureprot.ProtectedRegionManager;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CPacketHandlerAddOrResetProtectedRegionIndicator implements IMessageHandler<SPacketAddOrResetProtectedRegionIndicator, IMessage> {

	@Override
	public IMessage onMessage(SPacketAddOrResetProtectedRegionIndicator message, MessageContext ctx) {
		if (ctx.side.isClient()) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				World world = CQRMain.proxy.getWorld(ctx);
				ProtectedRegionManager protectedRegionManager = ProtectedRegionManager.getInstance(world);

				if (protectedRegionManager != null) {
					ProtectedRegion protectedRegion = protectedRegionManager.getProtectedRegion(message.getUuid());

					if (protectedRegion != null) {
						ProtectedRegionClientEventHandler.addOrResetProtectedRegionIndicator(protectedRegion, message.getPos(), null);
					}
				}
			});
		}
		return null;
	}

}