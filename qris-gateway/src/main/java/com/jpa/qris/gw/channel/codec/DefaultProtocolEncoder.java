/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * 
 * @author Fikri Ilyas
 */
class DefaultProtocolEncoder implements ProtocolEncoder {

	public DefaultProtocolEncoder() {
	}

	public void dispose(IoSession iosession) throws Exception {
	}

	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput output) throws Exception {
		byte packet[] = (byte[]) message;
		IoBuffer buffer = IoBuffer.wrap(packet);
		output.write(buffer);
	}
}