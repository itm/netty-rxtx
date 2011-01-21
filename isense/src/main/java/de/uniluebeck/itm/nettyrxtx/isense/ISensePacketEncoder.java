package de.uniluebeck.itm.nettyrxtx.isense;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;


public class ISensePacketEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		ISensePacket packet = (ISensePacket) msg;
		return packet.getBuffer();
	}
}
