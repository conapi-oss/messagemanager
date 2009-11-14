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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.activation.DataHandler;
import javax.jms.JMSException;

import nl.queuemanager.core.jms.JMSPart;
import nl.queuemanager.core.util.CollectionFactory;

public class JMSPartImpl implements JMSPart {

	public Object content;
	public String contentType;
	protected final Map<String, String> headers;
	
	public JMSPartImpl() {
		headers = CollectionFactory.newHashMap();
	}
	
	public JMSPartImpl(DataHandler h) throws JMSException {
		this();
		try {
			setContent(h.getContent(), h.getContentType());
		} catch (IOException e) {
			throw new JMSException(e.toString());
		}
	}

	public Object getContent() {
		return content;
	}
	
	/**
	 * Set the content of the part to the specified object reference. 
	 * THIS METHOD DOES NOT COPY THE OBJECT!
	 * 
	 * @param content
	 */
	public void setContent(Object content, String contentType) {
		this.content = content;
		this.contentType = contentType;
	}

	/**
	 * If the content of this part is a byte[], return the byte[]. If it is a String, return
	 * String.getBytes(). If it is neither return the serialized form of the content object (if 
	 * Serializable).
	 * <P>
	 * If none of the above apply, throws RuntimeException
	 */
	public byte[] getContentBytes() {
		if(byte[].class.isAssignableFrom(content.getClass()))
			return (byte[])content;
		
		if(String.class.isAssignableFrom(content.getClass()))
			return ((String)content).getBytes();
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(getContent());
			oos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert the part content to bytes", e);
		}
	}

	public String getContentType() {
		return contentType;
	}
	
	public String getHeaderField(String name) {
		return headers.get(name);
	}

	public Enumeration<String> getHeaderFieldNames() {
		return Collections.enumeration(headers.keySet());
	}

	public void setHeaderField(String name, String value) {
		headers.put(name, value);
	}

}
