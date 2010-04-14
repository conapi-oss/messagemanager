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

import javax.activation.DataHandler;
import javax.jms.JMSException;
import javax.jms.Message;

import nl.queuemanager.core.jms.JMSMultipartMessage;
import nl.queuemanager.core.jms.JMSPart;
import progress.message.jclient.Channel;
import progress.message.jclient.MultipartMessage;
import progress.message.jclient.Part;

/**
 * Wraps a SonicMQ specfic multipart message in a JMSMultipartMessage interface to
 * insulate the SonicMQ specifics from the rest of the app.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
class SonicMQMultipartMessage extends SonicMQMessage implements JMSMultipartMessage {

	protected SonicMQMultipartMessage(MultipartMessage delegate) {
		super(delegate);
	}

	@Override
	MultipartMessage getDelegate() {
		return (MultipartMessage)delegate;
	}
	
	public void addPart(JMSPart part) throws JMSException {
		getDelegate().addPart(((SonicMQMultipartMessagePart)part).getDelegate());
	}

	public JMSPart createMessagePart(Message message) throws JMSException {
		return new SonicMQMultipartMessagePart(getDelegate().createMessagePart(message));
	}

	public JMSPart createPart(DataHandler h) throws JMSException {
		return new SonicMQMultipartMessagePart(getDelegate().createPart(h));
	}

	public JMSPart createPart() {
		return new SonicMQMultipartMessagePart(getDelegate().createPart());
	}

	public JMSPart createPart(Object object, String contentType) throws JMSException {
		return new SonicMQMultipartMessagePart(getDelegate().createPart(object, contentType));
	}

	public Message getMessageFromPart(int index) throws JMSException {
		Message ret = getDelegate().getMessageFromPart(index);
		if(ret instanceof MultipartMessage)
			return new SonicMQMultipartMessage((MultipartMessage)ret);
		return ret;
	}

	public JMSPart getPart(int index) throws JMSException {
		return new SonicMQMultipartMessagePart(getDelegate().getPart(index));
	}

	public int getPartCount() throws JMSException {
		return getDelegate().getPartCount();
	}

	public boolean isMessagePart(int index) throws JMSException {
		return getDelegate().isMessagePart(index);
	}

	public void removePart(int index) throws JMSException {
		getDelegate().removePart(index);
	}

	public void addPart(Part part) throws JMSException {
		getDelegate().addPart(part);
	}

	public void clearReadOnly() throws JMSException {
		getDelegate().clearReadOnly();
	}

	public boolean doesPartExist(String cid) throws JMSException {
		return getDelegate().doesPartExist(cid);
	}

	public int getBodySize() throws JMSException {
		return getDelegate().getBodySize();
	}

	public Channel getChannel() throws JMSException {
		return getDelegate().getChannel();
	}

	public Message getMessageFromPart(String cid) throws JMSException {
		return getDelegate().getMessageFromPart(cid);
	}

	public Part getPart(String cid) throws JMSException {
		return getDelegate().getPart(cid);
	}

	public boolean isMessagePart(String cid) throws JMSException {
		return getDelegate().isMessagePart(cid);
	}

	public void removePart(String cid) throws JMSException {
		getDelegate().removePart(cid);
	}

	
}
