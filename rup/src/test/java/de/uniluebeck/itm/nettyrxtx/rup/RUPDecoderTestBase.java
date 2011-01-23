package de.uniluebeck.itm.nettyrxtx.rup;

import com.google.common.collect.Maps;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import org.jboss.netty.buffer.ChannelBuffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;


public abstract class RUPDecoderTestBase {

	private Random random;

	private Map<Long, Byte> lastSequenceNumbers;

	protected void setUp() {
		random = new Random();
		lastSequenceNumbers = Maps.newHashMap();
	}

	protected void tearDown() {
		random = null;
		lastSequenceNumbers = null;
	}

	protected ChannelBuffer createOpeningAndClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + 2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected ChannelBuffer createOpeningMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(DleStxEtxConstants.DLE_STX);
		bb.put(payload.getBytes());
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected ChannelBuffer createMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, payload.getBytes()).getChannelBuffer();
	}

	protected ChannelBuffer createClosingMessageFragment(byte sequenceNumber, long destination, long source, String payload) {
		ByteBuffer bb = ByteBuffer.allocate(2 + payload.getBytes().length);
		bb.put(payload.getBytes());
		bb.put(DleStxEtxConstants.DLE_ETX);
		return RUPFragmentFactory.create(RUPPacket.Type.MESSAGE, sequenceNumber, destination, source, bb.array()).getChannelBuffer();
	}

	protected byte getRandomSequenceNumber(long sender) {
		byte sequenceNumber = (byte) (0xFF & (random.nextInt(255) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}

	protected byte getSubsequentSequenceNumber(long sender) {
		if (!lastSequenceNumbers.containsKey(sender)) {
			throw new IllegalArgumentException("No first sequence number existing!");
		}
		byte lastSequenceNumber = lastSequenceNumbers.get(sender);
		byte sequenceNumber = (byte) (0xFF & ((lastSequenceNumber + 1) % 255));
		lastSequenceNumbers.put(sender, sequenceNumber);
		return sequenceNumber;
	}
}
