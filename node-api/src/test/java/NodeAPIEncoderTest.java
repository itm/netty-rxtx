import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static packet.NodeAPIPacketType.DEBUG_MESSAGE;
import static packet.NodeAPIPacketType.NODE_OUTPUT_TEXT;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.03.11
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */

import com.google.common.primitives.Bytes;

public class NodeAPIEncoderTest {

	final byte COMMAND_PACKET_TYPE = DEBUG_MESSAGE.getValue();
	final byte NODE_OUTPUT_PACKET_TYPE = NODE_OUTPUT_TEXT.getValue();
	final byte[] payloadBytes = "hello, world".getBytes();
	EncoderEmbedder<ChannelBuffer> embedder = null;
	final byte request_id = 1;

	@Before
	public void setUp(){
		embedder = new EncoderEmbedder<ChannelBuffer>(new NodeAPIEncoder());
	}

	@Test
	public void testEncodingOfCommandPacket() {
		embedder.offer(ChannelBuffers.wrappedBuffer(
				new byte[]{COMMAND_PACKET_TYPE},
				new byte[]{request_id},
				payloadBytes));
		byte[] encodedBytes = getEncodedBytesFromEmbedder();
		assertArrayEquals(Bytes.concat(new byte[]{COMMAND_PACKET_TYPE}, new byte[]{request_id}, payloadBytes), encodedBytes);
	}

	@Test
	public void testEncodingOfNodeOutputPacket() {
		embedder.offer(ChannelBuffers.wrappedBuffer(
				new byte[]{NODE_OUTPUT_PACKET_TYPE},
				payloadBytes));
		byte[] encodedBytes = getEncodedBytesFromEmbedder();
		assertArrayEquals(Bytes.concat(new byte[]{NODE_OUTPUT_PACKET_TYPE}, payloadBytes), encodedBytes);
	}

	public byte[] getEncodedBytesFromEmbedder(){
		ChannelBuffer encodedBuffer = embedder.poll();
		byte[] encodedBytes = new byte[encodedBuffer.readableBytes()];
		encodedBuffer.readBytes(encodedBytes);

		return encodedBytes;
	}

}
