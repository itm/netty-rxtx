package de.uniluebeck.itm.nettyrxtx.rup;

import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoderFactory;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RUPPacketDecoderTest {

	private DecoderEmbedder<RUPPacket> decoder;

	@Before
	public void setUp() {
		decoder = new DecoderEmbedder<RUPPacket>(
				new RUPPacketFragmentDecoder(),
				new RUPPacketDecoder(
						new Tuple<ChannelUpstreamHandlerFactory, Object>(new DleStxEtxFramingDecoderFactory(), null)
				)
		);
	}

	@After
	public void tearDown() {

	}

	/**
	 * Tests if the decoder correctly recombines fragments that contain one packet that is framed by DLE STX ... DLE ETX.
	 */
	@Test
	public void testFragmentedOnePacketDleStxEtx() {

		/*
		// randomly generate packet values and remember them
		final PacketData packetData = PacketData.generateNewPacketFragment();
		final RUPPacketFragment packetFragment = RUPPacketFragmentFactory.create(
				packetData.cmdType,
				packetData.sequenceNumber,
				packetData.destination,
				packetData.source,
				packetData.payload
		);

		// decode the packet
		decoder.offer(packetFragment.getChannelBuffer());
		RUPPacketFragment decodedPacketFragment = decoder.poll();

		// at least one packet must have been decoded
		assertNotNull(decodedPacketFragment);

		// only one packet should have been generated
		assertNull(decoder.poll());

		// compare the original header values with the decoded values
		assertTrue(packetData.cmdType == decodedPacketFragment.getCmdType());
		assertTrue(packetData.sequenceNumber == decodedPacketFragment.getSequenceNumber());
		assertTrue(packetData.destination == decodedPacketFragment.getDestination());
		assertTrue(packetData.source == decodedPacketFragment.getSource());

		// compare the original payload with the decoded payload
		byte[] decodedPayload = new byte[decodedPacketFragment.getPayload().readableBytes()];
		decodedPacketFragment.getPayload().readBytes(decodedPayload);
		assertArrayEquals(packetData.payload, decodedPayload);
		*/

	}

	/**
	 * Tests if the decoder correctly recombines fragments of multiple packets containing data which is framed by DLE STX
	 * ... DLE ETX.
	 */
	@Test
	public void testFragmentedMultiplePacketsDleStxEtx() {

	}

	/**
	 * Tests if a packet with an empty payload is successfully decoded.
	 */
	@Test
	public void testEmptyPacket() {

	}

	/**
	 * Tests if one fragmented packet for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedOnePacketMultipleSenders() {

	}

	/**
	 * Tests if multiple fragmented packets for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedMultiplePacketsMultipleSenders() {

	}

}
