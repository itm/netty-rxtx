package de.uniluebeck.itm.nettyrxtx.rup;

import com.google.common.base.Preconditions;
import de.uniluebeck.itm.nettyrxtx.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;


public class RUPPacketImpl implements RUPPacket {

	private final ChannelBuffer payload;

	private final byte cmdType;

	private final long destination;

	private final long source;

	public RUPPacketImpl(final byte cmdType, final long destination, final long source, final ChannelBuffer... payloads) {
		this(cmdType, destination, source, ChannelBuffers.wrappedBuffer(payloads));
	}

	public RUPPacketImpl(final Type cmdType, final long destination, final long source, final ChannelBuffer... payloads) {
		this(cmdType.getValue(), destination, source, ChannelBuffers.wrappedBuffer(payloads));
	}

	public RUPPacketImpl(final Type cmdType, final long destination, final long source, final ChannelBuffer payload) {
		this(cmdType.getValue(), destination, source, payload);
	}

	public RUPPacketImpl(final byte cmdType, final long destination, final long source, final ChannelBuffer payload) {

		Preconditions.checkNotNull(cmdType, "cmdType is null");
		Preconditions.checkNotNull(destination, "destination is null");
		Preconditions.checkNotNull(source, "source is null");
		// payload is allowed to be null in case somebody wants to send empty packets

		this.cmdType = cmdType;
		this.destination = destination;
		this.source = source;
		this.payload = payload;

	}

	public byte getCmdType() {
		return cmdType;
	}

	public long getDestination() {
		return destination;
	}

	public long getSource() {
		return source;
	}

	public ChannelBuffer getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return "RUPPacketImpl[" +
				"cmdType=" + cmdType +
				",destination=" + destination +
				",source=" + source +
				",payload=" + StringUtils.toHexString(payload) +
				']';
	}
}
