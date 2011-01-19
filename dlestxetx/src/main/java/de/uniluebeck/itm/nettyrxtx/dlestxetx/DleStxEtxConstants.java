package de.uniluebeck.itm.nettyrxtx.dlestxetx;


public class DleStxEtxConstants {

	public static final byte DLE = 0x10;

	public static final byte STX = 0x02;

	public static final byte ETX = 0x03;

	public static final byte[] DLE_STX = new byte[] { DLE, STX };

	public static final byte[] DLE_ETX = new byte[] { DLE, ETX };

}
