package de.uniluebeck.itm.nettyrxtx.rup;

import java.util.Random;

public class PacketData {

	final byte cmdType;

	final byte sequenceNumber;

	final long destination;

	final long source;

	final byte[] payload;

	private PacketData(final byte cmdType, final byte sequenceNumber, final long destination, final long source,
					   final byte[] payload) {
		this.cmdType = cmdType;
		this.sequenceNumber = sequenceNumber;
		this.destination = destination;
		this.source = source;
		this.payload = payload;
	}

	public static PacketData generateNewPacketFragment() {
		final Random random = new Random();
		byte[] payload = new byte[random.nextInt(19) + 1]; // at least one byte of payload
		random.nextBytes(payload);
		PacketData packetData = new PacketData(
				(byte) (0xFF & (random.nextInt(3) + 60)), // random value out of 60, 61, 62, 63
				(byte) (0xFF & random.nextInt(255)),
				random.nextLong(),
				random.nextLong(),
				payload
		);
		return packetData;
	}

}