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

import javax.activation.DataHandler;

public interface JMSMultipartMessage extends javax.jms.Message {

	/**
	 * Get the number of Parts in this message.
	 * 
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public int getPartCount() throws javax.jms.JMSException;

	/**
	 * Get the JMSPart at index
	 * 
	 * @param index
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public JMSPart getPart(int index) throws javax.jms.JMSException;

	/**
	 * Get the javax.jms.Message from part with index.
	 * @param arg0
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public javax.jms.Message getMessageFromPart(int index) throws javax.jms.JMSException;

	/**
	 * Does the part at index contain a javax.jms.Message?
	 * 
	 * @param arg0
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public boolean isMessagePart(int index) throws javax.jms.JMSException;

	/**
	 * Remove the part at index
	 * 
	 * @param arg0
	 * @throws javax.jms.JMSException
	 */
	public void removePart(int arg0) throws javax.jms.JMSException;

	/**
	 * Add the JMSPart to the message
	 * 
	 * @param part
	 * @throws javax.jms.JMSException
	 */
	public void addPart(JMSPart part) throws javax.jms.JMSException;

	/**
	 * Create a new JMSPart and set it's contents from the specified DataHandler.
	 * 
	 * @param h
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public JMSPart createPart(DataHandler h) throws javax.jms.JMSException;

	/**
	 * Create a new JMSPart for this message
	 * 
	 * @return
	 */
	public JMSPart createPart();

	/**
	 * Create a new JMSPart with the specified content and content-type
	 * 
	 * @param content
	 * @param contentType
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public JMSPart createPart(Object content, String contentType) throws javax.jms.JMSException;

	/**
	 * Create a new JMSPart with the specified javax.jms.Message as the content.
	 * 
	 * @param message
	 * @return
	 * @throws javax.jms.JMSException
	 */
	public JMSPart createMessagePart(javax.jms.Message message) throws javax.jms.JMSException;
}
