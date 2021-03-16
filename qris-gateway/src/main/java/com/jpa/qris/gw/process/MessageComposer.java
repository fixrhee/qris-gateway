package com.jpa.qris.gw.process;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MessageComposer {

	private ISOMsg targetComposer;
	private HazelcastInstance instance;

	public byte[] composeLogon(String stan) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0800");
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(48, "6011000112M003602");
			targetComposer.set(70, "001");
			targetComposer.dump(System.out, "LOGON-REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeLogoff(String stan) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0800");
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(48, "6011000112M003602");
			targetComposer.set(70, "002");
			targetComposer.dump(System.out, "LOGOFF-REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeEcho(String stan) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0800");
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(70, "301");
			targetComposer.dump(System.out, "ECHO-REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeInboundReply(HashMap<Integer, String> payload) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			Iterator<Entry<Integer, String>> it = payload.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>) it.next();
				if (pair.getKey() == 0) {
					targetComposer.setMTI(pair.getValue().substring(0, 2) + "10");
				} else {
					targetComposer.set(pair.getKey(), pair.getValue());
				}
				it.remove(); // avoids a ConcurrentModificationException
			}
			targetComposer.set(39, "00");
			targetComposer.dump(System.out, "INBOUND-RESPONSE");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeCutover(String stan) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0800");
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(15, GetSettlementDate(1, "ddMM"));
			targetComposer.set(70, "201");
			targetComposer.dump(System.out, "CUTOVER-REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeInquiryMPAN(String stan, String amount, String merchantType, String convenienceFee,
			String acquiringID, String issuerID, String acceptorTID, String acceptorID, String nationalID,
			String customerPAN, String merchantName, String merchantCity, String countryCode, String customerName,
			String merchantCriteria, String postalCode, String additionalData) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0200");
			targetComposer.set(2, "93600000" + nationalID);
			targetComposer.set(3, "376000");
			targetComposer.set(4, amount);
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(12, GetDate("HHmmss"));
			targetComposer.set(13, GetDate("MMdd"));
			targetComposer.set(15, GetSettlementDate(1, "ddMM"));
			targetComposer.set(17, GetDate("MMdd"));
			targetComposer.set(18, merchantType); // --> Merchant Type
			targetComposer.set(22, "011"); // --> Point of Service Entry Mode
			targetComposer.set(28, convenienceFee);
			targetComposer.set(32, acquiringID);
			targetComposer.set(33, "360004"); // --> Forwarding ID JALIN
			targetComposer.set(37, GetDate("HHmmss") + stan); // --> RRN
			targetComposer.set(38, issuerID); // Issuer ID
			targetComposer.set(41, acceptorTID); // --> Card Acceptor Terminal Identification
			targetComposer.set(42, acceptorID); // Card Acceptor ID
			targetComposer.set(43,
					StringUtils.rightPad(StringUtils.left(merchantName, 25), 25, ' ')
							+ StringUtils.rightPad(StringUtils.left(merchantCity, 13), 13, ' ')
							+ StringUtils.rightPad(StringUtils.left(countryCode, 2), 2, ' '));
			targetComposer.set(48, "PI04Q01"); // Example value of this data element: “PI04IQ01” for single MPAN,
												// “PI04IQ02” for multiple MPAN
			targetComposer.set(49, "360");
			targetComposer.set(100, nationalID); // --> NNS
			targetComposer.set(102, customerPAN); // --> CUSTOMER PAN
			targetComposer.dump(System.out, "INQUIRY-MPAN");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeCreditRequest(String stan, String amount, String rrn, String merchantType,
			String convenienceFee, String acquiringID, String authID, String acceptorTID, String acceptorID,
			String nationalID, String customerPAN, String merchantPAN, String merchantName, String merchantCity,
			String countryCode, String customerName, String merchantCriteria, String postalCode,
			String additionalData) {
		try {

			IMap<String, ISOMsg> creditMap = instance.getMap("CreditMap");
			String bit4 = new BigDecimal(amount).round(new MathContext(2))
					.add(new BigDecimal(convenienceFee).round(new MathContext(2))).setScale(2).toPlainString()
					.replace(".", "");
			System.out.println("amount :" + amount + " fee : " + convenienceFee + " sum : " + bit4);
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0200");
			targetComposer.set(2, merchantPAN); // Tag 26 - 51 subtag 01
			targetComposer.set(3, "266000");
			targetComposer.set(4, bit4);
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(12, GetDate("HHmmss"));
			targetComposer.set(13, GetDate("MMdd"));
			targetComposer.set(15, GetSettlementDate(1, "ddMM"));
			targetComposer.set(17, GetDate("MMdd"));
			targetComposer.set(18, merchantType); // --> Merchant Type QR Tag 52
			targetComposer.set(22, "011"); // --> Point of Service Entry Mode (manual entry and has PIN)
			targetComposer.set(28, "C" + convenienceFee); // Tag 56 or 57
			targetComposer.set(32, acquiringID); // First 8 digit from Tag 26 - 51 subtag 01
			targetComposer.set(33, "360004"); // --> Forwarding ID JALIN
			targetComposer.set(37, rrn); // --> RRN
			targetComposer.set(38, authID); // Authorization ID
			targetComposer.set(41, acceptorTID); // --> Card Acceptor Terminal Identification
			targetComposer.set(42, acceptorID); // Card Acceptor ID --> last 16 of Tag 62 subtag 07, if NA take Tag 26-51 sub tag 02
			targetComposer.set(43,
					StringUtils.rightPad(StringUtils.left(merchantName, 25), 25, ' ')
							+ StringUtils.rightPad(StringUtils.left(merchantCity, 13), 13, ' ')
							+ StringUtils.rightPad(StringUtils.left(countryCode, 2), 2, ' '));
			targetComposer.set(48,
					"PI04Q001" + "CD" + StringUtils.leftPad(String.valueOf(customerName.length()), 2, '0')
							+ StringUtils.left(customerName, 30) + "MC"
							+ StringUtils.leftPad(String.valueOf(merchantCriteria.length()), 2, '0')
							+ merchantCriteria);
			targetComposer.set(49, "360");
			targetComposer.set(57, "61" + StringUtils.leftPad(String.valueOf(postalCode.length()), 2, '0') + postalCode
					+ "62" + StringUtils.leftPad(String.valueOf(additionalData.length()), 2, '0') + additionalData);
			targetComposer.set(100, nationalID); // --> NNS
			targetComposer.set(102, customerPAN); // --> CUSTOMER PAN
			targetComposer.dump(System.out, "CREDIT-REQUEST");
			creditMap.put(stan, targetComposer);
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeCreditResponse(HashMap<Integer, String> payload, String rc, String invoiceNumber) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0210");
			Iterator<Entry<Integer, String>> it = payload.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>) it.next();
				targetComposer.set(pair.getKey(), pair.getValue());
			}
			it.remove(); // avoids a ConcurrentModificationException
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(39, rc);
			targetComposer.set(123, invoiceNumber);
			targetComposer.dump(System.out, "CREDIT-RESPONSE");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeCheckStatusRequest(String stan, String rrn) {
		IMap<String, ISOMsg> creditMap = instance.getMap("CreditMap");
		ISOMsg cache = creditMap.get(rrn);
		if (cache == null) {
			return null;
		}
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0200");
			targetComposer.set(2, cache.getString(2));
			targetComposer.set(3, "366000");
			targetComposer.set(4, cache.getString(4));
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(12, cache.getString(12)); //
			targetComposer.set(13, cache.getString(13)); //
			targetComposer.set(15, cache.getString(15)); //
			targetComposer.set(17, cache.getString(17));
			targetComposer.set(18, cache.getString(18)); // --> Merchant Type
			targetComposer.set(22, cache.getString(22)); // --> Point of Service Entry Mode
			targetComposer.set(28, cache.getString(28));
			targetComposer.set(32, cache.getString(32));
			targetComposer.set(33, cache.getString(33)); // --> Forwarding ID JALIN
			targetComposer.set(37, cache.getString(37)); // --> RRN
			targetComposer.set(38, cache.getString(38)); // Issuer ID
			targetComposer.set(41, cache.getString(41)); // --> Card Acceptor Terminal Identification
			targetComposer.set(42, cache.getString(42)); // Card Acceptor ID
			targetComposer.set(43, cache.getString(43));
			targetComposer.set(48, cache.getString(48));
			targetComposer.set(49, cache.getString(49));
			targetComposer.set(57, cache.getString(57));
			targetComposer.set(100, cache.getString(100)); // NNS
			targetComposer.set(102, cache.getString(102)); // --> CUSTOMER PAN
			targetComposer.dump(System.out, "CHECKSTATUS- REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeCheckStatusResponse(HashMap<Integer, String> payload, String rc, String invoiceNumber) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0210");
			Iterator<Entry<Integer, String>> it = payload.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>) it.next();
				targetComposer.set(pair.getKey(), pair.getValue());
			}
			it.remove(); // avoids a ConcurrentModificationException
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(39, rc);
			targetComposer.set(123, invoiceNumber);
			targetComposer.dump(System.out, "CHECKSTATUS-RESPONSE");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeRefundRequest(String stan, String rrn, String invoiceNumber, String amount,
			String convenienceFee) {
		try {
			IMap<String, ISOMsg> creditMap = instance.getMap("CreditMap");
			ISOMsg cache = creditMap.get(rrn);
			if (cache == null) {
				return null;
			}

			String bit4 = new BigDecimal(amount).round(new MathContext(2))
					.add(new BigDecimal(convenienceFee).round(new MathContext(2))).setScale(2).toPlainString()
					.replace(".", "");
			System.out.println("amount :" + amount + " fee : " + convenienceFee + " sum : " + bit4);

			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0200");
			targetComposer.set(2, GetTransmissionDate());
			targetComposer.set(3, "202060");
			targetComposer.set(4, bit4);
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(11, stan);
			targetComposer.set(12, GetDate("HHmmss"));
			targetComposer.set(13, GetDate("MMdd"));
			targetComposer.set(15, GetSettlementDate(1, "ddMM"));
			targetComposer.set(17, GetDate("MMdd"));
			targetComposer.set(18, cache.getString(18)); // --> Merchant Type
			targetComposer.set(22, cache.getString(22)); // --> Point of Service Entry Mode
			targetComposer.set(28, cache.getString(28));
			targetComposer.set(32, cache.getString(32));
			targetComposer.set(33, "360004"); // --> Forwarding ID JALIN
			targetComposer.set(37, GetDate("HHmmss") + stan); // --> RRN
			targetComposer.set(41, cache.getString(41)); // --> Card Acceptor Terminal Identification
			targetComposer.set(42, cache.getString(42)); // Card Acceptor ID
			targetComposer.set(43, cache.getString(43));
			targetComposer.set(48, cache.getString(48));
			targetComposer.set(49, cache.getString(49));
			targetComposer.set(100, cache.getString(100)); // --> NNS
			targetComposer.set(102, cache.getString(102)); // --> CUSTOMER PAN
			targetComposer.set(123, invoiceNumber);
			targetComposer.dump(System.out, "REFUND-REQUEST");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] composeRefundResponse(HashMap<Integer, String> payload, String rc, String authID,
			String invoiceNumber) {
		try {
			ISOPackager packA = new ISO87APackager();
			targetComposer = new ISOMsg();
			targetComposer.setPackager(packA);
			targetComposer.setMTI("0210");
			Iterator<Entry<Integer, String>> it = payload.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>) it.next();
				targetComposer.set(pair.getKey(), pair.getValue());
			}
			it.remove(); // avoids a ConcurrentModificationException
			targetComposer.set(7, GetTransmissionDate());
			targetComposer.set(38, authID);
			targetComposer.set(39, rc);
			targetComposer.set(123, invoiceNumber);
			targetComposer.dump(System.out, "REFUND-RESPONSE");
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] packMessage(HashMap<Integer, String> payload) {
		try {
			Iterator<Integer> enumKey = payload.keySet().iterator();
			while (enumKey.hasNext()) {
				Integer key = enumKey.next();
				targetComposer.set(key, payload.get(key));
			}
			return targetComposer.pack();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void deleteCreditCache(String rrn) {
		IMap<String, ISOMsg> creditMap = instance.getMap("CreditMap");
		creditMap.delete(rrn);
	}

	public static String GetTransmissionDate() {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
		return format.format(date);
	}

	public static String GetSettlementDate(int periode, String format) {
		GregorianCalendar calendar = new GregorianCalendar();
		Date now = calendar.getTime();
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		String formattedDate = fmt.format(now);
		calendar.add(GregorianCalendar.DAY_OF_MONTH, periode);
		Date tomorrow = calendar.getTime();
		formattedDate = fmt.format(tomorrow);
		return formattedDate;
	}

	public static String GetDate(String form) {
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(form);
		return format.format(date);
	}

	public HazelcastInstance getInstance() {
		return instance;
	}

	public void setInstance(HazelcastInstance instance) {
		this.instance = instance;
	}
}
