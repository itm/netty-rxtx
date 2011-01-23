/**********************************************************************************************************************
 * Copyright (c) 2011, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.nettyrxtx;

import gnu.io.SerialPort;
import org.jboss.netty.channel.DefaultChannelConfig;
import org.jboss.netty.util.internal.ConversionUtil;

/**
 * TODO listen to change events and adapt channel to this
 */
public class RXTXChannelConfig extends DefaultChannelConfig {

	public static enum Stopbits {

		STOPBITS_1(SerialPort.STOPBITS_1),
		STOPBITS_2(SerialPort.STOPBITS_2),
		STOPBITS_1_5(SerialPort.STOPBITS_1_5);

		private int value;

		private Stopbits(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Stopbits ofValue(int value) {
			for (Stopbits stopbit : Stopbits.values()) {
				if (stopbit.value == value) {
					return stopbit;
				}
			}
			throw new IllegalArgumentException("Unknown value for Stopbits: " + value + ".");
		}
	}

	public static enum Databits {

		DATABITS_5(SerialPort.DATABITS_5),
		DATABITS_6(SerialPort.DATABITS_6),
		DATABITS_7(SerialPort.DATABITS_7),
		DATABITS_8(SerialPort.DATABITS_8);

		private int value;

		private Databits(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Databits ofValue(int value) {
			for (Databits databit : Databits.values()) {
				if (databit.value == value) {
					return databit;
				}
			}
			throw new IllegalArgumentException("Unknown value for Databits: " + value + ".");
		}
	}

	public static enum Paritybit {

		NONE(SerialPort.PARITY_NONE),
		ODD(SerialPort.PARITY_ODD),
		EVEN(SerialPort.PARITY_EVEN),
		MARK(SerialPort.PARITY_MARK),
		SPACE(SerialPort.PARITY_SPACE);

		private int value;

		private Paritybit(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Paritybit ofValue(int value) {
			for (Paritybit paritybit : Paritybit.values()) {
				if (paritybit.value == value) {
					return paritybit;
				}
			}
			throw new IllegalArgumentException("Unknown value for paritybit: " + value + ".");
		}
	}

	private int baudrate = 115200;

	private Stopbits stopbits = RXTXChannelConfig.Stopbits.STOPBITS_1;

	private Databits databits = RXTXChannelConfig.Databits.DATABITS_8;

	private Paritybit paritybit = RXTXChannelConfig.Paritybit.NONE;

	public RXTXChannelConfig() {
		// work with defaults ...
	}

	public RXTXChannelConfig(final int baudrate, final Stopbits stopbits,
							 final Databits databits, final Paritybit paritybit) {
		this.baudrate = baudrate;
		this.stopbits = stopbits;
		this.databits = databits;
		this.paritybit = paritybit;
	}

	@Override
	public boolean setOption(final String key, final Object value) {
		if (key.equals("baudrate")) {
			setBaudrate(ConversionUtil.toInt(value));
			return true;
		} else if (key.equals("stopbits")) {
			setStopbits((Stopbits) value);
			return true;
		} else if (key.equals("databits")) {
			setDatabits((Databits) value);
			return true;
		} else if (key.equals("paritybit")) {
			setParitybit((Paritybit) value);
			return true;
		} else {
			return super.setOption(key, value);
		}
	}

	public void setBaudrate(final int baudrate) {
		this.baudrate = baudrate;
	}

	public void setStopbits(final Stopbits stopbits) {
		this.stopbits = stopbits;
	}

	public void setDatabits(final Databits databits) {
		this.databits = databits;
	}

	private void setParitybit(final Paritybit paritybit) {
		this.paritybit = paritybit;
	}

	public int getBaudrate() {
		return baudrate;
	}

	public Stopbits getStopbits() {
		return stopbits;
	}

	public Databits getDatabits() {
		return databits;
	}

	public Paritybit getParitybit() {
		return paritybit;
	}
}
