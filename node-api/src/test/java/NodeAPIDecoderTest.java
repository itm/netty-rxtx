import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;
import packet.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static packet.NodeAPIPacketType.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class NodeAPIDecoderTest {
	final byte[] payload = "hello, world".getBytes();
	final byte request_id = 1;
	final byte result = 0;
	final byte RESPONSE_PACKET_TYPE = NodeAPIPacketType.BYTE_MESSAGE.getValue();

	@Test
	public void testPayloadOfCommandPacketType(){
		ChannelBuffer encodedBuffer = ChannelBuffers.wrappedBuffer(
				new byte[]{RESPONSE_PACKET_TYPE},
				new byte[]{request_id},
				new byte[]{result},
				payload
		);
		testDecoding(encodedBuffer, payload);
	}

	private void testDecoding(ChannelBuffer encodedBuffer, byte[] expectedPayload) {
		DecoderEmbedder<ChannelBuffer> decoder = new DecoderEmbedder<ChannelBuffer>(new NodeAPIDecoder());
		ResponsePacket responsePacket = new ResponsePacket(encodedBuffer);
		decoder.offer(responsePacket);

		NodeAPIPacket packet = (NodeAPIPacket) decoder.poll();

		ChannelBuffer decodedBuffer = packet.getPayload();
		byte[] decodedPayload = new byte[decodedBuffer.readableBytes()];
		decodedBuffer.readBytes(decodedPayload);
		assertTrue(packet instanceof ResponsePacket);
		assertArrayEquals(expectedPayload, decodedPayload);
	}

}
