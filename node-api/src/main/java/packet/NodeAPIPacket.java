package packet;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class NodeAPIPacket {

	protected final ChannelBuffer buffer;

	protected NodeAPIPacket(ChannelBuffer buffer) {
		this.buffer = buffer;
	}

	//getters

	public abstract ChannelBuffer getPayload();

	protected byte getCommandType(){
		return getCommandType(this.buffer);
	}

	protected byte getRequestId(){
		return this.buffer.getByte(1);
	}

	protected byte getResult(){
		return this.buffer.getByte(2);
	}

	public ChannelBuffer getBuffer() {
		return buffer;
	}


	//static methods
	
	public static NodeAPIPacketType getPacketType(ChannelBuffer buffer){
		return NodeAPIPacketType.fromValue(getCommandType(buffer));
	}

	private static byte getCommandType(ChannelBuffer buffer){
		return buffer.getByte(0);
	}	
}
