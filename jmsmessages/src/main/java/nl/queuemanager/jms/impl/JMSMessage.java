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
package nl.queuemanager.jms.impl;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.queuemanager.jms.MetaDataProvider;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.Serializable;
import java.util.*;

/**
 * Implement a JMS Message without an actual JMS implementation.
 * 
 * @author gerco
 *
 */
class JMSMessage implements Message, Serializable, MetaDataProvider {

	private static final long serialVersionUID = 5662495489350887464L;

	protected final Map<Object, Object> properties;
	
	protected String correlationID;
	protected byte[] correlationIDBytes;
	
	protected int deliveryMode;
	protected transient Destination destination;
	private long expiration;
	private String messageID;
	private int priority;
	private boolean redelivered;
	private transient Destination replyTo;
	private long timestamp;
	private String type;

	private long deliveryTime;

	// optional metadata, will not be serialized
	@Getter
	@Setter
	private transient Map<String, Object> metaData;
	
	JMSMessage() {
		this.properties = new HashMap<Object, Object>();
		this.timestamp = new Date().getTime();
		metaData = null; // we use null to indicate that the metadata is not supported
	}
	
	public void acknowledge() {
	}

	public void clearBody() throws JMSException {
	}

	@Override
	public <T> T getBody(Class<T> c) throws JMSException {
		return null;
	}

	@Override
	public boolean isBodyAssignableTo(Class c) throws JMSException {
		return false;
	}

	public void clearProperties() {
		properties.clear();
	}

	public boolean getBooleanProperty(String name) {
		return (Boolean)getObjectProperty(name);
	}

	public byte getByteProperty(String name) {
		return (Byte)getObjectProperty(name);
	}

	public double getDoubleProperty(String name) {
		return (Double)getObjectProperty(name);
	}

	public float getFloatProperty(String name) {
		return (Float)getObjectProperty(name);
	}

	public int getIntProperty(String name) {
		return (Integer)getObjectProperty(name);
	}

	public String getJMSCorrelationID() {
		if(correlationID != null) 
			return correlationID;
		
		if(correlationIDBytes != null)
			return new String(correlationIDBytes);
		
		return null;
	}

	public byte[] getJMSCorrelationIDAsBytes() {
		if(correlationIDBytes != null)
			return correlationIDBytes;
		
		if(correlationID != null) 
			return correlationID.getBytes();
		
		return null;
	}

	public int getJMSDeliveryMode() {
		return deliveryMode;
	}

	public Destination getJMSDestination() {
		return destination;
	}

	public long getJMSExpiration() {
		return expiration;
	}

	public String getJMSMessageID() {
		if(messageID == null)
			setJMSMessageID("ID:" + UUID.randomUUID().toString());
		return messageID;
	}

	public int getJMSPriority() {
		return priority;
	}

	public boolean getJMSRedelivered() {
		return redelivered;
	}

	public Destination getJMSReplyTo() {
		return replyTo;
	}

	public long getJMSTimestamp() {
		return timestamp;
	}

	public String getJMSType() {
		return type;
	}

	public long getLongProperty(String name) {
		return (Long)getObjectProperty(name);
	}

	public Object getObjectProperty(String name) {
		return properties.get(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getPropertyNames() {
		return Collections.enumeration(properties.keySet());
	}

	public short getShortProperty(String name) {
		return (Short)getObjectProperty(name);
	}

	public String getStringProperty(String name) {
		return (String)getObjectProperty(name);
	}

	public boolean propertyExists(String name) {
		return properties.containsKey(name);
	}

	public void setBooleanProperty(String name, boolean value) {
		setObjectProperty(name, value);
	}

	public void setByteProperty(String name, byte value) {
		setObjectProperty(name, value);
	}

	public void setDoubleProperty(String name, double value) {
		setObjectProperty(name, value);
	}

	public void setFloatProperty(String name, float value) {
		setObjectProperty(name, value);
	}

	public void setIntProperty(String name, int value) {
		setObjectProperty(name, value);
	}

	public void setJMSCorrelationID(String value) {
		correlationID = value;
		correlationIDBytes = null;
	}

	public void setJMSCorrelationIDAsBytes(byte[] value) {
		correlationID = null;
		correlationIDBytes = value;
	}

	public void setJMSDeliveryMode(int arg0) {
		deliveryMode = arg0;
	}

	public void setJMSDestination(Destination arg0) {
		destination = arg0;
	}

	public void setJMSExpiration(long arg0) {
		expiration = arg0;
	}

	@Override
	public long getJMSDeliveryTime() throws JMSException {
		return deliveryTime;
	}

	@Override
	public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
		this.deliveryTime = deliveryTime;
	}

	public void setJMSMessageID(String arg0) {
		messageID = arg0;
	}

	public void setJMSPriority(int arg0) {
		priority = arg0;
	}

	public void setJMSRedelivered(boolean arg0) {
		redelivered = arg0;
	}

	public void setJMSReplyTo(Destination arg0) {
		replyTo = arg0;
	}

	public void setJMSTimestamp(long arg0) {
		timestamp = arg0;
	}

	public void setJMSType(String arg0) {
		type = arg0;
	}

	public void setLongProperty(String arg0, long arg1) {
		setObjectProperty(arg0, arg1);
	}

	public void setObjectProperty(String arg0, Object arg1) {
		properties.put(arg0, arg1);
	}

	public void setShortProperty(String arg0, short arg1) {
		setObjectProperty(arg0, arg1);
	}

	public void setStringProperty(String arg0, String arg1) {
		setObjectProperty(arg0, arg1);
	}


}
