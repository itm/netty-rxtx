package de.uniluebeck.itm.nettyrxtx;


import gnu.io.*;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.TooManyListenersException;
import java.util.concurrent.Executor;

public class RXTXChannel implements Channel {

	private static final Logger log = LoggerFactory.getLogger(RXTXChannel.class);

	private final Executor executor;

	private final ChannelPipeline pipeline;

	private final RXTXChannelFactory rxtxChannelFactory;

	private final Integer id = new Random().nextInt();

	private final RXTXChannelConfig channelConfig;

	private final DefaultChannelFuture closeFuture = new DefaultChannelFuture(this, false);

	private RXTXDeviceAddress remoteAddress;

	private BufferedOutputStream outputStream;

	private BufferedInputStream inputStream;

	private SerialPort serialPort;

	private volatile boolean closed = false;

	public RXTXChannel(final Executor executor, final ChannelPipeline pipeline,
					   final RXTXChannelFactory rxtxChannelFactory) {

		this.executor = executor;
		this.pipeline = pipeline;
		this.rxtxChannelFactory = rxtxChannelFactory;

		this.channelConfig = new RXTXChannelConfig();
	}

	public Integer getId() {
		return id;
	}

	public ChannelFactory getFactory() {
		return rxtxChannelFactory;
	}

	public Channel getParent() {
		return null;
	}

	public ChannelConfig getConfig() {
		return channelConfig;
	}

	public ChannelPipeline getPipeline() {
		return pipeline;
	}

	public boolean isOpen() {
		return !closed && isConnected();
	}

	public boolean isBound() {
		return false;
	}

	public boolean isConnected() {
		return inputStream != null && outputStream != null;
	}

	public SocketAddress getLocalAddress() {
		return null;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public ChannelFuture write(final Object message) {
		DefaultChannelFuture future = new DefaultChannelFuture(this, false);
		executor.execute(new WriteRunnable(future, this, message));
		return future;
	}

	public ChannelFuture write(final Object message, final SocketAddress remoteAddress) {
		return new FailedChannelFuture(this, new UnsupportedOperationException());
	}

	public ChannelFuture bind(final SocketAddress localAddress) {
		throw new UnsupportedOperationException();
	}

	private class WriteRunnable implements Runnable {

		private final DefaultChannelFuture future;

		private final RXTXChannel channel;

		private final Object message;

		public WriteRunnable(final DefaultChannelFuture future, final RXTXChannel channel, final Object message) {
			this.future = future;
			this.channel = channel;
			this.message = message;
		}

		public void run() {
			try {

				ByteBuffer buffer = (ByteBuffer) message;
				channel.outputStream.write(buffer.array());
				channel.outputStream.flush();
				future.setSuccess();

			} catch (Exception e) {
				future.setFailure(e);
			}
		}
	}

	private static class ConnectRunnable implements Runnable {

		private DefaultChannelFuture channelFuture;

		private RXTXChannel channel;

		private ConnectRunnable(final DefaultChannelFuture channelFuture, final RXTXChannel channel) {
			this.channelFuture = channelFuture;
			this.channel = channel;
		}

		public void run() {

			if (channel.closed) {
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

			CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(channel.remoteAddress.getDeviceAddress());
			CommPort commPort = cpi.open(this.getClass().getName(), 1000);

			channel.serialPort = ((SerialPort) commPort);
			log.debug("Adding SerialPortEventListener");
			channel.serialPort.addEventListener(new RXTXSerialPortEventListener(channel));
			channel.serialPort.notifyOnDataAvailable(true);
			channel.serialPort.setSerialPortParams(
					channel.channelConfig.getBaudrate(),
					channel.channelConfig.getDatabits().getValue(),
					channel.channelConfig.getStopbits().getValue(),
					channel.channelConfig.getParitybit().getValue()
			);
			channel.serialPort.setDTR(false);
			channel.serialPort.setRTS(false);

			channel.outputStream = new BufferedOutputStream(channel.serialPort.getOutputStream());
			channel.inputStream = new BufferedInputStream(channel.serialPort.getInputStream());
		}
	}

	private static class DisconnectRunnable implements Runnable {

		private DefaultChannelFuture channelFuture;

		private RXTXChannel channel;

		public DisconnectRunnable(final DefaultChannelFuture channelFuture, final RXTXChannel channel) {
			this.channelFuture = channelFuture;
			this.channel = channel;
		}

		public void run() {
			if (channel.closed) {
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
				if (channel.inputStream != null) {
					channel.inputStream.close();
				}
			} catch (IOException e) {
				log.debug("Failed to close in-stream :" + e, e);
				exception = e;
			}

			try {
				if (channel.outputStream != null) {
					channel.outputStream.close();
				}
			} catch (IOException e) {
				log.debug("Failed to close out-stream :" + e, e);
				exception = e;
			}

			if (channel.serialPort != null) {
				channel.serialPort.removeEventListener();
				channel.serialPort.close();
			}

			channel.inputStream = null;
			channel.outputStream = null;
			channel.serialPort = null;

			if (exception != null) {
				throw exception;
			}

		}
	}

	private static class RXTXSerialPortEventListener implements SerialPortEventListener {

		private RXTXChannel channel;

		public RXTXSerialPortEventListener(final RXTXChannel channel) {
			this.channel = channel;
		}

		public void serialEvent(final SerialPortEvent event) {
			log.debug("{}", event);
			switch (event.getEventType()) {
				case SerialPortEvent.DATA_AVAILABLE:
					try {
						if (channel.inputStream != null && channel.inputStream.available() > 0) {
							int available = channel.inputStream.available();
							byte[] buffer = new byte[available];
							int read = channel.inputStream.read(buffer);
							if (read > 0) {
								ByteBuffer byteBuffer = ByteBuffer.allocate(read);
								byteBuffer.put(buffer, 0, read);
								UpstreamMessageEvent upstreamMessageEvent =
										new UpstreamMessageEvent(channel, byteBuffer, channel.getRemoteAddress());
								channel.getPipeline().sendUpstream(upstreamMessageEvent);
							}
						}
					} catch (IOException e) {
						log.error("" + e, e);
						channel.close();
					}
					break;
			}
		}
	}

	public ChannelFuture connect(final SocketAddress remoteAddress) {
		this.remoteAddress = (RXTXDeviceAddress) remoteAddress;
		DefaultChannelFuture future = new DefaultChannelFuture(this, false);
		executor.execute(new ConnectRunnable(future, this));
		return future;
	}

	public ChannelFuture disconnect() {
		DefaultChannelFuture future = new DefaultChannelFuture(this, false);
		executor.execute(new DisconnectRunnable(future, this));
		return future;
	}

	public ChannelFuture unbind() {
		throw new UnsupportedOperationException();
	}

	public ChannelFuture close() {
		if (!closed) {
			disconnect().addListener(new ChannelFutureListener() {
				public void operationComplete(final ChannelFuture future) throws Exception {
					closed = true;
					if (future.isSuccess()) {
						closeFuture.setSuccess();
					} else {
						closeFuture.setFailure(future.getCause());
					}
				}
			}
			);
		}
		return getCloseFuture();
	}

	public ChannelFuture getCloseFuture() {
		return closeFuture;
	}

	public int getInterestOps() {
		return OP_READ;
	}

	public boolean isReadable() {
		return (getInterestOps() & OP_READ) != 0;
	}

	public boolean isWritable() {
		return (getInterestOps() & OP_WRITE) == 0;
	}

	public ChannelFuture setInterestOps(final int interestOps) {
		return new FailedChannelFuture(this, new UnsupportedOperationException());
	}

	public ChannelFuture setReadable(final boolean readable) {
		return new FailedChannelFuture(this, new UnsupportedOperationException());
	}

	public int compareTo(final Channel o) {

		if (this == o) {
			return 0;
		}
		if (o == null || getClass() != o.getClass()) {
			return 1;
		}

		final RXTXChannel that = (RXTXChannel) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return id.compareTo(that.id);
		}

		return 0;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final RXTXChannel that = (RXTXChannel) o;

		if (id != null ? !id.equals(that.id) : that.id != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
