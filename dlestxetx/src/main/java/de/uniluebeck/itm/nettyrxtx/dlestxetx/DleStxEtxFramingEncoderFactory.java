package de.uniluebeck.itm.nettyrxtx.dlestxetx;

import de.uniluebeck.itm.nettyrxtx.ChannelDownstreamHandlerFactory;
import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;


public class DleStxEtxFramingEncoderFactory implements ChannelDownstreamHandlerFactory {

	public ChannelDownstreamHandler create() {
		return new DleStxEtxFramingEncoder();
	}
}
