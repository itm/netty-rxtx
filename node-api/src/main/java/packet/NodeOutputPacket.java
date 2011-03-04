package packet;

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

	//Constructors
	public NodeOutputPacket(ChannelBuffer buffer){
		super(buffer);
	}

	public NodeOutputPacket(final byte command_type, final ChannelBuffer payload){
		super(ChannelBuffers.wrappedBuffer(
				ChannelBuffers.wrappedBuffer(new byte[]{command_type}),
				payload));
	}

	public NodeOutputPacket(final NodeAPIPacketType command_type, final ChannelBuffer payload){
		this(command_type.getValue(), payload);
	}

	
	public byte getCommandType(){
		return super.getCommandType();
	}

	public ChannelBuffer getPayload(){
		return super.buffer.slice(1, this.buffer.readableBytes() - 1);
	}

}
