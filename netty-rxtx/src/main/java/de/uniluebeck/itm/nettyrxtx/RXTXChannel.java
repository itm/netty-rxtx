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
		return ((RXTXChannelSink) getPipeline().getSink()).getConfig();
	}

	public boolean isBound() {
		return ((RXTXChannelSink) getPipeline().getSink()).isBound();
	}

	public boolean isConnected() {
		return ((RXTXChannelSink) getPipeline().getSink()).isConnected();
	}

	public SocketAddress getLocalAddress() {
		throw new UnsupportedOperationException();
	}

	public SocketAddress getRemoteAddress() {
		return ((RXTXChannelSink) getPipeline().getSink()).getRemoteAddress();
	}

	@Override
	public ChannelFuture bind(final SocketAddress localAddress) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ChannelFuture unbind() {
		throw new UnsupportedOperationException();
	}

	void doSetClosed() {
		setClosed();
	}

}
