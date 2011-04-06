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

package packet;

import java.util.HashMap;
import java.util.Map;

public enum NodeAPIPacketType {

	DEBUG_MESSAGE((byte) (0xFF & 10)),
	VIRTUAL_LINK_MESSAGE((byte) (0xFF & 11)),
	BYTE_MESSAGE((byte) (0xFF & 12)),
	FLASH_MESSAGE((byte) (0xFF & 13)),

	ENABLE_NODE((byte) (0xFF & 20)),
	DISABLE_NODE((byte) (0xFF & 21)),
	RESET_NODE((byte) (0xFF & 22)),
	SET_START_TIME((byte) (0xFF & 23)),
	SET_VIRTUAL_ID((byte) (0xFF & 24)),
	IS_NODE_ALIVE((byte) (0xFF & 25)),
	GET_VERSION((byte) (0xFF & 26)),

	SET_VIRTUAL_LINK((byte) (0xFF & 30)),
	DESTROY_VIRTUAL_LINK((byte) (0xFF & 31)),
	ENABLE_PHYSICAL_LINK((byte) (0xFF & 32)),
	DISABLE_PHYSICAL_LINK((byte) (0xFF & 33)),

	GET_PROPERTY_VALUE((byte) (0xFF & 40)),
	GET_NEIGHBORHOOD((byte) (0xFF & 41)),

	NODE_OUTPUT_TEXT((byte) (0xFF & 50)),
	NODE_OUTPUT_BYTE((byte) (0xFF & 51)),
	NODE_OUTPUT_VIRTUAL_LINK((byte) (0xFF & 52));
	
	private static final Map<Byte, NodeAPIPacketType> typesMap = new HashMap<Byte, NodeAPIPacketType>();

	static {
		for (NodeAPIPacketType packetType : NodeAPIPacketType.values()) {
			typesMap.put(packetType.value, packetType);
		}
	}

	private final byte value;

	NodeAPIPacketType(byte value) {
		this.value = value;
	}

	/**
	 * Returns the enum constant with value {@code value} or null if none of the enum values matches {@code value}.
	 *
	 * @param value the packets type
	 * @return an ISensePacketType enum constant or {@code null} if unknown
	 */
	public static NodeAPIPacketType fromValue(byte value) {
		return typesMap.get(value);
	}

	public byte getValue() {
		return value;
	}

	public boolean isNodeOutputPacket(){
		return (this.equals(NODE_OUTPUT_BYTE) || this.equals(NODE_OUTPUT_TEXT) || this.equals(NODE_OUTPUT_VIRTUAL_LINK));
	}
}
