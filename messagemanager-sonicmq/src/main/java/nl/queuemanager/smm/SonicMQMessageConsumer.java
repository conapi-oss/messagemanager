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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import progress.message.jclient.MultipartMessage;
import progress.message.jclient.XMLMessage;

public class SonicMQMessageConsumer implements MessageConsumer {

	private final MessageConsumer delegate;
	
	public SonicMQMessageConsumer(MessageConsumer delegate) {
		this.delegate = delegate;
	}

	public void close() throws JMSException {
		delegate.close();
	}

	public MessageListener getMessageListener() throws JMSException {
		return delegate.getMessageListener();
	}

	public String getMessageSelector() throws JMSException {
		return delegate.getMessageSelector();
	}

	public Message receive() throws JMSException {
		return wrap(delegate.receive());
	}

	public Message receive(long arg0) throws JMSException {
		return wrap(delegate.receive(arg0));
	}

	public Message receiveNoWait() throws JMSException {
		return wrap(delegate.receiveNoWait());
	}

	public void setMessageListener(MessageListener listener) throws JMSException {
		delegate.setMessageListener(new MessageListenerWrapper(listener));
	}

	private class MessageListenerWrapper implements MessageListener {
		private final MessageListener delegate;
		
		public MessageListenerWrapper(MessageListener delegate) {
			this.delegate = delegate;
		}
		
		public void onMessage(Message msg) {
			this.delegate.onMessage(wrap(msg));
		}
	}
	
	private static Message wrap(Message message) {
		if(message instanceof MultipartMessage) {
			return new SonicMQMultipartMessage((MultipartMessage)message);
		} else if(message instanceof XMLMessage) {
			return new SonicMQXMLMessage((XMLMessage)message);
		}
		
		return message;
	}
}
