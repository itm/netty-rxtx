package de.uniluebeck.itm.nettyrxtx.isense;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;


public class ISenseDecoder extends OneToOneDecoder {

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		ChannelBuffer buffer = (ChannelBuffer) msg;
		return new ISensePacket(buffer.readByte(), buffer.slice());
	}
}
