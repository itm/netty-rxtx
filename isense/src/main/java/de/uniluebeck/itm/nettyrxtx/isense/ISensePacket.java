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
		ISensePacketType packetType = ISensePacketType.fromValue(type);
		StringBuilder builder = new StringBuilder();
		builder.append("ISensePacket[type=");
		builder.append(packetType == null ? type : packetType);
		builder.append(",payload=");
		if (packetType == ISensePacketType.LOG) {
			ReadOnlyChannelBuffer buffer = getPayload();
			byte[] payloadBuffer = new byte[buffer.readableBytes()];
			buffer.readBytes(payloadBuffer);
			String s = new String(payloadBuffer);
			builder.append(s.endsWith("\n") ? s.substring(0, s.length()-2) : s);
		} else {
			builder.append(StringUtils.toHexString(payload));
		}
		builder.append("]");
		return builder.toString();
	}
}
