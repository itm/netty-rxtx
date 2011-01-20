package de.uniluebeck.itm.nettyrxtx.isense;


import de.uniluebeck.itm.nettyrxtx.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.ReadOnlyChannelBuffer;

public class ISensePacket {

	private final ChannelBuffer buffer;

	/**
	 * Creates a new ISensePacket instance that creates a new wrapping ChannelBuffer around {@code type} and
	 * {@code payload}. Any changes made to {@code payload} will reflect on this packet.
	 *
	 * @param type	the type of the packet (see {@link ISensePacketType})
	 * @param payload the packets payload
	 * @see ChannelBuffers#wrappedBuffer(java.nio.ByteBuffer...)
	 */
	public ISensePacket(final byte type, final ChannelBuffer payload) {
		this.buffer = ChannelBuffers.wrappedBuffer(ChannelBuffers.wrappedBuffer(new byte[]{type}), payload);
	}

	/**
	 * Creates a new ISensePacket instance using {@code buffer} as its backing buffer.
	 *
	 * @param buffer the backing buffer
	 */
	public ISensePacket(ChannelBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Creates a new ISensePacket by calling {@code this(type.getValue(), channelBuffer)}.
	 *
	 * @param type the type of the packet
	 * @param channelBuffer the packets payload
	 */
	public ISensePacket(ISensePacketType type, ChannelBuffer channelBuffer) {
		this(type.getValue(), channelBuffer);
	}

	/**
	 * Returns a slice of the underlying {@link ChannelBuffer} containing the packets payload.
	 *
	 * @return a slice of the underlying ChannelBuffer
	 * @see ChannelBuffer#slice(int, int)
	 */
	public ChannelBuffer getPayload() {
		return buffer.slice(1, buffer.readableBytes() - 1);
	}

	/**
	 * Returns the {@code type} byte of the packet. This method does not modify the ChannelBuffer backing this packet.
	 *
	 * @return the type byte
	 */
	public byte getType() {
		return buffer.getByte(0);
	}

	@Override
	public String toString() {
		ISensePacketType packetType = ISensePacketType.fromValue(getType());
		StringBuilder builder = new StringBuilder();
		builder.append("ISensePacket[type=");
		builder.append(packetType == null ? getType() : packetType);
		builder.append(",payload=");
		if (packetType == ISensePacketType.LOG) {
			ChannelBuffer buffer = getPayload();
			byte[] payloadBuffer = new byte[buffer.readableBytes()];
			buffer.readBytes(payloadBuffer);
			String s = new String(payloadBuffer);
			builder.append(s.endsWith("\n") ? s.substring(0, s.length() - 2) : s);
		} else {
			builder.append(StringUtils.toHexString(getPayload()));
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the buffer backing this packet. <i>Please note:</i> This is the original buffer, any modifications made
	 * to it will reflect on the packet.
	 *
	 * @return this packets backing buffer
	 */
	public ChannelBuffer getBuffer() {
		return buffer;
	}
}
