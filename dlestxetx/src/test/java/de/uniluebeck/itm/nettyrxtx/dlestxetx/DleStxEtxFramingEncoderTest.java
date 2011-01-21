package de.uniluebeck.itm.nettyrxtx.dlestxetx;

import com.google.common.primitives.Bytes;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DleStxEtxFramingEncoderTest {

	@Test
	public void testEncoding() {

		byte[] payloadBytes = "hello, world".getBytes();

		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(new DleStxEtxFramingEncoder());
		embedder.offer(ChannelBuffers.wrappedBuffer(payloadBytes));

		ChannelBuffer encodedBuffer = embedder.poll();
		byte[] encodedBytes = new byte[encodedBuffer.readableBytes()];
		encodedBuffer.readBytes(encodedBytes);

		assertArrayEquals(Bytes.concat(DleStxEtxConstants.DLE_STX, payloadBytes, DleStxEtxConstants.DLE_ETX), encodedBytes);
	}

}
