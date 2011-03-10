package packet;

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.nio.channels.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class CommandPacket extends NodeAPIPacket {

	//Constructor
	/*public CommandPacket(ISensePacket iSensePacket){
		super(iSensePacket);
	}*/

	public CommandPacket(byte command_type, byte request_id, ChannelBuffer payload){
		//TODO check if constructor with ISensePacketType.NETWORK_IN is correct?
		super(new ISensePacket(ISensePacketType.NETWORK_IN,
				ChannelBuffers.wrappedBuffer(
					ChannelBuffers.wrappedBuffer(new byte[]{command_type}),
					ChannelBuffers.wrappedBuffer(new byte[]{request_id}),
					payload
				)
			));
	}

	public byte getCommandType(){
		return super.getCommandType();
	}

	public byte getRequestId(){
		return buffer.getByte(1);
	}

	public ChannelBuffer getPayload(){
		return buffer.slice(2, buffer.readableBytes() - 2);
	}


}
