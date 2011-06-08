package de.uniluebeck.itm.nettyrxtx.nodeapi; /**********************************************************************************************************************
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
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uniluebeck.itm.nettyrxtx.nodeapi.packet.NodeAPIPacket;
import de.uniluebeck.itm.nettyrxtx.nodeapi.packet.ResponsePacket;

/**
 * This decoder-class is for decoding ISensePackets or returning a ResponsePacket
 */
public class NodeAPIDecoder extends OneToOneDecoder {

	private static final Logger log = LoggerFactory.getLogger(NodeAPIDecoder.class);

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		ChannelBuffer payload;

		if (msg instanceof ISensePacket) {
			payload = ((ISensePacket) msg).getPayload();
		} else if (msg instanceof ChannelBuffer) {
			payload = (ChannelBuffer) msg;
		} else {
			return msg;
		}
		
		NodeAPIPacket responsePacket = new ResponsePacket(payload);
		log.trace("[{}] Decoded NodeAPIPacket: {}", ctx.getName(), payload);
		return responsePacket;
	}
}
