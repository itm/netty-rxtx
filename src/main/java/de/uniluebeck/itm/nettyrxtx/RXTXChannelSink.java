package de.uniluebeck.itm.nettyrxtx;

import gnu.io.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TooManyListenersException;
import java.util.concurrent.Executor;


public class RXTXChannelSink extends AbstractChannelSink {

	private static class WriteRunnable implements Runnable {

		private final DefaultChannelFuture future;

		private final RXTXChannelSink channelSink;

		private final Object message;

		public WriteRunnable(final DefaultChannelFuture future, final RXTXChannelSink channelSink, final Object message) {
			this.future = future;
			this.channelSink = channelSink;
			this.message = message;
		}

		public void run() {
			try {

				ByteBuffer buffer = (ByteBuffer) message;
				channelSink.outputStream.write(buffer.array());
				channelSink.outputStream.flush();
				future.setSuccess();

			} catch (Exception e) {
				future.setFailure(e);
			}
		}
	}

	private static class ConnectRunnable implements Runnable {

		private DefaultChannelFuture channelFuture;

		private RXTXChannelSink channelSink;

		private ConnectRunnable(final DefaultChannelFuture channelFuture, final RXTXChannelSink channelSink) {
			this.channelFuture = channelFuture;
			this.channelSink = channelSink;
		}

		public void run() {

			if (channelSink.closed) {
				channelFuture.setFailure(new Exception("Channel is already closed."));
			} else {
				try {
					connectInternal();
					log.debug("Successfully connected.");
					channelFuture.setSuccess();
				} catch (Exception e) {
					log.warn("" + e, e);
					channelFuture.setFailure(e);
				}
			}

		}

		private void connectInternal()
				throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException,
				TooManyListenersException {

			CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(channelSink.remoteAddress.getDeviceAddress());
			CommPort commPort = cpi.open(this.getClass().getName(), 1000);

			channelSink.serialPort = ((SerialPort) commPort);
			log.debug("Adding SerialPortEventListener");
			channelSink.serialPort.addEventListener(new RXTXSerialPortEventListener(channelSink));
			channelSink.serialPort.notifyOnDataAvailable(true);
			channelSink.serialPort.setSerialPortParams(
					channelSink.config.getBaudrate(),
					channelSink.config.getDatabits().getValue(),
					channelSink.config.getStopbits().getValue(),
					channelSink.config.getParitybit().getValue()
			);

			// TODO do this more generic
			channelSink.serialPort.setDTR(false);
			channelSink.serialPort.setRTS(false);

			channelSink.outputStream = new BufferedOutputStream(channelSink.serialPort.getOutputStream());
			channelSink.inputStream = new BufferedInputStream(channelSink.serialPort.getInputStream());
		}
	}

	private static class DisconnectRunnable implements Runnable {

		private DefaultChannelFuture channelFuture;

		private RXTXChannelSink channelSink;

		public DisconnectRunnable(final DefaultChannelFuture channelFuture, final RXTXChannelSink channelSink) {
			this.channelFuture = channelFuture;
			this.channelSink = channelSink;
		}

		public void run() {
			if (channelSink.closed) {
				channelFuture.setFailure(new Exception("Channel is already closed."));
			} else {
				try {
					disconnectInternal();
					log.debug("Successfully disconnected");
					channelFuture.setSuccess();
				} catch (Exception e) {
					log.warn("" + e, e);
					channelFuture.setFailure(e);
				}
			}
		}

		private void disconnectInternal() throws Exception {

			Exception exception = null;

			try {
				if (channelSink.inputStream != null) {
					channelSink.inputStream.close();
				}
			} catch (IOException e) {
				log.debug("Failed to close in-stream :" + e, e);
				exception = e;
			}

			try {
				if (channelSink.outputStream != null) {
					channelSink.outputStream.close();
				}
			} catch (IOException e) {
				log.debug("Failed to close out-stream :" + e, e);
				exception = e;
			}

			if (channelSink.serialPort != null) {
				channelSink.serialPort.removeEventListener();
				channelSink.serialPort.close();
			}

			channelSink.inputStream = null;
			channelSink.outputStream = null;
			channelSink.serialPort = null;

			if (exception != null) {
				throw exception;
			}

		}
	}

	private static final Logger log = LoggerFactory.getLogger(RXTXChannelSink.class);

	private final Executor executor;

	private RXTXChannelConfig config;

	private RXTXChannel channel;

	public RXTXChannelSink(final Executor executor) {
		this.executor = executor;
		config = new RXTXChannelConfig();
	}

	public boolean isConnected() {
		return inputStream != null && outputStream != null;
	}

	public RXTXDeviceAddress getRemoteAddress() {
		return remoteAddress;
	}

	public boolean isBound() {
		return false;
	}

	public ChannelConfig getConfig() {
		return config;
	}

	public void setChannel(final RXTXChannel channel) {
		this.channel = channel;
	}

	private static class RXTXSerialPortEventListener implements SerialPortEventListener {

		private RXTXChannelSink channelSink;

		public RXTXSerialPortEventListener(final RXTXChannelSink channelSink) {
			this.channelSink = channelSink;
		}

		public void serialEvent(final SerialPortEvent event) {
			log.trace("{}", event);
			switch (event.getEventType()) {
				case SerialPortEvent.DATA_AVAILABLE:
					try {
						if (channelSink.inputStream != null && channelSink.inputStream.available() > 0) {
							int available = channelSink.inputStream.available();
							byte[] buffer = new byte[available];
							int read = channelSink.inputStream.read(buffer);
							if (read > 0) {
								ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(buffer, 0, read);
								UpstreamMessageEvent upstreamMessageEvent = new UpstreamMessageEvent(
										channelSink.channel,
										channelBuffer,
										channelSink.getRemoteAddress()
								);
								channelSink.channel.getPipeline().sendUpstream(upstreamMessageEvent);
							}
						}
					} catch (IOException e) {
						log.error("" + e, e);
						channelSink.channel.close();
					}
					break;
			}
		}
	}

	private RXTXDeviceAddress remoteAddress;

	private BufferedOutputStream outputStream;

	private BufferedInputStream inputStream;

	private SerialPort serialPort;

	private volatile boolean closed = false;

	public void eventSunk(final ChannelPipeline pipeline, final ChannelEvent e) throws Exception {

		final ChannelFuture future = e.getFuture();

		if (e instanceof ChannelStateEvent) {

            final ChannelStateEvent stateEvent = (ChannelStateEvent) e;
            final ChannelState state = stateEvent.getState();
            final Object value = stateEvent.getValue();

            switch (state) {

            case OPEN:
                if (Boolean.FALSE.equals(value)) {
					executor.execute(new DisconnectRunnable((DefaultChannelFuture) future, this));
                }
                break;

            case BOUND:
				throw new UnsupportedOperationException();

            case CONNECTED:
                if (value != null) {
					remoteAddress = (RXTXDeviceAddress) value;
					executor.execute(new ConnectRunnable((DefaultChannelFuture) future, this));
                } else {
                    executor.execute(new DisconnectRunnable((DefaultChannelFuture) future, this));
                }
                break;

            case INTEREST_OPS:
                throw new UnsupportedOperationException();

            }

        } else if (e instanceof MessageEvent) {

            final MessageEvent event = (MessageEvent) e;
			executor.execute(new WriteRunnable((DefaultChannelFuture) future, this, event.getMessage()));
        }
	}

}
