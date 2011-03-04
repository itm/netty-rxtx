import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.*;

import static packet.NodeAPIPacketType.DEBUG_MESSAGE;
import static packet.NodeAPIPacketType.NODE_OUTPUT_TEXT;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 17:32
 * To change this template use File | Settings | File Templates.
 */
public class NodeAPIEncoder extends OneToOneEncoder {

	private static final Logger log = LoggerFactory.getLogger(NodeAPIEncoder.class);

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {

		if (!(msg instanceof NodeAPIPacket)) {
			return msg;
		}

		ChannelBuffer buffer = (ChannelBuffer) msg;
		NodeAPIPacketType packetType = NodeAPIPacket.getPacketType(buffer);
		NodeAPIPacket nodeAPIPacket = null;

		//find out type of message
		if (packetType.isNodeOutputPacket()) {
			nodeAPIPacket = new NodeOutputPacket(buffer);
		} else {
			nodeAPIPacket = new CommandPacket(buffer);
		}

		log.trace("[{}] Encoded NodeAPIPacket: {}", ctx.getName(), nodeAPIPacket);
		return nodeAPIPacket;

	}
}
