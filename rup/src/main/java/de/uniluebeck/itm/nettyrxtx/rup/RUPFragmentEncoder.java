package de.uniluebeck.itm.nettyrxtx.rup;

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;


public class RUPFragmentEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		RUPPacketFragment packetFragment = (RUPPacketFragment) msg;
		return new ISensePacket(ISensePacketType.PLOT, packetFragment.getChannelBuffer());
	}
}
