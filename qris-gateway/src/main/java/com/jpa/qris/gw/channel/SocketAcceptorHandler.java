/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author Fikri Ilyas
 */
public class SocketAcceptorHandler extends IoHandlerAdapter {

	private boolean debugMode;
	private boolean singleSessionConnection;
	private boolean idle;
	private boolean closeWhenIdle;
	private boolean closeOnDuplicate;
	private Integer idleTIme = 10;
	private HashMap<String, IoSession> mapSession = new HashMap<String, IoSession>();
	private DefaultSocketAcceptorInterface receiver;
	private static final Logger logger = Logger.getLogger(SocketAcceptorHandler.class);

	public SocketAcceptorHandler(boolean validate, boolean singleSessionConnection, boolean idle, boolean closeWhenIdle,
			boolean debugMode, String allowedAddresses, Integer idleTime, DefaultSocketAcceptorInterface receiver) {
		this.receiver = receiver;
		this.debugMode = debugMode;
		this.singleSessionConnection = singleSessionConnection;
		this.idle = idle;
		this.idleTIme = idleTime;
		this.closeWhenIdle = closeWhenIdle;
		logger.info("[Socket Acceptor Validate Client : " + validate + "]");
		logger.info("[Socket Acceptor Debug Mode : " + debugMode + "]");
		logger.info("[Socket Acceptor Idle Mode : " + this.idle + "]");
		logger.info("[Socket Acceptor Close Connection When Idle Mode : " + this.closeWhenIdle + "]");
		if (idle) {
			logger.info("[Socket Acceptor Idle Time : " + this.idleTIme + " sec]");
		}
		logger.info("[Single Session Host Mode : " + this.singleSessionConnection + "]");
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		if (debugMode) {
			cause.printStackTrace();
		}
		receiver.ConnectionInterrupted(session);
		super.sessionClosed(session);
		logger.info("[CONNECTION reset by : " + session.getRemoteAddress().toString() + "]");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		String remoteSession = session.getRemoteAddress().toString();
		String remoteIP = remoteSession.substring(0, remoteSession.indexOf(":"));

		if (singleSessionConnection) {
			if (mapSession.isEmpty()) {
				logger.info("[Connection Map Empty > Creating New Session From: " + remoteIP + "]");
				mapSession.put(remoteIP, session);
			} else {
				if (mapSession.containsKey(remoteIP)) {
					IoSession prevSession = mapSession.get(remoteIP);
					logger.info("[Terminating Previous Session: " + prevSession.getRemoteAddress().toString() + "]");
					prevSession.closeNow();
					mapSession.put(remoteIP, session);
					closeOnDuplicate = true;
				} else {
					logger.info("[Creating Another Session From: " + remoteIP + "]");
					mapSession.put(remoteIP, session);
				}
			}
		}
		super.sessionCreated(session);
		logger.info("[CREATED connection from: " + session.getRemoteAddress().toString() + "]");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		receiver.ConnectionOpened(session);
		logger.info("[OPENED connection from: " + session.getRemoteAddress().toString() + "]");
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// byte block[] = (byte[]) message;
		// logger.info("[" + Thread.currentThread().getId() + "] " +
		// block.length + " bytes received from: " +
		// session.getRemoteAddress().toString());
		receiver.MessageReceived(message, session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		try {
			if (!closeWhenIdle) {
				if (status == IdleStatus.BOTH_IDLE) {
					receiver.ConnectionIdle(session);
				}
			} else {
				logger.info("[Closing IDLE Connection : " + session.getRemoteAddress() + "]");
				super.sessionClosed(session);
				session.closeNow();
			}
		} catch (Exception ex) {
			logger.info("[IDLE Connection Problem: " + session.getRemoteAddress() + "!" + "]");
		}

	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		String remoteSession = session.getRemoteAddress().toString();
		String remoteIP = remoteSession.substring(0, remoteSession.indexOf(":"));
		receiver.ConnectionClosed(session);
		super.sessionClosed(session);
		if (singleSessionConnection) {
			if (!closeOnDuplicate) {
				mapSession.remove(remoteIP);
			}
			logger.info("[Flushing Session Connection : " + remoteSession + ", Connection Remaining Before Closing : "
					+ mapSession.size() + "]");
			closeOnDuplicate = false;
		} else {
			mapSession.remove(remoteIP);
		}
		logger.info("[CLOSED connection from: " + session.getRemoteAddress().toString() + "]");
	}
}