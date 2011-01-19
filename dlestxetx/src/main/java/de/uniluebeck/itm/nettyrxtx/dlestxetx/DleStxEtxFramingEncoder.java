package de.uniluebeck.itm.nettyrxtx.dlestxetx;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class DleStxEtxFramingEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		ChannelBuffer buffer = (ChannelBuffer) msg;
		ChannelBuffer packet = ChannelBuffers.dynamicBuffer(buffer.readableBytes()+4);
		packet.writeBytes(DleStxEtxConstants.DLE_STX);
		for (byte b : buffer.array()) {
			if (b == DleStxEtxConstants.DLE) {
				packet.writeByte(DleStxEtxConstants.DLE);
			}
			packet.writeByte(b);
		}
		packet.writeBytes(DleStxEtxConstants.DLE_ETX);
		return packet;
	}
}
