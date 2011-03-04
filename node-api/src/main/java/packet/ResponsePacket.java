package packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class ResponsePacket extends NodeAPIPacket {

	//Constructors
	public ResponsePacket(ChannelBuffer buffer){
		super(buffer);
	}

	public ResponsePacket(final byte command_type, final byte request_id, final byte result, final ChannelBuffer payload){
		this(ChannelBuffers.wrappedBuffer(
				ChannelBuffers.wrappedBuffer(new byte[]{command_type}),
				ChannelBuffers.wrappedBuffer(new byte[]{request_id}),
				ChannelBuffers.wrappedBuffer(new byte[]{result}),
				payload));
	}

	public ResponsePacket(final NodeAPIPacketType command_type, final byte request_id, final byte result, final ChannelBuffer payload){
		this(command_type.getValue(), request_id, result, payload);
	}

	//getters

	public byte getCommandType(){
		return super.getCommandType();
	}

	public byte getRequestId(){
		return super.getRequestId();
	}

	public byte getResult(){
		return super.getResult();
	}

	public ChannelBuffer getPayload(){
		return this.buffer.slice(3, this.buffer.readableBytes() - 3);
	}
}
