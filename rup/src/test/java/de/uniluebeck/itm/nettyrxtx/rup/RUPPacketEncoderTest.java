package de.uniluebeck.itm.nettyrxtx.rup;


import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingEncoderFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class RUPPacketEncoderTest {

	private EncoderEmbedder<RUPPacketFragment> encoder;

	@Before
	public void setUp() {
		encoder = new EncoderEmbedder<RUPPacketFragment>(
				new RUPPacketEncoder(19 + 10 /* header + payload */, new DleStxEtxFramingEncoderFactory())
		);
	}

	@After
	public void tearDown() {
		encoder = null;
	}

	@Test
	public void testEncoderUnfragmentedSinglePacket() {

		RUPPacket packet = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x1234,
				0x4321,
				ChannelBuffers.wrappedBuffer("helloo".getBytes())
		);

		// encode the packet
		encoder.offer(packet);
		RUPPacketFragment decodedFragment = encoder.poll();

		// check if one and only one packet has come out from the encoding process
		assertNotNull(decodedFragment);
		assertNull(encoder.poll());

		assertPacketCorrect(0x1234, 0x4321, constructPayload(true, true, "helloo"), decodedFragment);

	}

	@Test
	public void testEncoderFragmentedSinglePacket() {

		RUPPacket packet = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x1234,
				0x4321,
				ChannelBuffers.wrappedBuffer("12345678901234567890".getBytes())
		);

		// encode the packet
		encoder.offer(packet);

		RUPPacketFragment encodedFragment1 = encoder.poll();
		RUPPacketFragment encodedFragment2 = encoder.poll();
		RUPPacketFragment encodedFragment3 = encoder.poll();

		// check if three and only three packets have come out from the encoding process
		assertNotNull(encodedFragment1);
		assertNotNull(encodedFragment2);
		assertNotNull(encodedFragment3);
		assertNull(encoder.poll());

		assertPacketCorrect(0x1234, 0x4321, constructPayload(true, false, "12345678"), encodedFragment1);
		assertPacketCorrect(0x1234, 0x4321, constructPayload(false, false, "9012345678"), encodedFragment2);
		assertPacketCorrect(0x1234, 0x4321, constructPayload(false, true, "90"), encodedFragment3);
	}

	@Test
	public void testEncoderUnfragmentedMultiPacket() {

		RUPPacket packet1 = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x1234,
				0x4321,
				ChannelBuffers.wrappedBuffer("helloo".getBytes())
		);
		RUPPacket packet2 = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x2345,
				0x5432,
				ChannelBuffers.wrappedBuffer("oolleh".getBytes())
		);

		// encode the packet1
		encoder.offer(packet1);
		encoder.offer(packet2);

		RUPPacketFragment encodedFragment1 = encoder.poll();
		RUPPacketFragment encodedFragment2 = encoder.poll();

		// check if one and only one packet1 has come out from the encoding process
		assertNotNull(encodedFragment1);
		assertNotNull(encodedFragment2);
		assertNull(encoder.poll());

		assertPacketCorrect(0x1234, 0x4321, constructPayload(true, true, "helloo"), encodedFragment1);
		assertPacketCorrect(0x2345, 0x5432, constructPayload(true, true, "oolleh"), encodedFragment2);

	}

	@Test
	public void testEncoderFragmentedMultiPacket() {

		RUPPacket packet1 = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x1234,
				0x4321,
				ChannelBuffers.wrappedBuffer("12345678901234567890".getBytes())
		);
		RUPPacket packet2 = new RUPPacketImpl(
				RUPPacket.Type.MESSAGE,
				0x2345,
				0x5432,
				ChannelBuffers.wrappedBuffer("09876543210987654321".getBytes())
		);

		// encode the packet1
		encoder.offer(packet1);
		encoder.offer(packet2);

		RUPPacketFragment encodedFragment11 = encoder.poll();
		RUPPacketFragment encodedFragment12 = encoder.poll();
		RUPPacketFragment encodedFragment13 = encoder.poll();
		RUPPacketFragment encodedFragment21 = encoder.poll();
		RUPPacketFragment encodedFragment22 = encoder.poll();
		RUPPacketFragment encodedFragment23 = encoder.poll();

		// check if three and only three fragment per packets have come out from the encoding process
		assertNotNull(encodedFragment11);
		assertNotNull(encodedFragment12);
		assertNotNull(encodedFragment13);
		assertNotNull(encodedFragment21);
		assertNotNull(encodedFragment22);
		assertNotNull(encodedFragment23);
		assertNull(encoder.poll());

		assertPacketCorrect(0x1234, 0x4321, constructPayload(true, false, "12345678"), encodedFragment11);
		assertPacketCorrect(0x1234, 0x4321, constructPayload(false, false, "9012345678"), encodedFragment12);
		assertPacketCorrect(0x1234, 0x4321, constructPayload(false, true, "90"), encodedFragment13);

		assertPacketCorrect(0x2345, 0x5432, constructPayload(true, false, "09876543"), encodedFragment21);
		assertPacketCorrect(0x2345, 0x5432, constructPayload(false, false, "2109876543"), encodedFragment22);
		assertPacketCorrect(0x2345, 0x5432, constructPayload(false, true, "21"), encodedFragment23);
	}

	private void assertPacketCorrect(long destination, long source, byte[] payload, RUPPacketFragment encodedFragment) {

		assertEquals(destination, encodedFragment.getDestination());
		assertEquals(source, encodedFragment.getSource());

		ChannelBuffer encodedPayload = encodedFragment.getPayload();
		byte[] encodedPayloadBytes = new byte[encodedPayload.readableBytes()];
		encodedPayload.readBytes(encodedPayloadBytes);

		assertArrayEquals(payload, encodedPayloadBytes);
	}

	private byte[] constructPayload(boolean dlestx, boolean dleetx, String payload) {
		ByteBuffer payloadBytes = ByteBuffer.allocate((dlestx ? 2 : 0) + payload.getBytes().length + (dleetx ? 2 : 0));
		if (dlestx) {
			payloadBytes.put(DleStxEtxConstants.DLE_STX);
		}
		payloadBytes.put(payload.getBytes());
		if (dleetx) {
			payloadBytes.put(DleStxEtxConstants.DLE_ETX);
		}
		return payloadBytes.array();
	}

}
