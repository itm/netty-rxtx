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
public class CommandPacket extends NodeAPIPacket {

	//Constructors
	public CommandPacket(ChannelBuffer buffer){
		super(buffer);
	}

	public CommandPacket(final byte command_type, final byte request_id, final ChannelBuffer payload){
		this(ChannelBuffers.wrappedBuffer(
				ChannelBuffers.wrappedBuffer(new byte[]{command_type}),
				ChannelBuffers.wrappedBuffer(new byte[]{request_id}),
				payload));
	}

	public CommandPacket(final NodeAPIPacketType command_type, final byte request_id, final ChannelBuffer payload){
		this(command_type.getValue(), request_id, payload);
	}


	public byte getCommandType(){
		return super.getCommandType();
	}

	public byte getRequestId(){
		return super.getRequestId();
	}

	public ChannelBuffer getPayload(){
		return super.buffer.slice(2, this.buffer.readableBytes() - 2);
	}


}
