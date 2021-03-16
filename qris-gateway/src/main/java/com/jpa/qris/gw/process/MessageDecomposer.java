package com.jpa.qris.gw.process;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;

import com.jpa.qris.gw.channel.DefaultSocketConnector;

public class MessageDecomposer {

	private DefaultSocketConnector connector;

	public HashMap<Integer, String> unpackMessage(byte[] payload) {
		try {
			ISOPackager packA = new ISO87APackager();
			ISOMsg sourceDecomposer = new ISOMsg();
			sourceDecomposer.setPackager(packA);
			sourceDecomposer.unpack(payload);
			sourceDecomposer.dump(System.out, "CONNECTOR-IN");

			@SuppressWarnings("unchecked")
			Map<Integer, String> table = sourceDecomposer.getChildren();
			Iterator<Integer> enumKey = table.keySet().iterator();
			HashMap<Integer, String> message = new HashMap<Integer, String>();
			while (enumKey.hasNext()) {
				Integer key = enumKey.next();
				if (sourceDecomposer.getString(key) != null) {
					message.put(key, sourceDecomposer.getString(key));
				}
			}
			return message;
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public DefaultSocketConnector getConnector() {
		return connector;
	}

	public void setConnector(DefaultSocketConnector connector) {
		this.connector = connector;
	}
}
