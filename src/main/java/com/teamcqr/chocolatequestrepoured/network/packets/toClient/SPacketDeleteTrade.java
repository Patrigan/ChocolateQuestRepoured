package com.teamcqr.chocolatequestrepoured.network.packets.toClient;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class SPacketDeleteTrade implements IMessage {

	private int entityId;
	private int tradeIndex;

	public SPacketDeleteTrade() {

	}

	public SPacketDeleteTrade(int entityId, int tradeIndex) {
		this.entityId = entityId;
		this.tradeIndex = tradeIndex;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityId = buf.readInt();
		this.tradeIndex = buf.readByte() + 128;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeByte(this.tradeIndex - 128);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public int getTradeIndex() {
		return this.tradeIndex;
	}

}
