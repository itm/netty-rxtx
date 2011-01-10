package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import java.util.concurrent.Executor;

public class RXTXChannelFactory implements ChannelFactory {

	private final ChannelGroup channels = new DefaultChannelGroup("RXTXChannelFactory-ChannelGroup");

	private Executor executor;

	public RXTXChannelFactory(Executor executor) {
		this.executor = executor;
	}

	@Override
	public Channel newChannel(final ChannelPipeline pipeline) {
		RXTXChannel channel = new RXTXChannel(executor, pipeline, this);
		channels.add(channel);
		return channel;
	}

	@Override
	public void releaseExternalResources() {
		ChannelGroupFuture close = channels.close();
		close.awaitUninterruptibly();
	}
}
