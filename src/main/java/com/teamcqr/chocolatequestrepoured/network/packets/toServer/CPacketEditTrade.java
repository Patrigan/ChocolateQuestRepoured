package com.teamcqr.chocolatequestrepoured.network.packets.toServer;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CPacketEditTrade implements IMessage {

	private int entityId;
	private int tradeIndex;
	private boolean[] ignoreMeta;
	private boolean[] ignoreNBT;

	public CPacketEditTrade() {

	}

	public CPacketEditTrade(int entityId, int tradeIndex, boolean[] ignoreMeta, boolean[] ignoreNBT) {
		this.entityId = entityId;
		this.tradeIndex = tradeIndex;
		this.ignoreMeta = ignoreMeta;
		this.ignoreNBT = ignoreNBT;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.entityId = buf.readInt();
		this.tradeIndex = buf.readByte() + 128;
		this.ignoreMeta = new boolean[buf.readByte()];
		for (int i = 0; i < this.ignoreMeta.length; i++) {
			this.ignoreMeta[i] = buf.readBoolean();
		}
		this.ignoreNBT = new boolean[buf.readByte()];
		for (int i = 0; i < this.ignoreNBT.length; i++) {
			this.ignoreNBT[i] = buf.readBoolean();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeByte(this.tradeIndex - 128);
		buf.writeByte(this.ignoreMeta.length);
		for (int i = 0; i < this.ignoreMeta.length; i++) {
			buf.writeBoolean(this.ignoreMeta[i]);
		}
		buf.writeByte(this.ignoreNBT.length);
		for (int i = 0; i < this.ignoreNBT.length; i++) {
			buf.writeBoolean(this.ignoreNBT[i]);
		}
	}

	public int getEntityId() {
		return this.entityId;
	}

	public int getTradeIndex() {
		return this.tradeIndex;
	}

	public boolean[] getIgnoreMeta() {
		return this.ignoreMeta;
	}

	public boolean[] getIgnoreNBT() {
		return this.ignoreNBT;
	}

}
