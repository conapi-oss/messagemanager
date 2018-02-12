package nl.queuemanager.solace;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

import lombok.extern.java.Log;

@Log
class SmfSempConnection implements SempConnection {

	private final SmfConnectionDescriptor connectionDescriptor;
	private final Session session;

	/**
	 * Create a new 
	 * @param domain
	 */
	public SmfSempConnection(SmfConnectionDescriptor connectionDescriptor, Session session) {
		this.connectionDescriptor = connectionDescriptor;
		this.session = session;
	}
	
	@Override
	public void showMessageVPN(String vpnName, SempResponseCallback responseHandler) throws SempException {
		performShowRequest(SempRequests.showMessageVPN(vpnName), responseHandler);
	}
	
	@Override
	public void showMessageSpoolByVPN(String vpnName, SempResponseCallback responseHandler) throws SempException {
		performShowRequest(SempRequests.showMessageSpoolByVpn(vpnName), responseHandler);
	}
	
	@Override
	public void performShowRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException {
		try {
			performSEMPRequest(showCommandTopic(), request, sempResponseHandler);
		} catch (JMSException e) {
			throw new SempException("JMSException while sending SEMP request", e);
		}
	}
	
	@Override
	public void performAdminRequest(byte[] request, SempResponseCallback sempResponseHandler) throws SempException {
		try {
			performSEMPRequest(adminCommandTopic(), request, sempResponseHandler);
		} catch (JMSException e) {
			throw new SempException("JMSException while sending SEMP request", e);
		}
	}
	
	protected void performSEMPRequest(Topic topic, byte[] request) throws SempException {		
		performSEMPRequest(topic, request, SempResponseCallback.NULL_HANDLER);
	}
	
	protected void performSEMPRequest(Topic topic, byte[] request, SempResponseCallback handler) throws SempException {		
		boolean haveMoreCookie;
		do try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			XPath xp = XPathFactory.newInstance().newXPath();
			
			haveMoreCookie = false;
			
			// Send the request
			log.finer("Sending request to " + topic + ":\n" + new String(request));
			
			// TODO Replace by custom requestor with timeouts and such
			Topic replyTopic = session.createTemporaryTopic();
			MessageConsumer c = session.createConsumer(replyTopic);
			
			MessageProducer p = session.createProducer(showCommandTopic());
			p.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			BytesMessage message = session.createBytesMessage();
			message.writeBytes(request);
			message.setJMSReplyTo(replyTopic);
			p.send(message);
			p.close();

			// FIXME Wrap this in some requestor class that makes this easier
			BytesMessage response = (BytesMessage) c.receive(5000);
			c.close();
			
			if(response == null) {
				throw new SempException("No response received from applicance, check the 'Appliance name' parameter");
			}
			
			// Get the response as a byte array
			byte[] responseBytes = new byte[(int)response.getBodyLength()];
			response.readBytes(responseBytes); // FIXME Do some more checking here
			log.finer("Got response:\n" + new String(responseBytes));
			
			// Interpret the response
			try {
				Document doc = db.parse(new InputSource(new ByteArrayInputStream(responseBytes)));

				// Check the execute-result to see if it's "ok"
				String executeResult = xp.evaluate("/rpc-reply/execute-result/@code", doc);
				boolean success = "ok".equals(executeResult);

				if(!success) {
					// Find out if this is a permission problem
					String permissionError = xp.evaluate("/rpc-reply/permission-error", doc);
					if(!Strings.isNullOrEmpty(permissionError)) {
						throw new SempException(403, "Forbidden", responseBytes, permissionError);
					} else {
						// Some other kind of error
						throw new SempException(responseBytes, "Appliance indicated request failure");
					}
				}
					
				// Check if there is a more-cookie and set up for the next request, if any
				Element moreCookie = (Element) xp.evaluate("/rpc-reply/more-cookie/rpc", doc, XPathConstants.NODE);
				if(moreCookie != null) {
					try {
						request = XmlUtils.serialize(moreCookie);
					} catch (TransformerException e) {
						throw new SempException(responseBytes, "Unable to serialize more-cookie from request", e);
					}
					haveMoreCookie = true;
				}
				
				// Handle the response
				try {
					handler.handle(doc);
				} catch (Exception e) {
					throw new SempException("SEMPResponseCallback threw Exception", e);
				}
			} catch (SAXException | XPathExpressionException e) {
				throw new SempException(responseBytes, "XML handling exception", e);
			}
		} catch (IOException | ParserConfigurationException e) {
			throw new SempException("Exception during SEMP request", e);
		} catch (JMSException e) {
			throw new SempException("JMSException during SEMP request", e);
		} while(haveMoreCookie);
	}
	
	private Topic showCommandTopic() throws JMSException {
		return session.createTopic("#SEMP/" + connectionDescriptor.getApplianceName() + "/SHOW");
	}

	private Topic adminCommandTopic() throws JMSException {
		// FIXME This appears to be unsupported. May need SEMP over HTTP to be able to do this.
		return session.createTopic("#SEMP/" + connectionDescriptor.getApplianceName() + "/ADMIN");
	}

	@Override
	public SmfConnectionDescriptor getSmfConnectionDescriptor() {
		return connectionDescriptor;
	}

}
