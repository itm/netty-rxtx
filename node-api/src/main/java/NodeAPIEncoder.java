import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.*;

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

		NodeAPIPacket nodeAPIInputPacket = (NodeAPIPacket) msg;

		log.trace("[{}] Encoded NodeAPIPacket: {}", ctx.getName(), nodeAPIInputPacket);
		return nodeAPIInputPacket.getISensePacket();

	}
}
