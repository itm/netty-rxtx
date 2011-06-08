/**********************************************************************************************************************
 * Copyright (c) 2011, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import de.uniluebeck.itm.nettyrxtx.nodeapi.NodeAPIDecoder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.Test;
import de.uniluebeck.itm.nettyrxtx.nodeapi.packet.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
