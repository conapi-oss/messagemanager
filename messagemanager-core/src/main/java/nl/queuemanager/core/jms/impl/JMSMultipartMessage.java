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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.jms.JMSException;
import javax.jms.Message;

import nl.queuemanager.core.jms.JMSPart;

class JMSMultipartMessage extends JMSMessage implements nl.queuemanager.core.jms.JMSMultipartMessage, Serializable {

	protected final List<JMSPart> parts;

	public JMSMultipartMessage() {
		parts = new ArrayList<JMSPart>();
	}

	@Override
	public void clearBody() {
		parts.clear();
	}
	
	public void addPart(JMSPart part) throws JMSException {
		parts.add(part);
	}

	public JMSPart createMessagePart(Message message) throws JMSException {
		JMSPartImpl part = new JMSPartImpl();
		part.setContent(message, JMSPart.CONTENT_MESSAGE);
		return part;
	}

	public JMSPart createPart(DataHandler h) throws JMSException {
		return new JMSPartImpl(h);
	}

	public JMSPart createPart() {
		return new JMSPartImpl();
	}

	public JMSPart createPart(Object content, String contentType) throws JMSException {
		JMSPartImpl part = new JMSPartImpl();
		part.setContent(content, contentType);
		return part;
	}

	public Message getMessageFromPart(int index) throws JMSException {
		return (Message)parts.get(index).getContent();
	}

	public JMSPart getPart(int index) throws JMSException {
		return parts.get(index);
	}

	public int getPartCount() throws JMSException {
		return parts.size();
	}

	public boolean isMessagePart(int index) throws JMSException {
		Object content = parts.get(index).getContent();
		return content != null && Message.class.isAssignableFrom(content.getClass());
	}

	public void removePart(int index) throws JMSException {
		parts.remove(index);
	}

}
