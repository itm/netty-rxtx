package packet;

import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
public class ResponsePacket extends NodeAPIPacket {

	//Constructor
	public ResponsePacket(ISensePacket iSensePacket){
		super(iSensePacket);
	}

	//getters

	public byte getCommandType(){
		return super.getCommandType();
	}

	public byte getRequestId(){
		return buffer.getByte(1);
	}

	public byte getResult(){
		return buffer.getByte(2);
	}

	public ChannelBuffer getPayload(){
		return buffer.slice(3, buffer.readableBytes() - 3);
	}
}
