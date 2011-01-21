package de.uniluebeck.itm.nettyrxtx.rup;


import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RUPPacketFragmentDecoderTest {

	private DecoderEmbedder<RUPPacketFragment> decoder;

	@Before
	public void setUp() {
		decoder = new DecoderEmbedder<RUPPacketFragment>(
				new RUPPacketFragmentDecoder()
		);
	}

	@After
	public void tearDown() {
		decoder = null;
	}

	/**
	 * Tests if one packet that is small enough to not be fragmented is decoded correctly.
	 */
	@Test
	public void testOnePacketFragment() {

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
	}

	/**
	 * Tests is several packets that are small enough to not be fragmented and are sent and received in the correct order
	 * are decoded correctly.
	 */
	@Test
	public void testOrderedMultiplePacketFragments() {

	}

	/**
	 * Tests is several packets that are small enough to not be fragmented and are sent and received in an incorrect order
	 * are decoded correctly.
	 */
	@Test
	public void testUnorderedMultiplePacketFragments() {

	}

	/**
	 * Tests if a packet with an empty payload is successfully decoded.
	 */
	@Test
	public void testEmptyPacketFragment() {

	}

	/**
	 * Tests if one unfragmented packet for each of multiple senders are decoded correctly.
	 */
	@Test
	public void testUnfragmentedOnePacketMultipleSenders() {

	}

	/**
	 * Tests if multiple unfragmented packets for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testUnfragmentedMultiplePacketsMultipleSenders() {

	}

}
