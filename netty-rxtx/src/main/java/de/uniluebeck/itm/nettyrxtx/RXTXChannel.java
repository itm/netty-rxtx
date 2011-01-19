package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class RXTXChannel extends AbstractChannel {

	protected static final Logger log = LoggerFactory.getLogger(RXTXChannel.class);

	RXTXChannel(final Channel parent, final ChannelFactory factory, final ChannelPipeline pipeline,
					   final ChannelSink sink) {
		super(parent, factory, pipeline, sink);
	}

	public ChannelConfig getConfig() {
		log.trace("RXTXChannel.getConfig()");
		return ((RXTXChannelSink) getPipeline().getSink()).getConfig();
	}

	public boolean isBound() {
		log.trace("RXTXChannel.isBound()");
		return ((RXTXChannelSink) getPipeline().getSink()).isBound();
	}

	public boolean isConnected() {
		log.trace("RXTXChannel.isConnected()");
		return ((RXTXChannelSink) getPipeline().getSink()).isConnected();
	}

	public SocketAddress getLocalAddress() {
		log.trace("RXTXChannel.getLocalAddress()");
		throw new UnsupportedOperationException();
	}

	public SocketAddress getRemoteAddress() {
		log.trace("RXTXChannel.getRemoteAddress()");
		return ((RXTXChannelSink) getPipeline().getSink()).getRemoteAddress();
	}

	@Override
	public ChannelFuture bind(final SocketAddress localAddress) {
		log.trace("RXTXChannel.bind({})", localAddress);
		throw new UnsupportedOperationException();
	}

	@Override
	public ChannelFuture unbind() {
		log.trace("RXTXChannel.unbind()");
		throw new UnsupportedOperationException();
	}
}
