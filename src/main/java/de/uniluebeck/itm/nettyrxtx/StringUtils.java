package de.uniluebeck.itm.nettyrxtx;


import java.nio.ByteBuffer;

public class StringUtils {

	/**
	 *
	 */
	public static String toHexString(char tmp) {
		return toHexString((byte) tmp);
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	public static String toHexString(byte[] tmp) {
		return toHexString(tmp, 0, tmp.length);
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	public static String toHexString(byte tmp) {
		return "0x" + Integer.toHexString(tmp & 0xFF);
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	public static String toHexString(byte[] tmp, int offset) {
		return toHexString(tmp, offset, tmp.length - offset);
	}

	// -------------------------------------------------------------------------

	/**
	 *
	 */
	public static String toHexString(byte[] tmp, int offset, int length) {
		StringBuffer s = new StringBuffer();
		for (int i = offset; i < offset + length; ++i) {
			if (s.length() > 0) {
				s.append(' ');
			}
			s.append("0x");
			s.append(Integer.toHexString(tmp[i] & 0xFF));
		}
		return s.toString();
	}

	public static String toHexString(ByteBuffer bb) {
		return toHexString(bb.array(), 0, bb.capacity());
	}

	public static String toHexString(ByteBuffer bb, int offset, int length) {
		return toHexString(bb.array(), offset, length);
	}

}
