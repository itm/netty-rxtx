package de.uniluebeck.itm.nettyrxtx;


import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 * Interface defining a factory method for creating ChannelUpstreamHandler instances.
 */
public interface ChannelUpstreamHandlerFactory {

	/**
	 * Creates a new instance of the ChannelUpstreamHandler with configuration parameters {@code parameters}.
	 *
	 * @param parameters a configuration object to pass to the actual implementation or {@code null} if no configuration is
	 *                   needed / must not be used
	 *
	 * @return the newly constructed ChannelUpstreamHandler instance
	 */
	ChannelUpstreamHandler create(Object parameters);

}
