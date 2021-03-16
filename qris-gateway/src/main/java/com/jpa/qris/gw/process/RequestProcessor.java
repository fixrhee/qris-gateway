package com.jpa.qris.gw.process;

import java.util.HashMap;

import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import com.jpa.qris.gw.channel.DefaultSocketConnector;

public class RequestProcessor {

	private MessageComposer composer;
	private DefaultSocketConnector connector;
	private SimpleStoreCache cache;
	private volatile Integer count = 0;

	public void sendCredit(String correlationID, String amount, String rrn, String merchantType, String convenienceFee,
			String acquiringID, String issuerID, String acceptorTID, String acceptorID, String nationalID,
			String customerPAN, String merchantPAN, String merchantName, String merchantCity, String countryCode,
			String customerName, String merchantCriteria, String postalCode, String additionalData) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeCreditRequest(stanCounter(), amount, rrn, merchantType, convenienceFee,
					acquiringID, issuerID, acceptorTID, acceptorID, nationalID, customerPAN, merchantPAN, merchantName,
					merchantCity, countryCode, customerName, merchantCriteria, postalCode, additionalData);
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendInquiryMPAN(String correlationID, String stan, String amount, String merchantType,
			String convenienceFee, String acquiringID, String issuerID, String acceptorTID, String acceptorID,
			String nationalID, String customerPAN, String merchantName, String merchantCity, String countryCode,
			String customerName, String merchantCriteria, String postalCode, String additionalData) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeInquiryMPAN(stanCounter(), amount, merchantType, convenienceFee,
					acquiringID, issuerID, acceptorTID, acceptorID, nationalID, customerPAN, merchantName, merchantCity,
					countryCode, customerName, merchantCriteria, postalCode, additionalData);
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendRefund(String correlationID, String stan, String rrn, String invoiceNo, String amount,
			String convenienceFee) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeRefundRequest(stan, rrn, invoiceNo, amount, convenienceFee);
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendLogon(String correlationID) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeLogon(stanCounter());
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendLogoff(String correlationID) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeLogoff(stanCounter());
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendCutover(String correlationID) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeCutover(stanCounter());
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendEcho(String correlationID) {
		cache.setCorrelationID(correlationID);
		try {
			byte[] payload = composer.composeEcho(stanCounter());
			if (payload != null) {
				connector.sendRequest(payload);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendIdleEcho(IoSession session) {
		try {
			byte[] payload = composer.composeEcho(stanCounter());
			if (payload != null) {
				connector.sendRequest(payload, session);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendInboundReply(HashMap<Integer, String> inbound, IoSession session) {
		try {
			byte[] payload = composer.composeInboundReply(inbound);
			if (payload != null) {
				connector.sendRequest(payload, session);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized String stanCounter() {
		try {
			if (count < 999999) {
				count = count + 1;
				return ISOUtil.zeropad(String.valueOf(count), 6);
			} else {
				count = 1;
				return ISOUtil.zeropad(String.valueOf(count), 6);
			}
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "000001";
		}
	}

	public void deleteCreditCache(String rrn) {
		composer.deleteCreditCache(rrn);
	}

	public MessageComposer getComposer() {
		return composer;
	}

	public void setComposer(MessageComposer composer) {
		this.composer = composer;
	}

	public DefaultSocketConnector getConnector() {
		return connector;
	}

	public void setConnector(DefaultSocketConnector connector) {
		this.connector = connector;
	}

	public SimpleStoreCache getCache() {
		return cache;
	}

	public void setCache(SimpleStoreCache cache) {
		this.cache = cache;
	}
}
