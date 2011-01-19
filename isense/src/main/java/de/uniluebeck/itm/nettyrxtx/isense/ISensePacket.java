package de.uniluebeck.itm.nettyrxtx.isense;


import de.uniluebeck.itm.nettyrxtx.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.ReadOnlyChannelBuffer;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ISensePacket {

	private final byte type;

	private final ReadOnlyChannelBuffer payload;

	public ISensePacket(final byte type, final ChannelBuffer payload) {
		this.type = type;
		this.payload = (ReadOnlyChannelBuffer) ChannelBuffers.unmodifiableBuffer(payload);
	}

	public ISensePacket(final byte type, final ReadOnlyChannelBuffer payload) {
		this.type = type;
		this.payload = payload;
	}

	public ReadOnlyChannelBuffer getPayload() {
		return (ReadOnlyChannelBuffer) ChannelBuffers.unmodifiableBuffer(payload);
	}

	public byte getType() {
		return type;
	}

	@Override
	public String toString() {
		ByteBuffer bb = payload.toByteBuffer();
		ISensePacketType packetType = ISensePacketType.fromValue(type);
		StringBuilder builder = new StringBuilder();
		builder.append("ISensePacket[type=");
		builder.append(packetType == null ? type : packetType);
		builder.append(",payload=");
		if (packetType == ISensePacketType.LOG) {
			ReadOnlyChannelBuffer buffer = getPayload();
			byte[] payloadBuffer = new byte[buffer.readableBytes()];
			buffer.readBytes(payloadBuffer);
			builder.append(new String(payloadBuffer));
		} else {
			builder.append(StringUtils.toHexString(bb));
		}
		builder.append("]");
		return builder.toString();
	}
}
