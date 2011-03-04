import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class NodeAPIDecoder extends OneToOneDecoder {

	private static final Logger log = LoggerFactory.getLogger(NodeAPIDecoder.class);

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}

		ResponsePacket packet = (ResponsePacket) msg;
		log.trace("[{}] Decoded NodeAPIPacket: {}", ctx.getName(), packet);
		return packet.getBuffer();
	}
}
