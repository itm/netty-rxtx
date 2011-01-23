package de.uniluebeck.itm.nettyrxtx.rup;


import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Decoder for the Remote UART Protocol (RUP) that can decode RUP packet fragments out of either a ChannelBuffer or an
 * ISensePacket. Also, this decoder makes sure the packet order is correct according to the sequence number for a given
 * window of number of packets and/or time.
 *
 * // TODO implement time based window
 */
public class RUPPacketFragmentDecoder extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(RUPPacketFragmentDecoder.class);

	private static class PacketBuffer {

		private Map<Integer, RUPPacket> packets = Maps.newTreeMap();

		private int windowSize = 10;

		private int windowOffset = 0;

		public boolean veryFirstPacket = true;

		/**
		 * Checks if a sequenceNumber is inside the acceptance window.
		 *
		 * @param sequenceNumber the sequenceNumber of the packet received
		 *
		 * @return {@code true} if sequenceNumber lies inside the window, {@code false} otherwise
		 */
		private boolean isInWindow(int sequenceNumber) {

			// if window overlaps number overflow at 255 check if sequenceNumber either lies in between windowOffset and 255
			// or in between zero and ((windowOffset+windowSize)%255)
			if (windowOffset + windowSize > 255) {

				return sequenceNumber > windowOffset || (sequenceNumber > 0 && sequenceNumber < ((windowOffset + windowSize) % 255));
			}

			// check if sequenceNumber lies in between the non-overlapping window
			return sequenceNumber >= windowOffset && sequenceNumber < (windowOffset + windowSize);
		}
	}

	private Map<Long, PacketBuffer> packetBuffers = Maps.newHashMap();

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		ChannelBuffer payload;

		// extract payload out of either a ChannelBuffer or an ISensePacket
		if (e.getMessage() instanceof ISensePacket) {

			final ISensePacket iSensePacket = (ISensePacket) e.getMessage();

			// only do something if the packet is a Remote UART packet, otherwise send it upstream
			if (!RUPPacketHelper.isRemoteUARTPacket(iSensePacket)) {
				ctx.sendUpstream(e);
				return;
			}

			payload = iSensePacket.getPayload();

		} else if (e.getMessage() instanceof ChannelBuffer) {

			payload = (ChannelBuffer) e.getMessage();

		} else {

			throw new IllegalArgumentException(
					"This decoder only encodes from either a ChannelBuffer or an ISensePacket"
			);
		}

		final RUPPacketFragment fragment = RUPPacketFragmentFactory.wrap(payload);

		final int sequenceNumber = fragment.getSequenceNumber();
		final long source = fragment.getSource();

		// get the packetBuffer for the sender of fragment
		PacketBuffer packetBuffer = packetBuffers.get(source);
		if (packetBuffer == null) {
			packetBuffer = new PacketBuffer();
			packetBuffers.put(source, packetBuffer);
		}

		if (packetBuffer.veryFirstPacket) {
			packetBuffer.veryFirstPacket = false;
			packetBuffer.windowOffset = sequenceNumber;
		}

		// only accept packets that are within window bounds
		if (packetBuffer.isInWindow(sequenceNumber)) {

			if (log.isTraceEnabled()) {
				log.trace(
						"[{}] Received packet with sequenceNumber in window ({} -> {}): {}", new Object[]{
								ctx.getName(),
								packetBuffer.windowOffset,
								((packetBuffer.windowOffset + packetBuffer.windowSize) % 255),
								fragment
						}
				);
			}

			packetBuffer.packets.put(sequenceNumber, fragment);
			sendUpstreamIfBuffered(packetBuffer, ctx);

		}

		// discard packets outside window bounds
		else {
			if (log.isTraceEnabled()) {
				log.trace("[{}] Ignored packet outside of packetBuffer window ({} -> {}): {}", new Object[]{
						ctx.getName(),
						packetBuffer.windowOffset,
						((packetBuffer.windowOffset + packetBuffer.windowSize) % 255),
						fragment
				}
				);
			}
		}

	}

	private void sendUpstreamIfBuffered(final PacketBuffer packetBuffer, final ChannelHandlerContext ctx) {

		// if there's no packet at currents offset it might mean we should possibly wait for it to arrive before sending
		// packages upstream
		int currentOffset = packetBuffer.windowOffset;
		while (packetBuffer.packets.containsKey(currentOffset)) {

			RUPPacket rupPacketFragment = packetBuffer.packets.remove(currentOffset);

			// send packet upstream
			if (log.isTraceEnabled()) {
				log.trace("[{}] Sending packet upstream: {}", ctx.getName(), rupPacketFragment);
			}
			ctx.sendUpstream(
					new UpstreamMessageEvent(ctx.getChannel(), rupPacketFragment,
							ctx.getChannel().getRemoteAddress()
					)
			);


			// move windowOffset
			packetBuffer.windowOffset = (++packetBuffer.windowOffset) % 255;
			currentOffset = packetBuffer.windowOffset;

		}
	}


}
