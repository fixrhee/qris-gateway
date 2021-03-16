/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel;

import org.apache.mina.core.session.IoSession;

/**
 * 
 * @author Fikri Ilyas
 */
public interface DefaultSocketConnectorInterface {

	public void GetResponse(IoSession session, Object response);

	public void MessageSent(IoSession session, Object request);

	public void ConnectionOpened(IoSession session);

	public void ConnectionIdle(IoSession session);

	public void ConnectionClosed(IoSession session);

	public void ConnectionFailed(String session);

	public void ConnectionInterrupted(IoSession session);
}