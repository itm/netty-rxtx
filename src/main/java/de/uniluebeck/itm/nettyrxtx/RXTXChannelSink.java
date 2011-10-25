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

import gnu.io.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.Executor;


public class RXTXChannelSink extends AbstractChannelSink {

	private static class WriteRunnable implements Runnable {

		private final DefaultChannelFuture future;

		private final RXTXChannelSink channelSink;

		private final ChannelBuffer message;

		public WriteRunnable(final DefaultChannelFuture future, final RXTXChannelSink channelSink,
							 final ChannelBuffer message) {
			this.future = future;
			this.channelSink = channelSink;
			this.message = message;
		}

		public void run() {
			try {

				channelSink.outputStream.write(message.array(), message.readerIndex(), message.readableBytes());
				channelSink.outputStream.flush();
				if (log.isTraceEnabled()) {
					log.trace("Wrote message to outputStream: {}", StringUtils.toHexString(message));
				}
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

			final CommPort commPort;
			try {

				final CommPortIdentifier cpi =
						CommPortIdentifier.getPortIdentifier(channelSink.remoteAddress.getDeviceAddress());
				commPort = cpi.open(this.getClass().getName(), 1000);

			} catch (NoSuchPortException e) {
				log.warn("No such port {}: {}", channelSink.remoteAddress.getDeviceAddress(), e.getMessage());
				throw e;
			} catch (PortInUseException e) {
				log.warn("Port {} in use: {}", channelSink.remoteAddress.getDeviceAddress(), e.getMessage());
				throw e;
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						commPort.close();
					} catch (Exception e) {
						log.warn("" + e, e);
					}
				}
			}
			);

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
					channelSink.channel.doSetClosed();
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
								log.trace("read from stream: {}", StringUtils.toHexString(channelBuffer));
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
			if (event.getMessage() instanceof ChannelBuffer) {
				executor.execute(
						new WriteRunnable((DefaultChannelFuture) future, this, (ChannelBuffer) event.getMessage())
				);
			} else {
				throw new IllegalArgumentException(
						"Only ChannelBuffer objects are supported to be written onto the RXTXChannelSink! "
								+ "Please check if the encoder pipeline is configured correctly."
				);
			}
		}
	}

}