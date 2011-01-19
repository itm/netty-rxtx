package de.uniluebeck.itm.nettyrxtx.dlestxetx;


import de.uniluebeck.itm.nettyrxtx.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DleStxEtxFramingDecoder extends FrameDecoder {

	private static final Logger log = LoggerFactory.getLogger(DleStxEtxFramingDecoder.class);

	private boolean foundDLE;

	private boolean foundPacket;

	private ChannelBuffer packet;

	public DleStxEtxFramingDecoder() {
		resetPacketState();
	}

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final ChannelBuffer buffer)
			throws Exception {

		while (buffer.readable()) {

			byte c = buffer.readByte();

			// check if last character read was DLE
			if (foundDLE) {
				foundDLE = false;

				if (c == DleStxEtxConstants.STX && !foundPacket) {

					log.trace("STX received in DLE mode");
					foundPacket = true;

				} else if (c == DleStxEtxConstants.ETX && foundPacket) {

					// packet was completely received
					if (log.isTraceEnabled()) {
						log.trace("Packet decoding completed: {}", StringUtils.toHexString(packet.array(),
								packet.readerIndex(), packet.readableBytes()
						)
						);
					}
					ChannelBuffer packetRead = packet;
					resetPacketState();
					return packetRead;

				} else if (c == DleStxEtxConstants.DLE && foundPacket) {

					// Stuffed DLE found
					log.trace("Stuffed DLE received in DLE mode");
					packet.writeByte(DleStxEtxConstants.DLE);

				} else {

					if (log.isWarnEnabled()) {
						log.warn("Incomplete packet received: {}",
								StringUtils.toHexString(packet.array(), packet.readerIndex(), packet.readableBytes())
						);
					}
					resetPacketState();
				}

			} else {

				if (c == DleStxEtxConstants.DLE) {
					log.trace("Plain DLE received");
					foundDLE = true;
				} else if (foundPacket) {
					packet.writeByte(c);
				}

			}
		}

		return null;
	}

	private void resetPacketState() {
		foundDLE = false;
		foundPacket = false;
		packet = ChannelBuffers.dynamicBuffer(512);
	}


}
