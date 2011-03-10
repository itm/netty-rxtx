import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;
import packet.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	final ISensePacketType iSENSE_PACKET_TYPE_LOG = ISensePacketType.LOG;

	@Test
	public void testPayloadOfResponsePacketType(){
		ChannelBuffer encodedPayload = ChannelBuffers.wrappedBuffer(
				new byte[]{RESPONSE_PACKET_TYPE},
				new byte[]{request_id},
				new byte[]{result},
				payload
		);
		NodeAPIPacket decodedPacket = getDecodedPacketFromDecoder(new ISensePacket(iSENSE_PACKET_TYPE_LOG, encodedPayload));
		assertTrue(decodedPacket instanceof ResponsePacket);
		comparePacketType(decodedPacket.getCommandType(), RESPONSE_PACKET_TYPE);
		compareRequestId(((ResponsePacket)decodedPacket).getRequestId(), request_id);
		compareResult(((ResponsePacket)decodedPacket).getResult(), result);
		comparePayload(decodedPacket, payload);
	}

	private NodeAPIPacket getDecodedPacketFromDecoder(ISensePacket iSensePacket){
		DecoderEmbedder<ResponsePacket> decoder = new DecoderEmbedder<ResponsePacket>(new NodeAPIDecoder());
		decoder.offer(iSensePacket);

		return decoder.poll();
	}

	private void comparePayload(NodeAPIPacket decodedPacket, byte[] expectedPayload) {
		ChannelBuffer decodedBuffer = decodedPacket.getPayload();
		byte[] decodedPayload = new byte[decodedBuffer.readableBytes()];
		decodedBuffer.readBytes(decodedPayload);
		assertArrayEquals(expectedPayload, decodedPayload);
	}

	private void compareRequestId(byte decodedRequestId, byte expectedRequestId){
		assertEquals(decodedRequestId, expectedRequestId);
	}

	private void comparePacketType(byte decodedPacketType, byte expectedPacketType){
		assertEquals(decodedPacketType, expectedPacketType);
	}

	private void compareResult(byte decodedResult, byte expectedResult){
		assertEquals(decodedResult, expectedResult);
	}
}
