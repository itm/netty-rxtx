import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.junit.Test;
import packet.CommandPacket;
import packet.NodeAPIPacket;
import packet.NodeOutputPacket;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static packet.NodeAPIPacketType.DEBUG_MESSAGE;
import static packet.NodeAPIPacketType.NODE_OUTPUT_TEXT;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.03.11
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */

public class NodeAPIEncoderTest {

	final byte COMMAND_PACKET_TYPE = DEBUG_MESSAGE.getValue();
	final byte NODE_OUTPUT_PACKET_TYPE = NODE_OUTPUT_TEXT.getValue();
	final byte[] payloadBytes = "hello, world".getBytes();
	EncoderEmbedder<ISensePacket> encoder = new EncoderEmbedder<ISensePacket>(new NodeAPIEncoder());
	final byte request_id = 1;

	@Test
	public void testEncodingOfCommandPacket() {
		NodeAPIPacket commandPacket = new CommandPacket(COMMAND_PACKET_TYPE, request_id, ChannelBuffers.wrappedBuffer(payloadBytes));
		encoder.offer(commandPacket);

		byte[] expectedPayload = getExpectedPayloadFromPacket(commandPacket);
		ISensePacket encodedISensePacket = getEncodedPacketFromEncoder();
		byte[] encodedPayload = getEncodedPayloadFromISensePacket(encodedISensePacket);
		assertArrayEquals(encodedPayload, expectedPayload);
	}

	@Test
	public void testEncodingOfNodeOutputPacket() {
		NodeAPIPacket nodeOutputPacket = new NodeOutputPacket(NODE_OUTPUT_PACKET_TYPE, ChannelBuffers.wrappedBuffer(payloadBytes));
		encoder.offer(nodeOutputPacket);

		byte[] expectedPayload = getExpectedPayloadFromPacket(nodeOutputPacket);
		ISensePacket encodedISensePacket = getEncodedPacketFromEncoder();
		byte[] encodedPayload = getEncodedPayloadFromISensePacket(encodedISensePacket);
		assertArrayEquals(encodedPayload, expectedPayload);
	}

	public byte[] getExpectedPayloadFromPacket(NodeAPIPacket nodeAPIPacket){
		byte[] expectedPayload = new byte[nodeAPIPacket.getISensePacket().getPayload().readableBytes()];
		nodeAPIPacket.getISensePacket().getPayload().readBytes(expectedPayload);
		return expectedPayload;
	}

	public ISensePacket getEncodedPacketFromEncoder() {
		return encoder.poll();
	}

	private byte[] getEncodedPayloadFromISensePacket(ISensePacket encodedISensePacket){
		ChannelBuffer encodedPayload = encodedISensePacket.getPayload();
		byte[] encodedBytes = new byte[encodedPayload.readableBytes()];
		encodedPayload.readBytes(encodedBytes);

		return encodedBytes;
	}

}
