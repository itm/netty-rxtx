package de.uniluebeck.itm.nettyrxtx;


import java.net.SocketAddress;

public class RXTXDeviceAddress extends SocketAddress {

	private final String deviceAddress;

	/**
	 *
	 * @param deviceAddress the address of the device (e.g. COM1, /dev/ttyUSB0, ...)
	 */
	public RXTXDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}
}
