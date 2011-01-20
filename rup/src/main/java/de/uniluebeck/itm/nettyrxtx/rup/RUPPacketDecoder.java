package de.uniluebeck.itm.nettyrxtx.rup;


import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;

public class RUPPacketDecoder extends SimpleChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(RUPPacketDecoder.class);

	private static class PacketBuffer {

		private Map<Byte, RUPPacket> packets = Maps.newTreeMap();

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

		final ISensePacket iSensePacket = (ISensePacket) e.getMessage();

		// only do something if the packet is a Remote UART packet, otherwise send it upstream
		if (!RUPPacketHelper.isRemoteUARTPacket(iSensePacket)) {
			ctx.sendUpstream(e);
			return;
		}

		final RUPPacket rupPacket = RUPPacketFactory.wrap(iSensePacket.getPayload());

		final byte sequenceNumber = rupPacket.getSequenceNumber();
		final long source = rupPacket.getSource();

		// get the packetBuffer for the sender of rupPacket
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
								rupPacket
						}
				);
			}

			packetBuffer.packets.put(sequenceNumber, rupPacket);
			sendUpstreamIfBuffered(packetBuffer, ctx);

		}

		// discard packets outside window bounds
		else {
			if (log.isTraceEnabled()) {
				log.trace("[{}] Ignored packet outside of packetBuffer window ({} -> {}): {}", new Object[]{
						ctx.getName(),
						packetBuffer.windowOffset,
						((packetBuffer.windowOffset + packetBuffer.windowSize) % 255),
						rupPacket
				}
				);
			}
		}

	}

	private void sendUpstreamIfBuffered(final PacketBuffer packetBuffer, final ChannelHandlerContext ctx) {

		// if there's no packet at currents offset it might mean we should possibly wait for it to arrive before sending
		// packages upstream
		Byte currentOffset = Byte.valueOf((byte) (0xFF & packetBuffer.windowOffset));
		while (packetBuffer.packets.containsKey(currentOffset)) {

			RUPPacket rupPacket = packetBuffer.packets.remove(currentOffset);

			// send packet upstream
			if (log.isTraceEnabled()) {
				log.trace("[{}] Sending packet upstream: {}", ctx.getName(), rupPacket);
			}
			ctx.sendUpstream(
					new UpstreamMessageEvent(ctx.getChannel(), rupPacket,
							ctx.getChannel().getRemoteAddress()
					)
			);


			// move windowOffset
			packetBuffer.windowOffset = (++packetBuffer.windowOffset) % 255;
			currentOffset = Byte.valueOf((byte) (0xFF & packetBuffer.windowOffset));

		}
	}


}
