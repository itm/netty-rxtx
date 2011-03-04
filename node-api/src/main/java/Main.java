import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoder;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingEncoder;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketDecoder;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketEncoder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.NodeAPIPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main (String[] args){
		String deviceAddress = args[0];

		ExecutorService executor = Executors.newCachedThreadPool();
		ClientBootstrap bootstrap = new ClientBootstrap(new RXTXChannelFactory(executor));

		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				DefaultChannelPipeline pipeline = new DefaultChannelPipeline();

				pipeline.addLast("framingDecoder", new DleStxEtxFramingDecoder());
				pipeline.addLast("iSenseDecoder", new ISensePacketDecoder());
				pipeline.addLast("nodeAPIDecoder", new NodeAPIDecoder());

				pipeline.addLast("loggingHandler", new SimpleChannelHandler() {
					@Override
					public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e)
							throws Exception {
						NodeAPIPacket packet = (NodeAPIPacket) e.getMessage();
						log.info("{}", packet);
					}
				});

				pipeline.addLast("nodeAPIEncoder", new NodeAPIEncoder());
				pipeline.addLast("iSenseEncoder", new ISensePacketEncoder());
				pipeline.addLast("framingEncoder", new DleStxEtxFramingEncoder());

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
