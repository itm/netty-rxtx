package de.uniluebeck.itm.nettyrxtx.isense;


import java.util.HashMap;
import java.util.Map;

public enum ISensePacketType {

	//reserved inbound types
	CODE((byte) (0xFF & 0)), // deprecated, might be removed in the future

	RESET((byte) (0xFF & 1)),

	SERAERIAL((byte) (0xFF & 2)),

	TIME((byte) (0xFF & 3)),

	CAMERA_APPLICATION((byte) (0xFF & 4)),

	AMR_APPLICATION((byte) (0xFF & 5)),

	ACC_APPLICATION((byte) (0xFF & 6)),

	VIRTUAL_RADIO_IN((byte) (0xFF & 7)),

	IN_RESERVED_2((byte) (0xFF & 8)),

	IN_RESERVED_3((byte) (0xFF & 9)),

	//inbound types for users
	CUSTOM_IN_1((byte) (0xFF & 10)),

	CUSTOM_IN_2((byte) (0xFF & 11)),

	CUSTOM_IN_3((byte) (0xFF & 12)),

	NETWORK_IN((byte) (0xFF & 13)),

	//output types
	//DEBUG(100),
	//INFO(101),
	//WARNING(102),
	//ERROR(103),
	LOG((byte) (0xFF & 104)),

	PLOT((byte) (0xFF & 105)),

	CUSTOM_OUT((byte) (0xFF & 106)),

	JPEG((byte) (0xFF & 108)),

	TIMEREQUEST((byte) (0xFF & 109)),

	AUDIO((byte) (0xFF & 110)),

	SPYGLASS((byte) (0xFF & 111)),

	FLOAT_BUFFER((byte) (0xFF & 112)),

	SQL((byte) (0xFF & 113)),

	VIRTUAL_RADIO_OUT((byte) (0xFF & 114)),

	NETWORK_OUT((byte) (0xFF & 115));

	private static final Map<Byte, ISensePacketType> typesMap = new HashMap<Byte, ISensePacketType>();

	static {
		for (ISensePacketType packetType : ISensePacketType.values()) {
			typesMap.put(packetType.value, packetType);
		}
	}

	private final byte value;

	ISensePacketType(byte value) {
		this.value = value;
	}

	/**
	 * Returns the enum constant with value {@code value} or null if none of the enum values matches {@code value}.
	 *
	 * @param value the packets type
	 * @return an ISensePacketType enum constant or {@code null} if unknown
	 */
	public static ISensePacketType fromValue(byte value) {
		return typesMap.get(value);
	}
}
