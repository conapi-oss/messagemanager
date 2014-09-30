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
package nl.queuemanager.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.util.Clearable;

/**
 * Receives and holds JMSMessages received from a JMS MessageConsumer.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class MessageBuffer extends AbstractEventSource<MessageEvent> implements MessageListener, Clearable {
	private final List<Message> messages = new ArrayList<Message>();
	private final Set<Message> lockedMessages = new HashSet<Message>();
	private final Object lock = new Object();
	
	private int maximumNumberOfMessages = 50;
	
	public MessageBuffer() {
		super(null);
	}
	
	public void onMessage(Message message) {
		synchronized(lock) {
			makeRoomForNewMessage();
			this.messages.add(message);
		}
		
		try {
			message.acknowledge();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		
		dispatchEvent(new MessageEvent(MessageEvent.EVENT.MESSAGE_RECEIVED, message, this));
	}

	/**
	 * Walks the buffer for messages to remove until there is room in the buffer for
	 * one more message. May remove more than one message if the buffer is over-filled
	 * because of previously locked messages.
	 */
	private void makeRoomForNewMessage() {
		if(messages.size() < getMaximumNumberOfMessages())
			return;
		
		for(Iterator<Message> it = messages.iterator(); it.hasNext();) {
			final Message candidate = it.next();
			if(!lockedMessages.contains(candidate)) {
				it.remove();
				dispatchEvent(new MessageEvent(MessageEvent.EVENT.MESSAGE_DISCARDED, candidate, this));
			}
			
			if(messages.size() < getMaximumNumberOfMessages())
				break;
		}
	}

	public List<Message> getMessages() {
		synchronized(lock) {
			return Collections.unmodifiableList(new ArrayList<Message>(messages));
		}
	}
	
	public int getMessageCount() {
		synchronized(lock) {
			return messages.size();
		}
	}
	
	public void remove(List<Message> messagesToRemove) {
		synchronized(lock) {
			messages.removeAll(messagesToRemove);
		}
	}
	
	public void clear() {
		synchronized(lock) {
			messages.clear();
		}
	}
	
	public void lockMessage(Message message) {
		synchronized(lock) {
			if(messages.contains(message)) {
				lockedMessages.add(message);
			}
		}
	}
	
	public void unlockMessage(Message message) {
		synchronized(lock) {
			lockedMessages.remove(message);
		}
	}
	
	public void unlockAll() {
		synchronized(lock) {
			lockedMessages.clear();
		}
	}

	public void setMaximumNumberOfMessages(int maximumNumberOfMessages) {
		this.maximumNumberOfMessages = maximumNumberOfMessages;
	}

	public int getMaximumNumberOfMessages() {
		return maximumNumberOfMessages;
	}
}
