package com.teamcqr.chocolatequestrepoured.network.server.packet;

import com.teamcqr.chocolatequestrepoured.structureprot.ProtectedRegion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SPacketAddProtectedRegion implements IMessage {

	private ByteBuf buffer = Unpooled.buffer();

	public SPacketAddProtectedRegion() {

	}

	public SPacketAddProtectedRegion(ProtectedRegion protectedRegion) {
		protectedRegion.writeToByteBuf(this.buffer);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.buffer.writeBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBytes(this.buffer);
	}

	public ByteBuf getBuffer() {
		return this.buffer;
	}

}