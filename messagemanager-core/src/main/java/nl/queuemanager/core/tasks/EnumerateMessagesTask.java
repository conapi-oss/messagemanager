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

import java.util.Enumeration;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.jms.Message;

import nl.queuemanager.core.events.AbstractEventSource;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.jms.JMSQueue;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;

public class EnumerateMessagesTask extends BackgroundTask implements CancelableTask {
	private final Logger log = Logger.getLogger(getClass().getName());
	
	private final JMSQueue queue;
	private final JMSDomain domain;
	private QueueBrowserEventSource eventSource;
	
	private volatile boolean canceled = false;

	@Inject
	EnumerateMessagesTask(
			@Assisted final JMSQueue queue, 
			@Assisted final EventListener<QueueBrowserEvent> listener,
			final JMSDomain domain,
			EventBus eventBus) 
	{
		super(queue.getBroker(), eventBus);
		this.queue = queue;
		this.domain = domain;
		this.eventSource = new QueueBrowserEventSource();
		this.eventSource.addListener(listener);
	}
	
	@Override
	public void execute() throws Exception {
		if(canceled) return;
		
		eventSource.fireBrowsingStarted(this);

		Enumeration<Message> e = domain.enumerateMessages(getQueue());
		Message message;
		while((message = e.nextElement()) != null) {
			log.finest("eventSource.fireMessageFound(this, message)");
			eventSource.fireMessageFound(this, message);
			if(canceled) {
				break;
			}
		}
		
		eventSource.fireBrowsingComplete(this);
	}
	
	public void cancel() {
		canceled = true;
	}

	public JMSQueue getQueue() {
		return queue;
	}
	
	@Override
	public String toString() {
		return "Browsing messages for queue " + getQueue();
	}

	@SuppressWarnings("serial")
	public static class QueueBrowserEvent extends EventObject {
		public static enum EVENT {
			BROWSING_STARTED,
			MESSAGE_FOUND,
			BROWSING_COMPLETE
		}
		
		private final EVENT id; 
		private final Object info; 
		
		public QueueBrowserEvent(EVENT id, Object source, Object info) {
			super(source);
			this.id = id;
			this.info = info;
		}

		public EVENT getId() {
			return id;
		}

		public Object getInfo() {
			return info;
		}
	}
	
	private class QueueBrowserEventSource extends AbstractEventSource<QueueBrowserEvent> {
		public QueueBrowserEventSource() {
			super(null);
		}
		
		public void fireBrowsingStarted(EnumerateMessagesTask source) {
			dispatchEvent(new QueueBrowserEvent(QueueBrowserEvent.EVENT.BROWSING_STARTED, source, getQueue()));
		}
		
		public void fireMessageFound(EnumerateMessagesTask source, Message message) {
			dispatchEvent(new QueueBrowserEvent(QueueBrowserEvent.EVENT.MESSAGE_FOUND, source, message));
		}
		
		public void fireBrowsingComplete(EnumerateMessagesTask source) {
			dispatchEvent(new QueueBrowserEvent(QueueBrowserEvent.EVENT.BROWSING_COMPLETE, source, null));
		}
	}
}
