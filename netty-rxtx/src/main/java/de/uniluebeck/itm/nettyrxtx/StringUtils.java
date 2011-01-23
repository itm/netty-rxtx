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


import org.jboss.netty.buffer.ChannelBuffer;

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

	/**
	 * Returns a hex string representation of {@code channelBuffer}. The method will read the buffer without modifying
	 * {@code readerIndex} and {@code writerIndex}.
	 *
	 * @param channelBuffer the buffer
	 * @param offset		the index in the buffer where to start from
	 * @param length		the number of bytes in the buffer to read
	 *
	 * @return a hex string representation of the buffer
	 */
	public static String toHexString(final ChannelBuffer channelBuffer, int offset, int length) {
		StringBuffer s = new StringBuffer();
		for (int i = offset; i < offset + length; ++i) {
			if (s.length() > 0) {
				s.append(' ');
			}
			s.append("0x");
			s.append(Integer.toHexString(channelBuffer.getByte(i) & 0xFF));
		}
		return s.toString();
	}

	/**
	 * Returns a hex string representation of {@code channelBuffer}. The method will read the buffer without modifying
	 * {@code readerIndex} and {@code writerIndex}.
	 *
	 * @param channelBuffer the buffer
	 *
	 * @return a hex string representation of the buffer
	 */
	public static String toHexString(final ChannelBuffer channelBuffer) {
		return toHexString(channelBuffer, channelBuffer.readerIndex(), channelBuffer.readableBytes());
	}

	/**
	 * Returns a string representation of {@code channelBuffer}. The method will read the buffer without modifying {@code
	 * readerIndex} and {@code writerIndex}.
	 *
	 * @param channelBuffer the buffer
	 * @param offset		the index in the buffer where to start from
	 * @param length		the number of bytes in the buffer to read
	 *
	 * @return a string representation of the buffer
	 */
	public static String toString(final ChannelBuffer channelBuffer, int offset, int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int i = offset; i < offset + length; i++) {
			builder.append(channelBuffer.getByte(i));
		}
		return builder.toString();
	}

	/**
	 * Returns a string representation of {@code channelBuffer}. The method will read the buffer after wrapping it, thereby
	 * not modifying {@code readerIndex} and {@code writerIndex}.
	 *
	 * @param channelBuffer the buffer
	 *
	 * @return a string representation of the buffer
	 */
	public static String toString(final ChannelBuffer channelBuffer) {
		return toString(channelBuffer, channelBuffer.readerIndex(), channelBuffer.readableBytes());
	}
}
