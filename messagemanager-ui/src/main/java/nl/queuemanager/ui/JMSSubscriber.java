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
package nl.queuemanager.ui;

import java.util.List;
import java.util.Observable;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.swing.SwingUtilities;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.core.MessageEvent;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.events.EventSource;
import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.util.Clearable;

/**
 * Contains a MessageBuffer and possibly a MessageConsumer subscribed to a JMS Topic.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class JMSSubscriber extends Observable implements EventSource<MessageEvent>, EventListener<MessageEvent>, Comparable<JMSSubscriber>, Clearable {
	private final JMSDomain sonic;
	private final TaskExecutor worker;
	private final JMSDestination destination;
	private final MessageBuffer buffer;
	private MessageConsumer consumer;
	
	public JMSSubscriber(JMSDomain sonic, TaskExecutor worker, Configuration config, JMSDestination destination, MessageBuffer buffer) {
		this.sonic = sonic;
		this.worker = worker;
		this.destination = destination;
		this.buffer = buffer;
		
		buffer.setMaximumNumberOfMessages(Integer.parseInt(config.getUserPref(
				Configuration.PREF_MAX_BUFFERED_MSG, "50")));
		buffer.addListener(this);
	}
	
	/**
	 * Process a MessageEvent. For any MessageEvent, we just want to notify our observers
	 * (if any) to update their state. Something will have changed in our message buffer.
	 * 
	 * Runs on the JMS Session Delivery Thread, not the EDT!
	 */
	public void processEvent(MessageEvent event) {
		// We don't care about the event type, just notify the observers
		fireNotifyObservers();
	}
	
	/**
	 * Remove the specified messages from the buffer.
	 * 
	 * @param messages the messages to remove
	 */
	public void removeMessages(List<Message> messages) {
		getBuffer().remove(messages);
		fireNotifyObservers();
	}

	/**
	 * Remove all messages from the buffer.
	 */
	public void clear() {
		getBuffer().clear();
		fireNotifyObservers();
	}
	
	/**
	 * Keep a certain message in the message buffer, even if the buffer is full.
	 * 
	 * @param message
	 */
	public void lockMessage(Message message) {
		getBuffer().lockMessage(message);
	}
	
	/**
	 * Unlock all locked messages.
	 */
	public void unlockMessages() {
		getBuffer().unlockAll();
	}
	
	/**
	 * Get the JMSDestination (JMSQueue or JMSTopic) for this subscriber.
	 * 
	 * @return
	 */
	public JMSDestination getDestination() {
		return destination;
	}

	public int getMessageCount() {
		return buffer.getMessageCount();
	}
	
	public List<Message> getMessages() {
		return getBuffer().getMessages();
	}
	
	private MessageBuffer getBuffer() {
		return buffer;
	}
	
	public boolean isActive() {
		return consumer != null;
	}
	
	// Runs on the worker thread, not the EDT!
	private void setConsumer(final MessageConsumer c) {
		// Invoke on the EDT because the EDT also reads the value of the consumer
		// field and I don't feel like synchronizing to set it.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				consumer = c;
				fireNotifyObservers();
			}
		});
	}
	
	public void setActive(boolean active) {
		if(isActive() && !active) {
			final MessageConsumer toClose = consumer;
			
			// Deactivate the consumer
			worker.execute(new BackgroundTask(null) {
				@Override
				public void execute() throws Exception {
					toClose.close();
					setConsumer(null);
				}
				
				@Override
				public String toString() {
					return "Closing message consumer for " + destination;
				}
			});

			return;
		}
		
		if(!isActive() && active) {
			// Activate the consumer
			worker.execute(new BackgroundTask(destination.getBroker()) {
				@Override
				public void execute() throws Exception {
					setConsumer(sonic.openConsumer(destination, buffer));
				}
				
				@Override
				public String toString() {
					return "Opening messageconsumer for " + destination;
				}
			});
		}
	}

	public void addListener(EventListener<MessageEvent> listener) {
		getBuffer().addListener(listener);
	}

	public void removeListener(EventListener<MessageEvent> listener) {
		getBuffer().removeListener(listener);
	}

	/**
	 * Fire notifyObservers on the EDT. If already running on the EDT, call notifyObservers directly.
	 */
	private void fireNotifyObservers() {
		setChanged();
		notifyObservers();
	}

	public int compareTo(JMSSubscriber o) {
		return getDestination().compareTo(o.getDestination());
	}
}
