package de.uniluebeck.itm.nettyrxtx.dlestxetx;

import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DleStxEtxFramingDecoderTest {

	@Test
	public void testPacket() {

		final String payloadString = "hello, world";
		byte[] payload = payloadString.getBytes();

		ChannelBuffer encodedBuffer = ChannelBuffers.wrappedBuffer(
				DleStxEtxConstants.DLE_STX,
				payload,
				DleStxEtxConstants.DLE_ETX
		);

		testDecoding(encodedBuffer, payload);
	}

	@Test
	public void testRandomBytesBeforePacket() {

		final String payloadString = "hello, world";
		byte[] expectedPayload = payloadString.getBytes();

		ChannelBuffer encodedBuffer = ChannelBuffers.wrappedBuffer(
				"klasjd".getBytes(),
				DleStxEtxConstants.DLE_STX,
				expectedPayload,
				DleStxEtxConstants.DLE_ETX
		);

		testDecoding(encodedBuffer, expectedPayload);
	}

	@Test
	public void testRandomBytesAfterPacket() {

		final String payloadString = "hello, world";
		byte[] expectedPayload = payloadString.getBytes();

		ChannelBuffer encodedBuffer = ChannelBuffers.wrappedBuffer(
				DleStxEtxConstants.DLE_STX,
				expectedPayload,
				DleStxEtxConstants.DLE_ETX,
				"klasjd".getBytes()
		);

		testDecoding(encodedBuffer, expectedPayload);
	}

	@Test
	public void testRandomBytesBeforeAndAfterPacket() {

		final String payloadString = "hello, world";
		byte[] expectedPayload = payloadString.getBytes();

		ChannelBuffer encodedBuffer = ChannelBuffers.wrappedBuffer(
				"klasjd".getBytes(),
				DleStxEtxConstants.DLE_STX,
				expectedPayload,
				DleStxEtxConstants.DLE_ETX,
				"klasjd".getBytes()
		);

		testDecoding(encodedBuffer, expectedPayload);
	}

	private void testDecoding(ChannelBuffer encodedBuffer, byte[] expectedPayload) {
		DecoderEmbedder<ChannelBuffer> decoder = new DecoderEmbedder<ChannelBuffer>(new DleStxEtxFramingDecoder());
		decoder.offer(encodedBuffer);
		ChannelBuffer decodedBuffer = decoder.poll();
		byte[] decodedPayload = new byte[decodedBuffer.readableBytes()];
		decodedBuffer.readBytes(decodedPayload);
		assertArrayEquals(expectedPayload, decodedPayload);
	}

}
