package de.uniluebeck.itm.nettyrxtx.rup;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RUPFragmentDecoderTest extends RUPDecoderTestBase {

	private DecoderEmbedder<RUPPacketFragment> decoder;

	@Before
	public void setUp() {
		super.setUp();
		decoder = new DecoderEmbedder<RUPPacketFragment>(
				new RUPFragmentDecoder()
		);
	}

	@After
	public void tearDown() {
		decoder = null;
		super.tearDown();
	}

	/**
	 * Tests if one packet that is small enough to not be fragmented is decoded correctly.
	 */
	@Test
	public void testOnePacketFragment() {

		// randomly generate packet values and remember them
		byte randomSequenceNumber = getRandomSequenceNumber(0x4321);
		ChannelBuffer fragment = createMessageFragment(randomSequenceNumber, 0x1234, 0x4321, "hello, world");

		// decode the packet
		decoder.offer(fragment);

		RUPPacketFragment decodedPacketFragment = decoder.poll();

		// at least one packet must have been decoded
		assertNotNull(decodedPacketFragment);

		// only one packet should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(randomSequenceNumber, 0x1234, 0x4321, "hello, world", decodedPacketFragment);
	}

	/**
	 * Tests is several packets that are small enough to not be fragmented and are sent and received in the correct order
	 * are decoded correctly.
	 */
	@Test
	public void testOrderedMultiplePacketFragments() {

		// randomly generate packet values and remember them
		byte sequenceNumber1 = getRandomSequenceNumber(0x4321);
		byte sequenceNumber2 = getSubsequentSequenceNumber(0x4321);
		byte sequenceNumber3 = getSubsequentSequenceNumber(0x4321);

		ChannelBuffer fragment1 = createMessageFragment(sequenceNumber1, 0x1234, 0x4321, "hello, world");
		ChannelBuffer fragment2 = createMessageFragment(sequenceNumber2, 0x1234, 0x4321, "werld, hollo");
		ChannelBuffer fragment3 = createMessageFragment(sequenceNumber3, 0x1234, 0x4321, "world, hello");

		// decode the packets
		decoder.offer(fragment1);
		decoder.offer(fragment2);
		decoder.offer(fragment3);

		RUPPacketFragment decodedFragment1 = decoder.poll();
		RUPPacketFragment decodedFragment2 = decoder.poll();
		RUPPacketFragment decodedFragment3 = decoder.poll();

		// at least three packets must have been decoded
		assertNotNull(decodedFragment1);
		assertNotNull(decodedFragment2);
		assertNotNull(decodedFragment3);

		// only three packets should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(sequenceNumber1, 0x1234, 0x4321, "hello, world", decodedFragment1);
		assertPacketCorrect(sequenceNumber2, 0x1234, 0x4321, "werld, hollo", decodedFragment2);
		assertPacketCorrect(sequenceNumber3, 0x1234, 0x4321, "world, hello", decodedFragment3);
	}

	/**
	 * Tests is several packets that are small enough to not be fragmented and are sent and received in an incorrect order
	 * are decoded correctly.
	 */
	@Test
	public void testUnorderedMultiplePacketFragments() {

		// randomly generate packet values and remember them
		byte sequenceNumber1 = getRandomSequenceNumber(0x4321);
		byte sequenceNumber2 = getSubsequentSequenceNumber(0x4321);
		byte sequenceNumber3 = getSubsequentSequenceNumber(0x4321);

		ChannelBuffer fragment1 = createMessageFragment(sequenceNumber1, 0x1234, 0x4321, "hello, world");
		ChannelBuffer fragment2 = createMessageFragment(sequenceNumber2, 0x1234, 0x4321, "werld, hollo");
		ChannelBuffer fragment3 = createMessageFragment(sequenceNumber3, 0x1234, 0x4321, "world, hello");

		// decode the packets in WRONG order (!!!)
		decoder.offer(fragment1);
		decoder.offer(fragment3);
		decoder.offer(fragment2);

		RUPPacketFragment decodedFragment1 = decoder.poll();
		RUPPacketFragment decodedFragment2 = decoder.poll();
		RUPPacketFragment decodedFragment3 = decoder.poll();

		// at least three packets must have been decoded
		assertNotNull(decodedFragment1);
		assertNotNull(decodedFragment2);
		assertNotNull(decodedFragment3);

		// only three packets should have been decoded
		assertNull(decoder.poll());

		// check that packets are correct and IN ORDER
		assertPacketCorrect(sequenceNumber1, 0x1234, 0x4321, "hello, world", decodedFragment1);
		assertPacketCorrect(sequenceNumber2, 0x1234, 0x4321, "werld, hollo", decodedFragment2);
		assertPacketCorrect(sequenceNumber3, 0x1234, 0x4321, "world, hello", decodedFragment3);

	}

	/**
	 * Tests if a packet with an empty payload is successfully decoded.
	 */
	@Test
	public void testEmptyPacketFragment() {
		byte sequenceNumber = getRandomSequenceNumber(0x4321);
		decoder.offer(createMessageFragment(sequenceNumber, 0x1234, 0x4321, ""));
		RUPPacketFragment decodedFragment = decoder.poll();
		assertNotNull(decodedFragment);
		assertPacketCorrect(sequenceNumber, 0x1234, 0x4321, "", decodedFragment);
	}

	/**
	 * Tests if one unfragmented packet for each of multiple senders are decoded correctly.
	 */
	@Test
	public void testUnfragmentedOnePacketMultipleSenders() {

		// randomly generate packet values and remember them
		byte sequenceNumber1 = getRandomSequenceNumber(0x4321);
		byte sequenceNumber2 = getRandomSequenceNumber(0x5432);
		byte sequenceNumber3 = getRandomSequenceNumber(0x6543);

		ChannelBuffer fragment1 = createMessageFragment(sequenceNumber1, 0x1234, 0x4321, "hello, world");
		ChannelBuffer fragment2 = createMessageFragment(sequenceNumber2, 0x2345, 0x5432, "werld, hollo");
		ChannelBuffer fragment3 = createMessageFragment(sequenceNumber3, 0x3456, 0x6543, "world, hello");

		// decode the packets
		decoder.offer(fragment1);
		decoder.offer(fragment2);
		decoder.offer(fragment3);

		RUPPacketFragment decodedFragment1 = decoder.poll();
		RUPPacketFragment decodedFragment2 = decoder.poll();
		RUPPacketFragment decodedFragment3 = decoder.poll();

		// at least three packets must have been decoded
		assertNotNull(decodedFragment1);
		assertNotNull(decodedFragment2);
		assertNotNull(decodedFragment3);

		// only three packets should have been decoded
		assertNull(decoder.poll());

		// check that packets for correctness
		assertPacketCorrect(sequenceNumber1, 0x1234, 0x4321, "hello, world", decodedFragment1);
		assertPacketCorrect(sequenceNumber2, 0x2345, 0x5432, "werld, hollo", decodedFragment2);
		assertPacketCorrect(sequenceNumber3, 0x3456, 0x6543, "world, hello", decodedFragment3);
	}

	/**
	 * Tests if multiple unfragmented packets for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testUnfragmentedMultiplePacketsMultipleSenders() {

		// generate four packets, two of each of two senders
		byte sequenceNumber11 = getRandomSequenceNumber(0x4321);
		byte sequenceNumber21 = getRandomSequenceNumber(0x5432);
		byte sequenceNumber12 = getSubsequentSequenceNumber(0x4321);
		byte sequenceNumber22 = getSubsequentSequenceNumber(0x5432);

		ChannelBuffer fragment11 = createMessageFragment(sequenceNumber11, 0x1234, 0x4321, "hello, world");
		ChannelBuffer fragment21 = createMessageFragment(sequenceNumber21, 0x2345, 0x5432, "werld, hollo");
		ChannelBuffer fragment12 = createMessageFragment(sequenceNumber12, 0x1234, 0x4321, "hello, world2");
		ChannelBuffer fragment22 = createMessageFragment(sequenceNumber22, 0x2345, 0x5432, "werld, hollo2");

		// decode the packets
		decoder.offer(fragment11);
		decoder.offer(fragment21);
		decoder.offer(fragment12);
		decoder.offer(fragment22);

		RUPPacketFragment decodedFragment11 = decoder.poll();
		RUPPacketFragment decodedFragment21 = decoder.poll();
		RUPPacketFragment decodedFragment12 = decoder.poll();
		RUPPacketFragment decodedFragment22 = decoder.poll();

		// at least four packets must have been decoded
		assertNotNull(decodedFragment11);
		assertNotNull(decodedFragment21);
		assertNotNull(decodedFragment12);
		assertNotNull(decodedFragment22);

		// only four packets should have been decoded
		assertNull(decoder.poll());

		// check that packets for correctness
		assertPacketCorrect(sequenceNumber11, 0x1234, 0x4321, "hello, world", decodedFragment11);
		assertPacketCorrect(sequenceNumber21, 0x2345, 0x5432, "werld, hollo", decodedFragment21);
		assertPacketCorrect(sequenceNumber12, 0x1234, 0x4321, "hello, world2", decodedFragment12);
		assertPacketCorrect(sequenceNumber22, 0x2345, 0x5432, "werld, hollo2", decodedFragment22);
	}

	private void assertPacketCorrect(byte expectedSequenceNumber, long expectedDestination, long expectedSource,
									 String expectedPayload, RUPPacketFragment decodedPacket) {

		// compare expected headers with decoded headers
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(expectedSequenceNumber == decodedPacket.getSequenceNumber());
		assertTrue(expectedDestination == decodedPacket.getDestination());
		assertTrue(expectedSource == decodedPacket.getSource());

		// compare expected payload with decoded payload
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals(expectedPayload.getBytes(), decodedPayload);

	}

}
