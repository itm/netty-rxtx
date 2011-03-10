package packet;

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class NodeOutputPacket extends NodeAPIPacket {

	/*public NodeOutputPacket(ISensePacket iSensePacket){
		super(iSensePacket);
	}*/
	
	public NodeOutputPacket(byte command_type, ChannelBuffer payload){
		//TODO check if constructor with ISensePacketType.NETWORK_IN is correct?
		super(new ISensePacket(ISensePacketType.NETWORK_IN,
				ChannelBuffers.wrappedBuffer(
						ChannelBuffers.wrappedBuffer(new byte[]{command_type}),
						payload
				)
			));
	}

	public byte getCommandType(){
		return super.getCommandType();
	}

	public ChannelBuffer getPayload(){
		return buffer.slice(1, buffer.readableBytes() - 1);
	}

}
