package com.jpa.qris.gw.process;

import org.apache.mina.core.session.IoSession;

public class SimpleStoreCache {

	private IoSession sess;
	private String correlationID;

	public IoSession getSess() {
		return sess;
	}

	public void setSess(IoSession sess) {
		this.sess = sess;
	}

	public String getCorrelationID() {
		return correlationID;
	}

	public void setCorrelationID(String correlationID) {
		this.correlationID = correlationID;
	}

}
