package nl.queuemanager.ui.message;

import java.io.IOException;
import java.io.StringReader;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.queuemanager.core.jms.JMSPart;
import nl.queuemanager.core.jms.JMSXMLMessage;
import nl.queuemanager.core.util.NullEntityResolver;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class XmlMessagePartContentViewer extends XmlContentViewer<JMSPart> implements MessagePartContentViewer {
	
	@Override
	protected String getContent(JMSPart part) {
		// FIXME Prevent this code duplication with XmlMessageContentViewer
		Message message = (Message)part.getContent();
		
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
	
	public boolean supports(JMSPart part) {
		String contentType = part.getContentType();
		
		return contentType.equals("application/x-sonicmq-textmessage")
			|| contentType.equals("application/x-sonicmq-xmlmessage");
	}
	
}
