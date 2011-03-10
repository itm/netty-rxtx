package packet;

import de.uniluebeck.itm.nettyrxtx.StringUtils;
import de.uniluebeck.itm.nettyrxtx.isense.ISensePacket;
import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.03.11
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public abstract class NodeAPIPacket {

	protected final ISensePacket iSensePacket;
	protected final ChannelBuffer buffer;

	protected NodeAPIPacket(ISensePacket iSensePacket) {
		this.iSensePacket = iSensePacket;
		this.buffer = iSensePacket.getPayload();
	}

	//getters

	public abstract ChannelBuffer getPayload();

	public byte getCommandType(){
		return this.buffer.getByte(0);
	}

	public ISensePacket getISensePacket() {
		return this.iSensePacket;
	}

	//String toString()
	public String toString(){
		NodeAPIPacketType packetType = NodeAPIPacketType.fromValue(getCommandType());
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName() + "[type=");
		builder.append(packetType == null ? getCommandType() : packetType);
		builder.append(",payload=");
		builder.append(StringUtils.toHexString(getPayload()));
		builder.append("]");
		return builder.toString();
	}
}
