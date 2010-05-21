package nl.queuemanager.ui.message;

import java.io.IOException;
import java.io.StringReader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.queuemanager.core.util.NullEntityResolver;
import nl.queuemanager.jms.JMSXMLMessage;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class TextMessageContentViewer extends XmlContentViewer<Message> implements MessageContentViewer {
	
	@Override
	public String getContent(Message message) {
		try {
			// Try to parse the message as Xml
			if(message instanceof JMSXMLMessage) {
				// Get the Document from the message
				try {
					Document d = ((JMSXMLMessage)message).getDocument();
					return formatXml(d);
				} catch (JMSException e) {
					// Getting the Document failed, perhaps it wasn't XML after all?
					return ((TextMessage)message).getText();
				}				
			}

			// The message wasn't an XMLMessage, try to parse as XML anyway
			try {
				String text = ((TextMessage)message).getText();
				InputSource is = new InputSource(new StringReader(text != null?text:""));
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				db.setEntityResolver(new NullEntityResolver());
				Document doc = db.parse(is);
				
				return formatXml(doc);
			} catch (SAXException e) {
			} catch (IOException e) {
			} catch (ParserConfigurationException e) {
			}
			
			// Parsing the Xml failed, just return the text content
			return ((TextMessage)message).getText();
		} catch (JMSException e) {
			return "Exception while retrieving the contents of the message.\n" +
				e.toString();
		}
	}

	public boolean supports(Message message) {
		return JMSXMLMessage.class.isAssignableFrom(message.getClass())
			|| TextMessage.class.isAssignableFrom(message.getClass());
	}

	public String getDescription(Message message) {
		if(message instanceof JMSXMLMessage)
			return "Xml";
		return "Text";
	}		
}
