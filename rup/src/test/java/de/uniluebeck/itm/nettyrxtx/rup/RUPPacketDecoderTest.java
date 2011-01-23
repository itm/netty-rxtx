package de.uniluebeck.itm.nettyrxtx.rup;

import de.uniluebeck.itm.nettyrxtx.ChannelUpstreamHandlerFactory;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxConstants;
import de.uniluebeck.itm.nettyrxtx.dlestxetx.DleStxEtxFramingDecoderFactory;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class RUPPacketDecoderTest {

	private DecoderEmbedder<RUPPacket> decoder;

	@Before
	public void setUp() {
		decoder = new DecoderEmbedder<RUPPacket>(
				new RUPPacketFragmentDecoder(),
				new RUPPacketDecoder(
						new Tuple<ChannelUpstreamHandlerFactory, Object>(new DleStxEtxFramingDecoderFactory(), null)
				)
		);
	}

	@After
	public void tearDown() {

	}

	/**
	 * Tests if the decoder correctly recombines fragments that contain one packet that is framed by DLE STX ... DLE ETX.
	 */
	@Test
	public void testFragmentedOnePacketDleStxEtx() {

		RUPPacketFragment f1 = RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, (byte) 0, 0x1234, 0x4321, new byte[]{
				DleStxEtxConstants.DLE,
				DleStxEtxConstants.STX,
				'a', 'b', 'c',
		});
		RUPPacketFragment f2 = RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, (byte) 1, 0x1234, 0x4321, new byte[]{
				'd', 'e', 'f', 'g', 'h',
		});
		RUPPacketFragment f3 = RUPPacketFragmentFactory.create(RUPPacket.Type.MESSAGE, (byte) 2, 0x1234, 0x4321, new byte[]{
				'i', 'j', 'k',
				DleStxEtxConstants.DLE,
				DleStxEtxConstants.ETX,
		});

		// decode the packet
		decoder.offer(f1.getChannelBuffer());
		decoder.offer(f2.getChannelBuffer());
		decoder.offer(f3.getChannelBuffer());
		
		RUPPacket decodedPacket = decoder.poll();

		// at least one packet must have been decoded
		assertNotNull(decodedPacket);

		// only one packet should have been generated
		assertNull(decoder.poll());

		// compare the original header values with the decoded values
		assertTrue(RUPPacket.Type.MESSAGE.getValue() == decodedPacket.getCmdType());
		assertTrue(0x1234 == decodedPacket.getDestination());
		assertTrue(0x4321 == decodedPacket.getSource());

		// compare the original payload with the decoded payload
		byte[] decodedPayload = new byte[decodedPacket.getPayload().readableBytes()];
		decodedPacket.getPayload().readBytes(decodedPayload);
		assertArrayEquals(new byte[] {'a','b','c','d','e','f','g','h','i','j','k'}, decodedPayload);

	}

	/**
	 * Tests if the decoder correctly recombines fragments of multiple packets containing data which is framed by DLE STX
	 * ... DLE ETX.
	 */
	@Test
	public void testFragmentedMultiplePacketsDleStxEtx() {

	}

	/**
	 * Tests if a packet with an empty payload is successfully decoded.
	 */
	@Test
	public void testEmptyPacket() {

	}

	/**
	 * Tests if one fragmented packet for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedOnePacketMultipleSenders() {

	}

	/**
	 * Tests if multiple fragmented packets for each of multiple senders is decoded correctly.
	 */
	@Test
	public void testFragmentedMultiplePacketsMultipleSenders() {

	}

}
