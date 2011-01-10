package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.logging.InternalLogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		String deviceAddress = args[0];

		ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(Executors.newCachedThreadPool()));

		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				DefaultChannelPipeline pipeline = new DefaultChannelPipeline();
				pipeline.addLast("loggingHandler", new SimpleChannelHandler() {
					@Override
					public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
							throws Exception {
						log.debug("{}", Arrays.toString(((ByteBuffer) e.getMessage()).array()));
						super.messageReceived(ctx, e);
					}
				});
				return pipeline;
			}
		});

		// Make a new connection.
		ChannelFuture connectFuture = bootstrap.connect(new RXTXDeviceAddress(deviceAddress));

		// Wait until the connection is made successfully.
		Channel channel = connectFuture.awaitUninterruptibly().getChannel();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		boolean exit = false;
		while (!exit) {
			try {
				String line = reader.readLine();
				if ("exit".equals(line)) {
					exit = true;
				}
			} catch (IOException e) {
				// ignore
			}
		}

		// Close the connection.
		channel.close().awaitUninterruptibly();

		// Shut down all thread pools to exit.
		bootstrap.releaseExternalResources();
	}

}
