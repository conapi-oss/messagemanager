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
package nl.queuemanager.smm;

import javax.jms.Destination;
import javax.jms.JMSException;
import java.util.Enumeration;

class SonicMQMessage implements javax.jms.Message {
	protected final javax.jms.Message delegate;

	protected SonicMQMessage(javax.jms.Message delegate) {
		this.delegate = delegate;
	}

	/**
	 * Return the delegate of this message wrapped.
	 * 
	 * @return
	 */
	javax.jms.Message getDelegate() {
		return delegate;
	}
	
	public void acknowledge() throws JMSException {
		delegate.acknowledge();
	}

	public void clearBody() throws JMSException {
		delegate.clearBody();
	}

	public void clearProperties() throws JMSException {
		delegate.clearProperties();
	}

	public boolean getBooleanProperty(String arg0) throws JMSException {
		return delegate.getBooleanProperty(arg0);
	}

	public byte getByteProperty(String name) throws JMSException {
		return delegate.getByteProperty(name);
	}

	public double getDoubleProperty(String name) throws JMSException {
		return delegate.getDoubleProperty(name);
	}

	public float getFloatProperty(String name) throws JMSException {
		return delegate.getFloatProperty(name);
	}

	public int getIntProperty(String name) throws JMSException {
		return delegate.getIntProperty(name);
	}

	public String getJMSCorrelationID() throws JMSException {
		return delegate.getJMSCorrelationID();
	}

	public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
		return delegate.getJMSCorrelationIDAsBytes();
	}

	public int getJMSDeliveryMode() throws JMSException {
		return delegate.getJMSDeliveryMode();
	}

	public Destination getJMSDestination() throws JMSException {
		return delegate.getJMSDestination();
	}

	public long getJMSExpiration() throws JMSException {
		return delegate.getJMSExpiration();
	}

	public String getJMSMessageID() throws JMSException {
		return delegate.getJMSMessageID();
	}

	public int getJMSPriority() throws JMSException {
		return delegate.getJMSPriority();
	}

	public boolean getJMSRedelivered() throws JMSException {
		return delegate.getJMSRedelivered();
	}

	public Destination getJMSReplyTo() throws JMSException {
		return delegate.getJMSReplyTo();
	}

	public long getJMSTimestamp() throws JMSException {
		return delegate.getJMSTimestamp();
	}

	public String getJMSType() throws JMSException {
		return delegate.getJMSType();
	}

	public long getLongProperty(String name) throws JMSException {
		return delegate.getLongProperty(name);
	}

	public Object getObjectProperty(String name) throws JMSException {
		return delegate.getObjectProperty(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration<String> getPropertyNames() throws JMSException {
		return delegate.getPropertyNames();
	}

	public short getShortProperty(String name) throws JMSException {
		return delegate.getShortProperty(name);
	}

	public String getStringProperty(String name) throws JMSException {
		return delegate.getStringProperty(name);
	}

	public boolean propertyExists(String name) throws JMSException {
		return delegate.propertyExists(name);
	}

	public void setBooleanProperty(String name, boolean value)
			throws JMSException {
		delegate.setBooleanProperty(name, value);
	}

	public void setByteProperty(String name, byte value) throws JMSException {
		delegate.setByteProperty(name, value);
	}

	public void setDoubleProperty(String name, double value)
			throws JMSException {
		delegate.setDoubleProperty(name, value);
	}

	public void setFloatProperty(String name, float value) throws JMSException {
		delegate.setFloatProperty(name, value);
	}

	public void setIntProperty(String name, int value) throws JMSException {
		delegate.setIntProperty(name, value);
	}

	public void setJMSCorrelationID(String correlationID) throws JMSException {
		delegate.setJMSCorrelationID(correlationID);
	}

	public void setJMSCorrelationIDAsBytes(byte[] correlationID)
			throws JMSException {
		delegate.setJMSCorrelationIDAsBytes(correlationID);
	}

	public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
		delegate.setJMSDeliveryMode(deliveryMode);
	}

	public void setJMSDestination(Destination destination) throws JMSException {
		delegate.setJMSDestination(destination);
	}

	public void setJMSExpiration(long expiration) throws JMSException {
		delegate.setJMSExpiration(expiration);
	}

	public void setJMSMessageID(String id) throws JMSException {
		delegate.setJMSMessageID(id);
	}

	public void setJMSPriority(int priority) throws JMSException {
		delegate.setJMSPriority(priority);
	}

	public void setJMSRedelivered(boolean redelivered) throws JMSException {
		delegate.setJMSRedelivered(redelivered);
	}

	public void setJMSReplyTo(Destination replyTo) throws JMSException {
		delegate.setJMSReplyTo(replyTo);
	}

	public void setJMSTimestamp(long timestamp) throws JMSException {
		delegate.setJMSTimestamp(timestamp);
	}

	public void setJMSType(String type) throws JMSException {
		delegate.setJMSType(type);
	}

	public void setLongProperty(String name, long value) throws JMSException {
		delegate.setLongProperty(name, value);
	}

	public void setObjectProperty(String name, Object value) throws JMSException {
		delegate.setObjectProperty(name, value);
	}

	public void setShortProperty(String name, short value) throws JMSException {
		delegate.setShortProperty(name, value);
	}

	public void setStringProperty(String name, String value) throws JMSException {
		delegate.setStringProperty(name, value);
	}
}
