package de.uniluebeck.itm.nettyrxtx.rup;


public enum RUPPacketType {

	/**
	 * A remote UART message.
	 */
	MESSAGE((byte) (0xFF & 60)),

	/**
	 * A remote UART message indicating that a node request a sink to send its output to.
	 */
	SINK_REQUEST((byte) (0xFF & 61)),

	/**
	 * A remote UART message indicating that the sender of this message offers to be a sink for the requester.
	 */
	SINK_RESPONSE((byte) (0xFF & 62)),

	/**
	 * A remote UART message that can be sent to the connected node to tell him to be sink for other nodes.
	 */
	SET_SINK((byte) (0xFF & 63));

	private final byte value;

	private RUPPacketType(byte value) {
		this.value = value;
	}

	public byte getValue() {
		return value;
	}

	public static RUPPacketType fromValue(byte value) {
		switch (value) {
			case 60:
				return MESSAGE;
			case 61:
				return SINK_REQUEST;
			case 62:
				return SINK_RESPONSE;
			case 63:
				return SET_SINK;
			default:
				throw new IllegalArgumentException(value + " is not a valid RUPPacketType");
		}
	}
}
