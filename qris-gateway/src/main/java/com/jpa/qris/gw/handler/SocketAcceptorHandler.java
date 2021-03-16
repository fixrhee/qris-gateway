package com.jpa.qris.gw.handler;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.ISO87APackager;
import com.jpa.qris.gw.channel.DefaultSocketAcceptorInterface;
import com.jpa.qris.gw.process.SimpleStoreCache;

public class SocketAcceptorHandler implements DefaultSocketAcceptorInterface {

	private SimpleStoreCache cache;
	private Logger logger = Logger.getLogger(SocketAcceptorHandler.class);

	@Override
	public void ConnectionClosed(IoSession arg0) {
	}

	@Override
	public void ConnectionIdle(IoSession arg0) {
		logger.info("[" + arg0.getRemoteAddress() + " Connection IDLE . . . ]");
	}

	@Override
	public void ConnectionInterrupted(IoSession arg0) {
		logger.info("[Connection INTERRUPTED : " + arg0.getRemoteAddress().toString() + "]");
	}

	@Override
	public void ConnectionOpened(IoSession arg0) {
		cache.setSess(arg0);
	}

	@Override
	public void MessageReceived(Object arg0, IoSession arg1) {
		logger.info("[ACCEPTOR RECEIVING MESSAGE With ID : " + arg1.getRemoteAddress().toString() + "]");
		try {
			byte[] obj = (byte[]) arg0;
			byte[] transplant = new byte[obj.length - 2];
			System.arraycopy(obj, 2, transplant, 0, obj.length - 2);

			ISOPackager decomposerPackA = new ISO87APackager();
			ISOMsg sourceDecomposer = new ISOMsg();
			sourceDecomposer.setPackager(decomposerPackA);
			sourceDecomposer.unpack(transplant);

			ISOPackager composerPackA = new ISO87APackager();
			ISOMsg targetComposer = new ISOMsg();
			targetComposer.setPackager(composerPackA);

			if (sourceDecomposer.getMTI().substring(2, 4).equalsIgnoreCase("10")) {
				logger.info("[SIMULATOR RECEIVING MESSAGE]");
				sourceDecomposer.dump(System.out, "SIM-REPLY-IN");
				return;
			}
			@SuppressWarnings("unchecked")
			Map<Integer, String> table = sourceDecomposer.getChildren();
			Iterator<Integer> enumKey = table.keySet().iterator();
			while (enumKey.hasNext()) {
				Integer key = enumKey.next();
				targetComposer.set(key, sourceDecomposer.getString(key));
				targetComposer.setMTI(sourceDecomposer.getMTI().substring(0, 2) + "10");
				targetComposer.set(39, "00");
			}

			byte[] isoresp = targetComposer.pack();
			byte[] header = new byte[2];
			short n = (new Integer((header.length + isoresp.length) - 2)).shortValue();
			header[0] = (byte) (n >> 8 & 0xff);
			header[1] = (byte) (n & 0xff);
			String payload = new String(header) + new String(isoresp);
			arg1.write(payload.getBytes());
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendRequest(byte[] request) throws InterruptedException {
		byte[] header = new byte[2];
		short n = (new Integer((header.length + request.length) - 2)).shortValue();
		header[0] = (byte) (n >> 8 & 0xff);
		header[1] = (byte) (n & 0xff);

		String payload = new String(header) + new String(request);
		if (cache.getSess() != null) {
			cache.getSess().write(payload.getBytes());
		}
	}

	public SimpleStoreCache getCache() {
		return cache;
	}

	public void setCache(SimpleStoreCache cache) {
		this.cache = cache;
	}
}
