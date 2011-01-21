package de.uniluebeck.itm.nettyrxtx.rup;

import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;

import java.util.Map;

/**
 * Decoder that decodes a series of RUPPacketFragment instances (fragments) into one {@link RUPPacketFragment} by
 * applying another decoder (e.g {@link de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoder} on the packets
 * payload. The result of the decoding is on {@link RUPPacketFragment} instance with a reassembled payload and the same
 * packet headers as the individual fragments (except of the sequenceNumber field).
 */
public class RUPPacketDecoder extends SimpleChannelUpstreamHandler {

	private static class Reassembler {

		private final DecoderEmbedder<ChannelBuffer> decoder;

		private final long source;

		private final long destination;

		public Reassembler(final long source, final long destination,
						   final ChannelUpstreamHandler[] channelUpstreamHandlers) {
			this.source = source;
			this.destination = destination;
			this.decoder = new DecoderEmbedder<ChannelBuffer>(channelUpstreamHandlers);
		}

		public RUPPacket[] receiveFragment(final RUPPacket fragmentedRupPacketFragment) {

			// let the decoder try to reassemble the package
			decoder.offer(fragmentedRupPacketFragment);

			// check if one or more packages have been reassembled
			Object[] decodedPayloads = decoder.pollAll();
			RUPPacket[] decodedRUPPackets = new RUPPacket[decodedPayloads.length];

			// return all reassembled packages
			for (int i = 0; i < decodedPayloads.length; i++) {

				ChannelBuffer decodedPayloadBuffer = (ChannelBuffer) decodedPayloads[i];
				decodedRUPPackets[i] = new RUPPacketImpl(
						RUPPacket.Type.MESSAGE,
						destination,
						source,
						decodedPayloadBuffer
				);
			}

			return decodedRUPPackets;
		}
	}

	/**
	 * A set of factories that are called to create ChannelUpstreamHandler instances upon creation of a new Reassembler
	 * instance.
	 */
	private final Tuple<ChannelUpstreamHandlerFactory, Object>[] channelUpstreamHandlerFactories;

	/**
	 * Map that holds a Reassembler instance for every source address of RUPPacketFragment instances received.
	 */
	private final Map<Long, Reassembler> reassemblersMap = Maps.newHashMap();

	/**
	 * Constructs a new RUPPacketDecoder instance that uses a {@link DecoderEmbedder} that wraps decoders created by the
	 * {@code channelUpstreamHandlerFactories} to reassemble a RUPPacketFragment (type {@link RUPPacket.Type#MESSAGE})
	 * instance from a series of RUPPacketFragment fragments. For each RUP endpoint one {@link DecoderEmbedder} instance
	 * that uses the decoders created by {@code channelUpstreamHandlers}.
	 *
	 * @param channelUpstreamHandlerFactories
	 *         the factories for creating handlers for reassembling the packet from a series of packet fragments
	 */
	public RUPPacketDecoder(final Tuple<ChannelUpstreamHandlerFactory, Object>... channelUpstreamHandlerFactories) {
		this.channelUpstreamHandlerFactories = channelUpstreamHandlerFactories;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		RUPPacket fragmentedRupPacketFragment = (RUPPacket) e.getMessage();

		// only reassembly RUP message packets, other types don't need reassembly
		if (RUPPacket.Type.MESSAGE.getValue() != fragmentedRupPacketFragment.getCmdType()) {
			ctx.sendUpstream(e);
			return;
		}

		Reassembler reassembler = getReassembler(fragmentedRupPacketFragment);
		RUPPacket[] reassembledPackets = reassembler.receiveFragment(fragmentedRupPacketFragment);

		for (RUPPacket reassembledPacket : reassembledPackets) {
			Channels.fireMessageReceived(ctx, reassembledPacket);
		}

	}

	/**
	 * Returns or constructs the Reassembler responsible for packets from the source node of {@code fragmentedRupPacket}.
	 *
	 * @param fragmentedRupPacketFragment the packet for which to get the reassembler
	 *
	 * @return the responsible Reassembler instance
	 */
	private Reassembler getReassembler(final RUPPacket fragmentedRupPacketFragment) {

		long source = fragmentedRupPacketFragment.getSource();
		long destination = fragmentedRupPacketFragment.getDestination();
		Reassembler reassembler = reassemblersMap.get(source);

		// construct new assembler if this packet is the first received from source
		if (reassembler == null) {
			reassembler = new Reassembler(source, destination, createChannelUpstreamHandlers());
			reassemblersMap.put(source, reassembler);
		}

		return reassembler;
	}

	private ChannelUpstreamHandler[] createChannelUpstreamHandlers() {

		ChannelUpstreamHandler[] channelUpstreamHandlers =
				new ChannelUpstreamHandler[channelUpstreamHandlerFactories.length];

		for (int i = 0; i < channelUpstreamHandlerFactories.length; i++) {
			ChannelUpstreamHandlerFactory factory = channelUpstreamHandlerFactories[i].getFirst();
			Object parameters = channelUpstreamHandlerFactories[i].getSecond();
			channelUpstreamHandlers[i] = factory.create(parameters);
		}

		return channelUpstreamHandlers;
	}
}
