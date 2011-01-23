package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 * Interface defining a factory method for creating ChannelUpstreamHandler instances.
 */
public interface ChannelUpstreamHandlerFactory {

	/**
	 * Creates a new instance of the ChannelUpstreamHandler with configuration parameters {@code parameters}.
	 *
	 * @return the newly constructed ChannelUpstreamHandler instance
	 */
	ChannelUpstreamHandler create();

}
