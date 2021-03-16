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
public interface DefaultSocketAcceptorInterface {

    public void MessageReceived(Object request, IoSession session);

    public void ConnectionOpened(IoSession session);

    public void ConnectionIdle(IoSession session);

    public void ConnectionClosed(IoSession session);

    public void ConnectionInterrupted(IoSession session);
    
}