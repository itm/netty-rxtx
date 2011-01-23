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

		// only one packet should have been generated
		assertNull(decoder.poll());

		// compare the original header values with the decoded values
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(0x1234 == decodedPacket.getDestination());
		assertTrue(0x4321 == decodedPacket.getSource());

		// compare the original payload with the decoded payload
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals("hello, world".getBytes(), decodedPayload);

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

		// decode second packet
		decoder.offer(createOpeningMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "hello"));
		decoder.offer(createMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, ", "));
		decoder.offer(createClosingMessageFragment(getSubsequentSequenceNumber(0x1234), 0x1234, 0x4321, "world2"));

		RUPPacket decodedPacket = decoder.poll();
		RUPPacket decodedPacket2 = decoder.poll();

		// at least two packet must have been decoded
		assertNotNull(decodedPacket);
		assertNotNull(decodedPacket2);

		// only two packets should have been generated
		assertNull(decoder.poll());

		// compare the original header values of the first packet with the decoded values
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(0x1234 == decodedPacket.getDestination());
		assertTrue(0x4321 == decodedPacket.getSource());

		// compare the original header values of the first packet with the decoded values
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket2.getCmdType());
		assertTrue(0x1234 == decodedPacket2.getDestination());
		assertTrue(0x4321 == decodedPacket2.getSource());

		// compare the original payloads with the decoded payloads
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals("hello, world".getBytes(), decodedPayload);

		byte[] decodedPayload2 = new byte[decodedPacket2.getPayload().readableBytes()];
		decodedPacket2.getPayload().readBytes(decodedPayload2);
		assertArrayEquals("hello, world2".getBytes(), decodedPayload2);
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

		// only one packet should have been generated
		assertNull(decoder.poll());

		// compare the original header values with the decoded values
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(0x1234 == decodedPacket.getDestination());
		assertTrue(0x4321 == decodedPacket.getSource());

		// compare the original payload with the decoded payload
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals("".getBytes(), decodedPayload);
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
