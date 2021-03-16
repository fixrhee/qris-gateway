/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.channel.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 *
 * @author Fikri Ilyas
 */
public class BinHeaderProtocolCodecFactory implements ProtocolCodecFactory {

    public BinHeaderProtocolCodecFactory() {
    }

    public ProtocolEncoder getEncoder(IoSession is) throws Exception {
        return new DefaultProtocolEncoder();
    }

    public ProtocolDecoder getDecoder(IoSession is) throws Exception {
        return new BinHeaderDecoder();
    }
}