package nl.queuemanager.core.tasks;

import java.io.File;
import java.util.List;

import javax.jms.Message;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.tasks.EnumerateMessagesTask.QueueBrowserEvent;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

import com.google.inject.assistedinject.Assisted;

public interface TaskFactory {
	
	public abstract ConnectToBrokerTask connectToBroker(JMSBroker broker);
	
	public abstract ClearQueuesTask clearQueues(List<JMSQueue> queues);
	public abstract EnumerateQueuesTask enumerateQueues(JMSBroker broker, String filter);
	
	public abstract EnumerateMessagesTask enumerateMessages(JMSQueue queue, EventListener<QueueBrowserEvent> listener);	
	public abstract DeleteMessagesTask deleteMessages(JMSQueue queue, List<Message> messages);
	public abstract MoveMessageListTask moveMessages(JMSQueue toQueue, List<Pair<JMSQueue, String>> messageList);
	
	// Send message (list) tasks
	public abstract SendMessageListTask sendMessage(JMSDestination destination, Message message);
	public abstract SendMessageListTask sendMessage(JMSDestination destination, Message message, @Assisted("repeats") int repeats, @Assisted("delay") int delay);
	public abstract SendMessageListTask sendMessages(JMSDestination destination, List<Message> messages);
	public abstract SendMessageListTask sendMessages(JMSDestination destination, List<Message> messages, @Assisted("repeats") int repeats, @Assisted("delay") int delay);

	// Send file (list) tasks
	public abstract SendFileListTask sendFile(JMSDestination destination, File file, Message template);
	public abstract SendFileListTask sendFile(JMSDestination destination, File file, Message template, @Assisted("repeats") int repeats, @Assisted("delay") int delay);
	public abstract SendFileListTask sendFiles(JMSDestination destination, List<File> files, Message template);
	public abstract SendFileListTask sendFiles(JMSDestination destination, List<File> files, Message template, @Assisted("repeats") int repeats, @Assisted("delay") int delay);
}
