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
package nl.queuemanager.solace;

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
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import nl.queuemanager.jms.JMSMultipartMessage;

/**
 * Converts messages from foreign JMS providers (anything that implements javax.jms.Message)
 * into Solace format.
 */
class SolaceMessageConverter {
	/**
	 * Convert a {@link Message} object to the representation of the provided broker
	 * in the provided {@link Domain}.
	 * 
	 * @param session
	 * @param in
	 * @return
	 */
	public static Message convertMessage(Session session, javax.jms.Message in) throws JMSException {
		Message out;

		// If the message is already an Solace message, return it.
		if(com.solacesystems.jms.message.SolMessage.class.isAssignableFrom(in.getClass()))
			return in;
		
		// The message is from a foreign JMS provider, convert it into an Solace message.
	    if(TextMessage.class.isAssignableFrom(in.getClass())) {
			out = session.createTextMessage();
			((TextMessage)out).setText(((TextMessage)in).getText());
		} else if (BytesMessage.class.isAssignableFrom(in.getClass())) {
			BytesMessage bin = (BytesMessage)in;
			BytesMessage bout = session.createBytesMessage();
			out = bout;
			bin.reset();
			
			// Assuming <2GB
			int length = (int)bin.getBodyLength();
			byte[] data = new byte[length]; 
			bin.readBytes(data, length);
			bout.writeBytes(data);
			
			bout.reset();
		} else if (MapMessage.class.isAssignableFrom(in.getClass())) {
			MapMessage min = (MapMessage)in;
			MapMessage mout = session.createMapMessage();
			out = mout;
			
			for(Enumeration e = min.getMapNames(); e.hasMoreElements();) {
				String name = (String) e.nextElement();
				mout.setObject(name, min.getObject(name));
			}
		} else if (ObjectMessage.class.isAssignableFrom(in.getClass())) {
			out = session.createObjectMessage();
			((ObjectMessage)out).setObject(((ObjectMessage)in).getObject());
		} else if (StreamMessage.class.isAssignableFrom(in.getClass())) {
			StreamMessage sin = (StreamMessage)in;
			StreamMessage sout = session.createStreamMessage();
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
			throw new JMSException("Multi part messages are not supported");
		} else {
			out = session.createMessage();
		}

		// Now copy the default JMS headers
		out.setJMSCorrelationID(in.getJMSCorrelationID());
		out.setJMSDeliveryMode(convertDeliveryMode(in.getJMSDeliveryMode()));
		out.setJMSDestination(convertDestination(session, in.getJMSDestination()));
		out.setJMSExpiration(in.getJMSExpiration());
		out.setJMSPriority(in.getJMSPriority());
		out.setJMSReplyTo(convertDestination(session, in.getJMSReplyTo()));
		out.setJMSTimestamp(in.getJMSTimestamp());
		out.setJMSType(in.getJMSType());
		
		// Copy custom properties
		for(Enumeration e = in.getPropertyNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			out.setObjectProperty(name, in.getObjectProperty(name));
		}
		
		return out;
	}

	protected static int convertDeliveryMode(int deliveryMode) {
		if(deliveryMode <= 2)
			return deliveryMode;
		
		// Some non-standard delivery mode, convert to PERSISTENT to be safe
		return DeliveryMode.PERSISTENT;
	}

	public static Destination convertDestination(Session session, Destination in) throws JMSException {
		if(in == null) {
			return null;
		}
		
		if(Queue.class.isAssignableFrom(in.getClass())) {
			return session.createQueue(((Queue)in).getQueueName());
		} else {
			return session.createTopic(((Topic)in).getTopicName());
		}
	}
}
