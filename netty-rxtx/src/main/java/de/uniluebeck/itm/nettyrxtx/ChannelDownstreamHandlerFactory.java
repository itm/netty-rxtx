package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.channel.ChannelDownstreamHandler;

/**
 * Interface defining a factory method for creating ChannelDownstreamHandler instances.
 */
public interface ChannelDownstreamHandlerFactory {

	/**
	 * Creates a new instance of the ChannelDownstreamHandler with configuration parameters {@code parameters}.
	 *
	 * @return the newly constructed ChannelDownstreamHandler instance
	 */
	ChannelDownstreamHandler create();

}
