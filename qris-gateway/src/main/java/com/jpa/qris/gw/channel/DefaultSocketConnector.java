/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel;

import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 * 
 * @author Fikri Ilyas
 */
public final class DefaultSocketConnector extends IoHandlerAdapter {

	private String host;
	private int port;
	private NioSocketConnector connector;
	private DefaultSocketConnectorInterface handlerInterface;
	private IoSession session;
	private int timeoutValue = 2500;
	private boolean keepAlive;
	private boolean reconnect;
	private boolean debugMode;
	private int rate = 10;
	private String key;
	private int reconnectTime = 5000;
	private ProtocolCodecFactory codecFactory;
	private static Logger logger = Logger.getLogger(DefaultSocketConnector.class);

	public DefaultSocketConnector(ProtocolCodecFactory codecFactory, String host, int port, boolean debugMode,
			boolean reconnect, boolean keepAlive, int rate, int reconnectTime, int timeoutValue)
			throws InterruptedException {
		this(codecFactory, keepAlive, rate);
		this.debugMode = debugMode;
		this.host = host;
		this.port = port;
		this.reconnectTime = reconnectTime;
		this.timeoutValue = timeoutValue;
		this.reconnect = reconnect;
		logger.info("[Socket Connector Host : " + host + "]");
		logger.info("[Socket Connector Port : " + port + "]");
		logger.info("[Socket Connector Connection Timeout : " + getTimeoutValue() + "]");
		logger.info("[Socket Connector Reconnection Mode : " + reconnect + "]");
		logger.info("[Socket Connector Keep Alive Mode : " + keepAlive + "]");
		logger.info("[Socket Connector Keep Alive Rate : " + rate + "]");
		logger.info("[Socket Connector Reconnect time : " + getReconnectTime() + "]");
	}

	public DefaultSocketConnector(ProtocolCodecFactory codecFactory, boolean keepAlive, int rate) {
		try {
			logger.info("[Initializing Default Socket Connector . . . ]");
			connector = new NioSocketConnector();
			connector.setConnectTimeoutMillis(getTimeoutValue());
			connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			connector.setHandler(this);
			this.keepAlive = keepAlive;
			this.rate = rate;
		} catch (IllegalArgumentException e) {
			logger.warn("[CONNECTOR to /" + host + ":" + port + " Failed !!! --> " + e.getCause() + "]");
		}
	}

	public boolean isConnect(String host, int port) throws InterruptedException {
		this.setHost(host);
		this.setPort(port);
		session = null;
		return create();
	}

	public boolean isClosed() {
		if (session == null) {
			logger.info("[CONNECTION Initialized . . . ]");
			return true;
		}
		return false;
	}

	public boolean isClosed(String key, String id) {
		this.key = id;
		if (session == null) {
			logger.info("[CONNECTION Initialized . . . " + key + "]");
			return true;
		}
		if (!key.matches(session.getRemoteAddress().toString())) {
			logger.info("[INITIALIZING New Connection To : " + key + "]");
			return true;
		}
		return false;
	}

	public boolean create() throws InterruptedException {
		try {
			ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
			connectFuture.awaitUninterruptibly();
			if (connectFuture.isConnected()) {
				session = connectFuture.getSession();
				logger.info("[CREATED socket session: <R:" + session.getRemoteAddress().toString() + ", L:"
						+ session.getLocalAddress().toString() + ">]");
				setIoSession(session);
				return true;
			} else {
				key = ("/" + this.getHost() + ":" + this.getPort());
				getHandlerInterface().ConnectionFailed(key);
				logger.warn("[CONNECTION to /" + host + ":" + port + " Failed !!!]");
				if (reconnect) {
					reConnection();
				}
				return false;
			}
		} catch (RuntimeIoException e) {
			e.printStackTrace();
			Thread.sleep(5000);
			return false;
		}
	}

	public void initConnection() throws InterruptedException {
		this.create();
	}

	public boolean close(IoSession session) {
		if (session == null) {
			return false;
		}
		CloseFuture closeFuture = session.getCloseFuture().awaitUninterruptibly();
		connector.dispose();
		if (closeFuture.isClosed()) {
			logger.info("[CLOSED socket session: " + session.getRemoteAddress().toString() + "]");
			getHandlerInterface().ConnectionClosed(session);
			closeFuture.setClosed();
			session = null;
			return true;
		} else {
			return false;
		}
	}

	public void destroy() {
		this.close(session);
	}

	public void reConnection() throws InterruptedException {
		logger.info("[Attempting to Reconnect . . . ]");
		Thread.sleep(getReconnectTime());
		this.create();
	}

	public void setIoSession(IoSession ioSession) {
		this.session = ioSession;
		return;

	}

	public void sendRequest(byte[] request) throws InterruptedException {
		byte[] header = new byte[2];
		short n = (new Integer((header.length + request.length) - 2)).shortValue();
		header[0] = (byte) (n >> 8 & 0xff);
		header[1] = (byte) (n & 0xff);

		String payload = new String(header) + new String(request);
		if (session == null) {
			this.create();
			session.write(payload.getBytes());
		} else {
			session.write(payload.getBytes());
		}
	}

	public void sendRequest(byte[] request, IoSession sess) throws InterruptedException {
		byte[] header = new byte[2];
		short n = (new Integer((header.length + request.length) - 2)).shortValue();
		header[0] = (byte) (n >> 8 & 0xff);
		header[1] = (byte) (n & 0xff);

		String payload = new String(header) + new String(request);
		sess.write(payload.getBytes());
	}

	@Override
	public void sessionOpened(IoSession session) {
		logger.info("[OPENED !!! " + session.toString() + "]");
		getHandlerInterface().ConnectionOpened(session);
		if (isKeepAlive()) {
			session.getConfig().setIdleTime(IdleStatus.READER_IDLE, rate);
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws InterruptedException {
		logger.warn("[Target Host is Closing Connection . . .]");
		if (reconnect) {
			reConnection();
		}
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		if (session.isIdle(IdleStatus.READER_IDLE)) {
			getHandlerInterface().ConnectionIdle(session);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		if (debugMode) {
			cause.printStackTrace();
		}
		// this.close(session);
		getHandlerInterface().ConnectionInterrupted(session);
	}

	@Override
	public void messageSent(IoSession session, Object request) throws Exception {
		getHandlerInterface().MessageSent(session, request);
	}

	@Override
	public void messageReceived(IoSession session, Object response) {
		getHandlerInterface().GetResponse(session, response);
	}

	public ProtocolCodecFactory getCodecFactory() {
		return codecFactory;
	}

	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	public int getTimeoutValue() {
		return timeoutValue;
	}

	public void setTimeoutValue(int timeoutValue) {
		this.timeoutValue = timeoutValue;
	}

	/**
	 * @return the handlerInterface
	 */
	public DefaultSocketConnectorInterface getHandlerInterface() {
		return handlerInterface;
	}

	/**
	 * @param handlerInterface
	 *            the handlerInterface to set
	 */
	public void setHandlerInterface(DefaultSocketConnectorInterface handlerInterface) {
		this.handlerInterface = handlerInterface;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the keepAlive
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * @param keepAlive
	 *            the keepAlive to set
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}

	/**
	 * @param rate
	 *            the rate to set
	 */
	public void setRate(int rate) {
		this.rate = rate;
	}

	/**
	 * @return the reconnectTime
	 */
	public int getReconnectTime() {
		return reconnectTime;
	}

	/**
	 * @param reconnectTime
	 *            the reconnectTime to set
	 */
	public void setReconnectTime(int reconnectTime) {
		this.reconnectTime = reconnectTime;
	}
}