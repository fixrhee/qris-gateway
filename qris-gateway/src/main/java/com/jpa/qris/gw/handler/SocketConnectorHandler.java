package com.jpa.qris.gw.handler;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.module.client.MuleClient;
import com.jpa.qris.gw.channel.DefaultSocketConnectorInterface;
import com.jpa.qris.gw.process.MessageDecomposer;
import com.jpa.qris.gw.process.RequestProcessor;
import com.jpa.qris.gw.process.SimpleStoreCache;

public class SocketConnectorHandler implements DefaultSocketConnectorInterface, MuleContextAware {

	private MuleContext context;
	private SimpleStoreCache cache;
	private MessageDecomposer decomposer;
	private RequestProcessor requestProcessor;
	private MuleClient client;
	private Logger logger = Logger.getLogger(SocketConnectorHandler.class);

	@Override
	public void setMuleContext(MuleContext context) {
		this.setContext(context);
		try {
			client = new MuleClient(context);
		} catch (MuleException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void GetResponse(IoSession session, Object response) {
		logger.info("[CONNECTOR RECEIVING MESSAGE With ID : " + session.getRemoteAddress().toString() + "]");
		byte[] obj = (byte[]) response;
		byte[] transplant = new byte[obj.length - 2];
		System.arraycopy(obj, 2, transplant, 0, obj.length - 2);
		HashMap<Integer, String> payload = decomposer.unpackMessage(transplant);
		try {
			MuleMessage mm = new DefaultMuleMessage(payload, context);
			mm.setCorrelationId(cache.getCorrelationID());
			mm.setOutboundProperty("IO_SESSION", session);
			client.dispatch("vm://connectorReceiver", mm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void MessageSent(IoSession session, Object request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ConnectionOpened(IoSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ConnectionIdle(IoSession session) {
		logger.info("[CONNECTOR IDLE : " + session.getRemoteAddress().toString() + "]");
		requestProcessor.sendIdleEcho(session);
	}

	@Override
	public void ConnectionClosed(IoSession session) {
		// TODO Auto-generated method stub
	}

	@Override
	public void ConnectionFailed(String session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ConnectionInterrupted(IoSession session) {
		// TODO Auto-generated method stub

	}

	public MuleContext getContext() {
		return context;
	}

	public void setContext(MuleContext context) {
		this.context = context;
	}

	public SimpleStoreCache getCache() {
		return cache;
	}

	public void setCache(SimpleStoreCache cache) {
		this.cache = cache;
	}

	public MessageDecomposer getDecomposer() {
		return decomposer;
	}

	public void setDecomposer(MessageDecomposer decomposer) {
		this.decomposer = decomposer;
	}

	public RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}

	public void setRequestProcessor(RequestProcessor requestProcessor) {
		this.requestProcessor = requestProcessor;
	}
}
