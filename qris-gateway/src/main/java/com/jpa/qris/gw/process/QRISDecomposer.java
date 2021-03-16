package com.jpa.qris.gw.process;

import java.util.Map;
import org.fx3.emv.qr.DefaultCrcCalculator;
import org.fx3.emv.qr.QRDecomposer;

public class QRISDecomposer {

	private QRDecomposer dc;

	public Map<String, String> decodeQR(String payload) {
		dc = new QRDecomposer(payload);
		dc.setCrc(new DefaultCrcCalculator());
		Map<String, String> map = dc.doDecompose();
		return map;
	}

	public boolean validCRC(String crc) {
		return dc.isValidCRC(crc);
	}

}
