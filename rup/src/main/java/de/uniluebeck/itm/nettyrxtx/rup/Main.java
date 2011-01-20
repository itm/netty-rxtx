package de.uniluebeck.itm.nettyrxtx.rup;


import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoder;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingEncoder;
import de.uniluebeck.itm.nettyrxtx.isense.ISenseDecoder;
import de.uniluebeck.itm.nettyrxtx.isense.ISenseEncoder;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		String deviceAddress = args[0];

		ExecutorService executor = Executors.newCachedThreadPool();
		ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(executor));

		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				DefaultChannelPipeline pipeline = new DefaultChannelPipeline();

				pipeline.addLast("FramingDecoder", new DleStxEtxFramingDecoder());
				pipeline.addLast("ISenseDecoder", new ISenseDecoder());
				pipeline.addLast("RUPPacketDecoder", new RUPPacketDecoder());
				pipeline.addLast("RUPPayloadExtractor", new RUPPayloadExtractor());
				pipeline.addLast("RUPPayloadFramingDecoder", new DleStxEtxFramingDecoder());
				pipeline.addLast("StringDecoder", new StringDecoder(CharsetUtil.UTF_8));

				pipeline.addLast("LoggingHandler", new SimpleChannelHandler() {
					@Override
					public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
							throws Exception {
						log.info("{}", e.getMessage());
					}
				});

				pipeline.addLast("RUPPacketEncoder", new RUPPacketEncoder());
				pipeline.addLast("ISenseEncoder", new ISenseEncoder());
				pipeline.addLast("FramingEncoder", new DleStxEtxFramingEncoder());

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
