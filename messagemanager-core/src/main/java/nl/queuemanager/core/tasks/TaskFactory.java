package nl.queuemanager.core.tasks;

import java.util.List;

import javax.jms.Message;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.tasks.EnumerateMessagesTask.QueueBrowserEvent;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSQueue;

public interface TaskFactory {
	
	public abstract ClearQueuesTask clearQueues(List<JMSQueue> queues);
	
	public abstract ConnectToBrokerTask connectToBroker(JMSBroker broker);
	
	public abstract DeleteMessagesTask deleteMessages(JMSQueue queue, List<Message> messages);
	
	public abstract EnumerateMessagesTask enumerateMessages(JMSQueue queue, EventListener<QueueBrowserEvent> listener);
	
	public abstract EnumerateQueuesTask enumerateQueues(JMSBroker broker, String filter);
	
}
