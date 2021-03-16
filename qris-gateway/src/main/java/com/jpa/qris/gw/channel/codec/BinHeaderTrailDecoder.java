/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel.codec;

import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 *
 * @author Fikri Ilyas
 */
public class BinHeaderTrailDecoder extends CumulativeProtocolDecoder {

	private static Logger logger = Logger.getLogger(BinHeaderTrailDecoder.class);
	protected byte trailer[];
	private int headerSize;
	private int BufferTemp;
	private int BlockLimit;

	public BinHeaderTrailDecoder() {
		setBlockLimit(8192);
		setHeaderSize(2);
		trailer = new byte[] { 3 };
	}

	protected int getBodySize(IoSession session, IoBuffer buffer) {
		int position = buffer.position();
		byte byte0 = buffer.get();
		byte byte1 = buffer.get();
		buffer.position(position);
		return ((byte0 & 0xff) << 8 | byte1 & 0xff) & 0xffff;
	}

	protected boolean validBlock(IoSession session, IoBuffer buffer, byte[] block) {
		if (block.length > 0) {
			if (block.length > 600) {
				buffer.rewind();
				logger.fatal("OVERSIZE LIMIT... [Size=" + block.length + " byte(s)]... DROP CURRENT BLOCK !!! "
						+ buffer.getHexDump());
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	@SuppressWarnings("finally")
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

		if (in.prefixedDataAvailable(getHeaderSize(), getBlockLimit())) {
			try {
				setBufferTemp(0);
				int bodySize = getBodySize(session, in);
				int blockSize = getHeaderSize() + bodySize;
				byte block[] = new byte[blockSize];
				in.get(block);
				in.rewind();
				int start = in.position();
				int limit = in.limit();
				byte dummy[] = new byte[trailer.length];
				for (int i = start; i <= limit; i++) {
					in.position(i);
					in.get(dummy);
					for (int j = 0; j < trailer.length; j++) {
						if (dummy[j] == trailer[j]) {
							logger.info("TRAILER FOUND ! [Position=" + in.position() + ", Limit=" + in.limit() + "]");
							in.limit(in.position());
							in.position(getBufferTemp());
							logger.info("DRAINING BLOCK...[Position=" + in.position() + ", Trailer=" + in.limit()
									+ ", Limit=" + limit + ", Size =" + (in.limit() - in.position()) + "]");
							int grab = in.limit() - in.position();
							byte sr[] = new byte[grab];
							in.get(sr);
							if (validBlock(session, in, sr)) {
								out.write(sr);
							}
							setBufferTemp(in.limit());
							in.limit(limit);
						} else if (in.position() == in.limit() && dummy[j] != trailer[j]) {
							in.position(getBufferTemp());
							logger.warn("END OF BLOCK... NO TRAILER FOUND ! " + " [Position=" + getBufferTemp()
									+ ", Limit=" + in.limit() + "] DUMP : " + in.getHexDump());
							return false;
						}
					}
				}
				return true;
			} finally {
				int remaining = (in.limit() - in.position());
				if (remaining > 0) {
					logger.warn("INSUFFICIENT MESSAGE BLOCK ! BUFFER REMAINING... " + remaining + " byte(s)");
					return false;
				}
				return true;
			}
		}
		logger.warn("INCOMPLETE MESSAGE...! READING NEXT...");
		return false;
	}

	public int getBufferTemp() {
		return BufferTemp;
	}

	public void setBufferTemp(int BufferTemp) {
		this.BufferTemp = BufferTemp;
	}

	public int getBlockLimit() {
		return BlockLimit;
	}

	public void setBlockLimit(int BlockLimit) {
		this.BlockLimit = BlockLimit;
	}

	public int getHeaderSize() {
		return headerSize;
	}

	public void setHeaderSize(int headerSize) {
		this.headerSize = headerSize;
	}
}