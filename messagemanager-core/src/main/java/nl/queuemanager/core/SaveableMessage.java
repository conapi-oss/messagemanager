package nl.queuemanager.core;

import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

public interface SaveableMessage {

    public static String getFileExtension() {
        return null;
    }

    public Message readFromFile(File file) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, JMSException ;

    public void saveToFile(Message message, File file) throws ParserConfigurationException, IOException, TransformerFactoryConfigurationError, TransformerException, JMSException ;

}
