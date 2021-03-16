package com.jpa.qris.gw.channel;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.jpa.qris.gw.channel.filter.DefaultSocketWhitelistFilter;

/**
 * 
 * @author Fikri Ilyas
 */
public class DefaultSocketAcceptor {

	private int portNumber;
	private boolean validate;
	private String allowedAddresses;
	private DefaultSocketAcceptorInterface receiver;
	private ProtocolCodecFactory codecFactory;
	private Integer idleTime = 10;
	private boolean idle;
	private boolean closeWhenIdle;
	private IoAcceptor acceptor;
	private InetSocketAddress socket;
	private boolean debugMode;
	private boolean singleSessionConnection;
	private static final Logger logger = Logger
			.getLogger(DefaultSocketAcceptor.class);

	public void Initialize() throws IOException {
		logger.info("[Initializing Default Socket Acceptor Channel...]");
		acceptor = new NioSocketAcceptor();

		if (validate) {
			if (allowedAddresses.contains(",")) {
				String[] allowedConnection = allowedAddresses.split(",");
				DefaultSocketWhitelistFilter whitelistFilter = new DefaultSocketWhitelistFilter();
				InetAddress[] address = new InetAddress[allowedConnection.length];
				for (int i = 0; i < allowedConnection.length; i++) {
					address[i] = InetAddress.getByName(allowedConnection[i]);
					logger.info("[Loading allowed addresses : "
							+ allowedConnection[i] + "]");
				}
				whitelistFilter.setWhitelist(address);
				acceptor.getFilterChain()
						.addFirst("whitelist", whitelistFilter);
			} else {
				DefaultSocketWhitelistFilter whitelistFilter = new DefaultSocketWhitelistFilter();
				InetAddress[] address = new InetAddress[1];
				address[0] = InetAddress.getByName(allowedAddresses);
				logger.info("[Loading single address : " + allowedAddresses
						+ "]");
				whitelistFilter.setWhitelist(address);
				acceptor.getFilterChain()
						.addFirst("whitelist", whitelistFilter);
			}
		}

		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(getCodecFactory()));
		acceptor.setHandler(new SocketAcceptorHandler(isValidate(),
				issingleSessionConnection(), isIdle(), isCloseWhenIdle(),
				isDebugMode(), getAllowedAddresses(), getIdleTime(),
				getReceiver()));
		acceptor.getSessionConfig().setReadBufferSize(2048);
		if (idle) {
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
					getIdleTime());
		}
		acceptor.bind(new InetSocketAddress(getPortNumber()));
		logger.info("[Socket Acceptor is Listening on Port : " + portNumber
				+ "]");
	}

	public void stopServer() {
		logger.info("[Closing Socket on Port : " + portNumber + "]");
		getAcceptor().unbind(getSocket());
		getAcceptor().dispose();
	}

	/**
	 * @return the PORT
	 */
	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * @return the validate
	 */
	public boolean isValidate() {
		return validate;
	}

	/**
	 * @param validate
	 *            the validate to set
	 */
	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	/**
	 * @return the allowedAddresses
	 */
	public String getAllowedAddresses() {
		return allowedAddresses;
	}

	/**
	 * @param allowedAddresses
	 *            the allowedAddresses to set
	 */
	public void setAllowedAddresses(String allowedAddresses) {
		this.allowedAddresses = allowedAddresses;
	}

	/**
	 * @return the codecFactory
	 */
	public ProtocolCodecFactory getCodecFactory() {
		return codecFactory;
	}

	/**
	 * @param codecFactory
	 *            the codecFactory to set
	 */
	public void setCodecFactory(ProtocolCodecFactory codecFactory) {
		this.codecFactory = codecFactory;
	}

	/**
	 * @return the receiver
	 */
	public DefaultSocketAcceptorInterface getReceiver() {
		return receiver;
	}

	/**
	 * @param receiver
	 *            the receiver to set
	 */
	public void setReceiver(DefaultSocketAcceptorInterface receiver) {
		this.receiver = receiver;
	}

	/**
	 * @return the debugMode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * @param debugMode
	 *            the debugMode to set
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * @return the singleSessionConnection
	 */
	public boolean issingleSessionConnection() {
		return singleSessionConnection;
	}

	/**
	 * @param singleSessionConnection
	 *            the singleSessionConnection to set
	 */
	public void setsingleSessionConnection(boolean singleSessionConnection) {
		this.singleSessionConnection = singleSessionConnection;
	}

	/**
	 * @return the idleTime
	 */
	public Integer getIdleTime() {
		return idleTime;
	}

	/**
	 * @param idleTime
	 *            the idleTime to set
	 */
	public void setIdleTime(Integer idleTime) {
		this.idleTime = idleTime;
	}

	/**
	 * @return the idle
	 */
	public boolean isIdle() {
		return idle;
	}

	/**
	 * @param idle
	 *            the idle to set
	 */
	public void setIdle(boolean idle) {
		this.idle = idle;
	}

	/**
	 * @return the acceptor
	 */
	public IoAcceptor getAcceptor() {
		return acceptor;
	}

	/**
	 * @param acceptor
	 *            the acceptor to set
	 */
	public void setAcceptor(IoAcceptor acceptor) {
		this.acceptor = acceptor;
	}

	/**
	 * @return the socket
	 */
	public InetSocketAddress getSocket() {
		return socket;
	}

	/**
	 * @param socket
	 *            the socket to set
	 */
	public void setSocket(InetSocketAddress socket) {
		this.socket = socket;
	}

	/**
	 * @return the closeWhenIdle
	 */
	public boolean isCloseWhenIdle() {
		return closeWhenIdle;
	}

	/**
	 * @param closeWhenIdle
	 *            the closeWhenIdle to set
	 */
	public void setCloseWhenIdle(boolean closeWhenIdle) {
		this.closeWhenIdle = closeWhenIdle;
	}
}