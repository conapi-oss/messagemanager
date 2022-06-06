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
package nl.queuemanager.jms;

import javax.jms.JMSException;
import java.util.Enumeration;


/**
 * Represents a single part of a Multipart Message
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public interface JMSPart {	
	public static final String CONTENT_TEXT = "text/plain";
	public static final String CONTENT_XML = "text/xml";
	public static final String CONTENT_BYTES = "application/octet-stream";
	public static final String CONTENT_MESSAGE = "application/x-jms-message";
	
	/**
	 * Get the content of the JMSPart
	 * 
	 * @return
	 */
	public Object getContent();

	/**
	 * Get the contents of the part as a byte[]
	 * 
	 * @return
	 */
	public byte[] getContentBytes();
	
	/**
	 * Get the mime content type of the part.
	 * 
	 * @return
	 */
	public String getContentType();

	/**
	 * Return an enumeration of all header field names in the current part.
	 * 
	 * @return
	 */
	public Enumeration<String> getHeaderFieldNames();

	/**
	 * Get the value of the part header field with name.
	 * 
	 * @param name
	 * @return
	 */
	public String getHeaderField(String name);

	/**
	 * Set the value of a header field.
	 * 
	 * @param name
	 * @param value
	 * @return
	 * @throws JMSException 
	 */
	public void setHeaderField(String name, String value) throws JMSException;
	
	/**
	 * Set the content of the part to the specified object reference. 
	 * THIS METHOD DOES NOT COPY THE OBJECT!
	 * 
	 * @param content
	 * @throws JMSException 
	 */
	public void setContent(Object content, String contentType) throws JMSException;
	
}
