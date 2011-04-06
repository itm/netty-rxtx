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
		byte[] expectedPayload = new byte[nodeAPIPacket.getBuffer().readableBytes()];
		nodeAPIPacket.getBuffer().readBytes(expectedPayload);
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
