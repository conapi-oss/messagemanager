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
package nl.queuemanager.core.tasks;

import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.jms.JMSDestination;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class SendMessageListTask extends Task implements CancelableTask {
	
	private final int repeats;
	private final JMSDestination queue;
	private final List<Message> messages;
	private final JMSDomain domain;
	private final int delay;
	private volatile boolean canceled;
	
	@AssistedInject
	SendMessageListTask(@Assisted JMSDestination queue, @Assisted Message message, JMSDomain domain, EventBus eventBus) {
		this(queue, Collections.singletonList(message), 1, 0, domain, eventBus);
	}
	
	@AssistedInject
	SendMessageListTask(@Assisted JMSDestination queue, @Assisted Message message, @Assisted("repeats") int repeats, @Assisted("delay") int delay, JMSDomain domain, EventBus eventBus) {
		this(queue, Collections.singletonList(message), repeats, delay, domain, eventBus);
	}
	
	@AssistedInject
	SendMessageListTask(@Assisted JMSDestination queue, @Assisted List<Message> messages, JMSDomain domain, EventBus eventBus) {
		this(queue, messages, 1, 0, domain, eventBus);
	}

	@AssistedInject
	SendMessageListTask(@Assisted JMSDestination queue, @Assisted List<Message> messages, @Assisted("repeats") int repeats, @Assisted("delay") int delay, JMSDomain domain, EventBus eventBus) {
		super(queue.getBroker(), eventBus);
		
		this.repeats = repeats;
		this.delay = delay;
		this.queue = queue;
		this.messages = messages;
		this.domain = domain;
	}
	
	@Override
	public void execute() throws Exception {
		if(canceled) return;
		
		int i = 0;
		for(int r=0; r<repeats; r++) {
			for(Message m: messages) {
				if(delay != 0 && i > 0)
					sleep(delay);
				
				// Store and replace the old CorrelationId to keep the %i token if
				// present for future repeats.
				String oldcid = m.getJMSCorrelationID();
				replaceFields(m, i+1);
				domain.sendMessage(queue, m);
				m.setJMSCorrelationID(oldcid);
				
				reportProgress(i++);
				if(canceled) return;
			}
		}
	}
	
	/**
	 * Replace tokens like %i (message number) in the correlationid
	 * 
	 * @param message
	 * @throws JMSException 
	 */
	private void replaceFields(final Message message, final int seqNum) throws JMSException {
		if(message.getJMSCorrelationID() == null)
			return;
		
		message.setJMSCorrelationID(
				message.getJMSCorrelationID().replaceAll(
						"\\%i", Integer.toString(seqNum)));
	}

	private void sleep(final int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public int getProgressMaximum() {
		return repeats * messages.size();
	}
	
	@Override
	public String toString() {
		return "Sending " + getProgressMaximum() + " message(s) to " + queue;
	}

	public void cancel() {
		this.canceled = true;
	}
}
