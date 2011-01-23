package de.uniluebeck.itm.nettyrxtx.rup;


import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RUPPacketFragmentEncoderTest {

	private EncoderEmbedder<ISensePacket> encoder;

	@Before
	public void setUp() {
		encoder = new EncoderEmbedder<ISensePacket>(new RUPPacketFragmentEncoder());
	}

	@After
	public void tearDown() {
		encoder = null;
	}

	@Test
	public void testSingleFragment() {
		RUPPacketFragment fragment = RUPPacketFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 0,
				0x1234,
				0x4321,
				new byte[]{1, 2, 3}
		);
		encoder.offer(fragment);
		ISensePacket iSensePacket = encoder.poll();
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 0, 0x1234, 0x4321, new byte[] {1,2,3}, iSensePacket);
	}

	private void assertBufferCorrect(byte cmdType, byte sequenceNumber, long destination, long source, byte[] payload, ISensePacket iSensePacket) {
		assertEquals(ISensePacketType.PLOT.getValue(), iSensePacket.getType());
		ChannelBuffer encodedPayload = iSensePacket.getPayload();
		assertEquals(cmdType, encodedPayload.getByte(0));
		assertEquals(sequenceNumber, encodedPayload.getByte(1));
		assertEquals(destination, encodedPayload.getLong(2));
		assertEquals(source, encodedPayload.getLong(10));
		assertEquals(payload.length, encodedPayload.getByte(18));
		byte[] encodedPayloadBytes = new byte[encodedPayload.readableBytes()-19];
		encodedPayload.getBytes(19, encodedPayloadBytes);
		assertArrayEquals(payload, encodedPayloadBytes);
	}

	@Test
	public void testMultipleFragments() {
		RUPPacketFragment fragment1 = RUPPacketFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 0,
				0x1234,
				0x4321,
				new byte[]{1, 2, 3}
		);
		RUPPacketFragment fragment2 = RUPPacketFragmentFactory.create(
				RUPPacket.Type.MESSAGE,
				(byte) 1,
				0x2345,
				0x5432,
				new byte[]{3, 2, 1}
		);
		encoder.offer(fragment1);
		encoder.offer(fragment2);
		ISensePacket iSensePacket1 = encoder.poll();
		ISensePacket iSensePacket2 = encoder.poll();
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 0, 0x1234, 0x4321, new byte[] {1,2,3}, iSensePacket1);
		assertBufferCorrect(RUPPacket.Type.MESSAGE.getValue(), (byte) 1, 0x2345, 0x5432, new byte[] {3,2,1}, iSensePacket2);
	}

}
