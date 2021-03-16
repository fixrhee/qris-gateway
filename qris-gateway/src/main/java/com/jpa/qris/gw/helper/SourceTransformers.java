/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpa.qris.gw.helper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Fikri Ilyas
 */
public class SourceTransformers {

    private TransformerFactory factory;
    private Transformer transformer;
    private StreamResult streamResult;

    public SourceTransformers() {
        try {
            factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "XML");
            OutputStream out = new ByteArrayOutputStream();
            streamResult = new StreamResult();
            streamResult.setOutputStream(out);
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    public String sourceToXML(Source s) {
        try {
            transformer.transform(s, streamResult);
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
        return streamResult.getOutputStream().toString();
    }
}
