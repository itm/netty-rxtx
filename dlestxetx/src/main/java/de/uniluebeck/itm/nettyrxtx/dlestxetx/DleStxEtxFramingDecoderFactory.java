package de.uniluebeck.itm.nettyrxtx.dlestxetx;

import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;


public class DleStxEtxFramingDecoderFactory implements ChannelUpstreamHandlerFactory {

	public ChannelUpstreamHandler create(final Object parameters) {
		return new DleStxEtxFramingDecoder();
	}
}
