package de.uniluebeck.itm.nettyrxtx.rup;

import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoderFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;


public class RUPPacketDecoderTest {

	private DecoderEmbedder<RUPPacket> decoder;

	private Random random;

	private Map<Long, Byte> lastSequenceNumbers = Maps.newHashMap();

	@Before
	public void setUp() {
		decoder = new DecoderEmbedder<RUPPacket>(
				new RUPPacketFragmentDecoder(),
				new RUPPacketDecoder(
						new Tuple<ChannelUpstreamHandlerFactory, Object>(new DleStxEtxFramingDecoderFactory(), null)
				)
		);
		random = new Random();
	}

	@After
	public void tearDown() {
		decoder = null;
		random = null;
	}

	/**
	 * Tests if the decoder correctly recombines fragments that contain one packet that is framed by DLE STX ... DLE ETX.
	 */
	@Test
	public void testFragmentedOnePacketDleStxEtx() {

		// decode the packet
		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world"));

		RUPPacket decodedPacket = decoder.poll();

		// at least one packet must have been decoded
		assertNotNull(decodedPacket);

		// only one packet should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(0x1234, 0x4321, "hello, world", decodedPacket);

	}

	/**
	 * Tests if the decoder correctly recombines fragments of multiple packets containing data which is framed by DLE STX
	 * ... DLE ETX.
	 */
	@Test
	public void testFragmentedMultiplePacketsDleStxEtx() {

		// decode first packet
		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world"));
		RUPPacket decodedPacket1 = decoder.poll();

		// decode second packet
		decoder.offer(createOpeningMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world2"));
		RUPPacket decodedPacket2 = decoder.poll();

		// at least two packet must have been decoded
		assertNotNull(decodedPacket1);
		assertNotNull(decodedPacket2);

		// only two packets should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(0x1234, 0x4321, "hello, world", decodedPacket1);
		assertPacketCorrect(0x1234, 0x4321, "hello, world2", decodedPacket2);

	}

	/**
	 * Tests if a packet with an empty payload is successfully discarded (no dle stx ... etx).
	 */
	@Test
	public void testEmptyPacketWithoutFraming() {

		decoder.offer(createMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, ""));

		RUPPacket decodedPacket = decoder.poll();

		assertNull(decodedPacket);

	}

	/**
	 * Tests if a packet with an empty payload is successfully decoded (with dle stx ... etx).
	 */
	@Test
	public void testEmptyPacketWithFraming() {

		decoder.offer(createOpeningAndClosingMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, ""));

		RUPPacket decodedPacket = decoder.poll();

		// at least one packet must have been decoded
		assertNotNull(decodedPacket);

		// only one packet should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(0x1234, 0x4321, "", decodedPacket);
	}

	/**
	 * Tests if one fragmented packet for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedOnePacketMultipleSenders() {

		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x2345), 0x2345, 0x5432, "world"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world"));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, "hello"));

		RUPPacket decodedPacket1 = decoder.poll();
		RUPPacket decodedPacket2 = decoder.poll();

		// at least two packet must have been decoded
		assertNotNull(decodedPacket1);
		assertNotNull(decodedPacket2);

		// only two packets should have been decoded
		assertNull(decoder.poll());

		assertPacketCorrect(0x1234, 0x4321, "hello, world", decodedPacket1);
		assertPacketCorrect(0x2345, 0x5432, "world, hello", decodedPacket2);

	}

	/**
	 * Tests if multiple fragmented packets for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedMultiplePacketsMultipleSenders() {

		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createOpeningMessageFragment(getRandomSequenceNumber(0x2345), 0x2345, 0x5432, "world"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world"));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, "hello"));

		decoder.offer(createOpeningMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createOpeningMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, "world"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world2"));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x2345), 0x2345, 0x5432, "hello2"));

		RUPPacket decodedPacket11 = decoder.poll();
		RUPPacket decodedPacket12 = decoder.poll();
		RUPPacket decodedPacket21 = decoder.poll();
		RUPPacket decodedPacket22 = decoder.poll();

		// at least four packets must have been decoded
		assertNotNull(decodedPacket11);
		assertNotNull(decodedPacket12);
		assertNotNull(decodedPacket21);
		assertNotNull(decodedPacket22);

		// only four packets should have been decoded
		assertNull(decoder.poll());

		// compare the original header values with the decoded values
		assertPacketCorrect(0x1234, 0x4321, "hello, world", decodedPacket11);
		assertPacketCorrect(0x2345, 0x5432, "world, hello", decodedPacket12);
		assertPacketCorrect(0x1234, 0x4321, "hello, world2", decodedPacket21);
		assertPacketCorrect(0x2345, 0x5432, "world, hello2", decodedPacket22);

	}

	private void assertPacketCorrect(long expectedDestination, long expectedSource, String expectedPayload, RUPPacket decodedPacket) {

		// compare expected headers with decoded headers
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(expectedDestination == decodedPacket.getDestination());
		assertTrue(expectedSource == decodedPacket.getSource());

		// compare expected payload with decoded payload
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals(expectedPayload.getBytes(), decodedPayload);

	}

	private ChannelBuffer createOpeningAndClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + 2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	private ChannelBuffer createOpeningMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		return RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	private ChannelBuffer createMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		return RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, payload.getBytes()).getChannelBuffer();
	}

	private ChannelBuffer createClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	private byte getRandomSequenceNumber(long sender) {
		byte sequenceNumber = (byte) (0xFF & (random.nextInt(255) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}

	private byte getSubsequentSequenceNumber(long sender) {
		if (!lastSequenceNumbers.containsKey(sender)) {
			throw new IllegalArgumentException("No first sequence number existing!");
		}
		byte lastSequenceNumber = lastSequenceNumbers.get(sender);
		byte sequenceNumber = (byte) (0xFF & ((lastSequenceNumber + 1) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}

}
