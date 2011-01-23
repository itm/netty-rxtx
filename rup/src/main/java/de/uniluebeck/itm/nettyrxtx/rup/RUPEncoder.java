package de.uniluebeck.itm.nettyrxtx.rup;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.ChannelDownstreamHandlerFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RUPEncoder extends SimpleChannelDownstreamHandler {

	private static class Fragmenter {

		/**
		 * An encoder chain that may e.g. wrap the payload of a packet to encode with e.g. DLE STX ... DLE ETX.
		 */
		private final EncoderEmbedder<ChannelBuffer> encoder;

		/**
		 * The maximum size of a fragment payload (i.e. excluding headers).
		 */
		private final int maximumFragmentPayloadSize;

		/**
		 * The last sequence number that was produced by this fragmenter.
		 */
		private byte lastSequenceNumber = 0;

		public Fragmenter(final int maximumFragmentPayloadSize, final ChannelDownstreamHandler[] channelDownstreamHandlers) {

			this.maximumFragmentPayloadSize = maximumFragmentPayloadSize;
			this.encoder = new EncoderEmbedder<ChannelBuffer>(channelDownstreamHandlers);
		}

		/**
		 * Fragments {@code packet} into fragments (i.e. packets of type {@link RUPPacketFragment}.
		 *
		 * @param packet the packet to fragment
		 * @return a {@link List} of fragments
		 */
		public List<RUPPacketFragment> fragment(RUPPacket packet) {

			encoder.offer(packet.getPayload());
			final ChannelBuffer encodedPayload = encoder.poll();


			int payloadBytesRemaining = encodedPayload.readableBytes();
			int payloadBytesWritten = 0;

			ArrayList<RUPPacketFragment> fragments = Lists.newArrayList();

			while (payloadBytesRemaining > 0) {

				int bytesToWrite = payloadBytesRemaining < maximumFragmentPayloadSize ? payloadBytesRemaining : maximumFragmentPayloadSize;

				fragments.add(RUPFragmentFactory.create(
						packet.getCmdType(),
						getNextSequenceNumber(),
						packet.getDestination(),
						packet.getSource(),
						encodedPayload.slice(payloadBytesWritten, bytesToWrite)
				));

				payloadBytesWritten += bytesToWrite;
				payloadBytesRemaining -= bytesToWrite;
			}

			return fragments;


		}

		private byte getNextSequenceNumber() {
			lastSequenceNumber = (byte) ((++lastSequenceNumber) % 255);
			return lastSequenceNumber;
		}

	}

	/**
	 * The maximum size of a fragment, including headers.
	 */
	private int maximumFragmentSize;

	/**
	 * A set of factories that are called to create ChannelDownstreamHandler instances upon a write request of a new
	 * sender.
	 */
	private final ChannelDownstreamHandlerFactory[] channelDownstreamHandlerFactories;

	private Map<Long, Fragmenter> fragmenters = Maps.newHashMap();

	/**
	 * Constructs a new RUPFragmentEncoder with a maximum fragment size (including 19 bytes RUP packet headers) of
	 * {@code maximumFragmentSize}. The maximum fragment size is depending on the actual protocol stack that is used
	 * for the current application, therefore it cannot be statically defined but differs from application to
	 * application.
	 * <p/>
	 * <i>Please note:</i> Another 19 bytes will be used for RUP headers and another 4 bytes will be used for
	 * DLE STX ... ETX encoding that frames the fragmented payload.
	 *
	 * @param maximumFragmentSize the maximum fragment size (including 19 bytes of packet headers)
	 * @param channelDownstreamHandlerFactories a set of factories that create e.g. encoders to encode a packets payload
	 *
	 */
	public RUPEncoder(int maximumFragmentSize, ChannelDownstreamHandlerFactory... channelDownstreamHandlerFactories) {
		this.maximumFragmentSize = maximumFragmentSize;
		this.channelDownstreamHandlerFactories = channelDownstreamHandlerFactories;
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		RUPPacket packet = (RUPPacket) e.getMessage();

		// only messages need fragmentation, set sink, sink requests and sink responses don't
		if (packet.getCmdType() != RUPPacket.Type.MESSAGE.getValue()) {
			ctx.sendDownstream(e);
			return;
		}

		for (RUPPacketFragment fragment : getFragmenter(packet).fragment(packet)) {
			ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), e.getFuture(), fragment, e.getRemoteAddress()));
		}

	}

	private Fragmenter getFragmenter(RUPPacket packet) {
		long source = packet.getSource();
		Fragmenter fragmenter = fragmenters.get(source);
		if (fragmenter == null) {
			fragmenter = new Fragmenter(maximumFragmentSize - 19, createChannelDownstreamHandlers());
			fragmenters.put(source, fragmenter);
		}
		return fragmenter;
	}

	private ChannelDownstreamHandler[] createChannelDownstreamHandlers() {

		ChannelDownstreamHandler[] channelDownstreamHandlers =
				new ChannelDownstreamHandler[channelDownstreamHandlerFactories.length];

		for (int i = 0; i < channelDownstreamHandlerFactories.length; i++) {
			channelDownstreamHandlers[i] = channelDownstreamHandlerFactories[i].create();
		}

		return channelDownstreamHandlers;
	}
}
