/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.core.jms.impl;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import nl.queuemanager.core.jms.JMSMultipartMessage;
import nl.queuemanager.core.jms.JMSPart;
import nl.queuemanager.core.jms.JMSXMLMessage;

/**
 * Factory for JMSMessage* subclasses from this package.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class MessageFactory {
	private MessageFactory() {}
	
	public static Message createMessage() {
		return new JMSMessage();
	}
	
	public static TextMessage createTextMessage() {
		return new JMSTextMessage();
	}
	
	public static JMSXMLMessage createXMLMessage() {
		return new nl.queuemanager.core.jms.impl.JMSXMLMessage();
	}
	
	public static BytesMessage createBytesMessage() {
		return new JMSBytesMessage();
	}
	
	public static StreamMessage createStreamMessage() {
		return new JMSStreamMessage();
	}
	
	public static MapMessage createMapMessage() {
		return new JMSMapMessage();
	}
	
	public static ObjectMessage createObjectMessage() {
		return new JMSObjectMessage();
	}
	
	public static JMSMultipartMessage createMultipartMessage() {
		return new nl.queuemanager.core.jms.impl.JMSMultipartMessage();
	}

	/**
	 * Copy an existing {@link javax.jms.Message} into internal message format.
	 * 
	 * @param message
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Message copyMessage(javax.jms.Message in) throws JMSException {
		Message out;
		
		if (JMSXMLMessage.class.isAssignableFrom(in.getClass())) {
			out = createXMLMessage();
			((JMSXMLMessage)out).setNamespaceAware(((JMSXMLMessage)in).isNamespaceAware());
			((JMSXMLMessage)out).setText(((JMSXMLMessage)in).getText());
	   } else if(TextMessage.class.isAssignableFrom(in.getClass())) {
			out = createTextMessage();
			((TextMessage)out).setText(((TextMessage)in).getText());
		} else if (BytesMessage.class.isAssignableFrom(in.getClass())) {
			BytesMessage bin = (BytesMessage)in;
			BytesMessage bout = createBytesMessage();
			out = bout;
			
			bin.reset();
			
			// Assuming <2GB
			int length = (int)bin.getBodyLength();
			byte[] data = new byte[length]; 
			bin.readBytes(data, length);
			bout.writeBytes(data);
			
			// Put the message body in read-only mode.
			bout.reset();
		} else if (MapMessage.class.isAssignableFrom(in.getClass())) {
			MapMessage min = (MapMessage)in;
			MapMessage mout = createMapMessage();
			out = mout;
			
			for(Enumeration e = min.getMapNames(); e.hasMoreElements();) {
				String name = (String) e.nextElement();
				mout.setObject(name, min.getObject(name));
			}
		} else if (ObjectMessage.class.isAssignableFrom(in.getClass())) {
			out = createObjectMessage();
			((ObjectMessage)out).setObject(((ObjectMessage)in).getObject());
		} else if (StreamMessage.class.isAssignableFrom(in.getClass())) {
			StreamMessage sin = (StreamMessage)in;
			StreamMessage sout = createStreamMessage();
			out = sout;
			
			sin.reset();
			
			try {
				while(true) {
					sout.writeObject(sin.readObject());
				}
			} catch (MessageEOFException e) {
				// The entire stream has been read
			}
			
			sout.reset();
		} else if (JMSMultipartMessage.class.isAssignableFrom(in.getClass())) {
			JMSMultipartMessage min = (JMSMultipartMessage)in;
			JMSMultipartMessage mout = createMultipartMessage();
			out = mout;
			
			// Copy all message parts
			for(int i = 0; i < min.getPartCount(); i++) {
				JMSPart pin = min.getPart(i);
				Object content = pin.getContent();
				String contentType = pin.getContentType();
				
				JMSPart pout = mout.createPart(content, contentType);
				
				// Copy the part headers
				for(Enumeration<String> e = pin.getHeaderFieldNames(); e.hasMoreElements();) {
					String name = e.nextElement();
					pout.setHeaderField(name, pin.getHeaderField(name));
				}
				
				mout.addPart(pout);
			}
		} else {
			out = createMessage();
		}

		copyHeaders(in, out);
		copyProperties(in, out);
		
		return out;
	}

	public static void copyHeaders(javax.jms.Message in, Message out)
			throws JMSException {
		// Now copy the default JMS headers
		out.setJMSCorrelationID(in.getJMSCorrelationID());
		out.setJMSDeliveryMode(convertDeliveryMode(in.getJMSDeliveryMode()));
		out.setJMSDestination(copyDestination(in.getJMSDestination()));
		out.setJMSExpiration(in.getJMSExpiration());
		out.setJMSMessageID(in.getJMSMessageID());
		out.setJMSPriority(in.getJMSPriority());
		out.setJMSRedelivered(in.getJMSRedelivered());
		out.setJMSReplyTo(copyDestination(in.getJMSReplyTo()));
		out.setJMSTimestamp(in.getJMSTimestamp());
		out.setJMSType(in.getJMSType());		
	}

	@SuppressWarnings("unchecked")
	public static void copyProperties(javax.jms.Message in, Message out)
			throws JMSException {
		// Copy custom properties
		for(Enumeration e = in.getPropertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			out.setObjectProperty(name, in.getObjectProperty(name));
		}
	}

	protected static int convertDeliveryMode(int deliveryMode) {
		if(deliveryMode <= 2)
			return deliveryMode;
		
		// Some non-standard delivery mode, convert to PERSISTENT to be safe
		return DeliveryMode.PERSISTENT;
	}

	/**
	 * Copy a javax.jms.Destination object into a new, internal format, JMSDestination.
	 * @param in
	 * @return
	 * @throws JMSException
	 */
	public static Destination copyDestination(Destination in) throws JMSException {
		if(in == null) {
			return null;
		}
		
		if(Queue.class.isAssignableFrom(in.getClass())) {
			return DestinationFactory.createQueue(((Queue)in).getQueueName());
		} else {
			return DestinationFactory.createTopic(((Topic)in).getTopicName());
		}
	}

}
