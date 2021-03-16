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
public class BinHeaderDecoder extends CumulativeProtocolDecoder {

    private static final Logger logger = Logger.getLogger(BinHeaderDecoder.class);
    protected int headerSize;
    protected int BlockLimit;
    protected int bufferTemp;

    public BinHeaderDecoder() {
        headerSize = 2;
        BlockLimit = 8192;
    }

    protected int getBodySize(IoBuffer buffer) {
        int position = buffer.position();
        byte byte0 = buffer.get();
        byte byte1 = buffer.get();
        buffer.position(position);
        return ((byte0 & 0xff) << 8 | byte1 & 0xff) & 0xffff;
    }

    @Override
    protected boolean doDecode(IoSession sess, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        if (in.prefixedDataAvailable(headerSize, BlockLimit)) {
            int len = getBodySize(in);
            int packet = in.limit();
            int body = len + headerSize;
            setBufferTemp(0);
            boolean nextHeader = true;
            while (nextHeader) {
                in.position(getBufferTemp());
                logger.info("DRAINING BLOCK...[Position=" + in.position() + ", Limit=" + (in.position() + body) + ", Size=" + body + ", Packet=" + packet + "]");
                byte message[] = new byte[body];
                in.get(message);
                out.write(message);
                setBufferTemp(in.position());
                in.limit(packet);
                if (in.prefixedDataAvailable(headerSize)) {
                    body = getBodySize(in) + headerSize;
                    nextHeader = true;
                } else {
                    if (in.limit() - in.position() != 0) {
                        logger.warn("INSUFFICIENT MESSAGE BLOCK ! BUFFER REMAINING... " + (in.limit() - in.position()) + " byte(s)");
                    }
                    nextHeader = false;
                    return false;
                }
            }
            return true;
        } else {
            logger.warn("INCOMPLETE MESSAGE...! READING NEXT...");
            return false;
        }
    }

    /**
     * @return the bufferTemp
     */
    public int getBufferTemp() {
        return bufferTemp;
    }

    /**
     * @param bufferTemp the bufferTemp to set
     */
    public void setBufferTemp(int bufferTemp) {
        this.bufferTemp = bufferTemp;
    }
}