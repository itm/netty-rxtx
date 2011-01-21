package de.uniluebeck.itm.nettyrxtx.rup;

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;


public class RUPPacketEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		RUPPacket packet = (RUPPacket) msg;
		return new ISensePacket(ISensePacketType.PLOT, packet.getChannelBuffer());
	}
}
