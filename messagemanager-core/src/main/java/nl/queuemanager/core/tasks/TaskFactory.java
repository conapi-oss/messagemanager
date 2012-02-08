package nl.queuemanager.core.tasks;

import nl.queuemanager.jms.JMSBroker;

public interface TaskFactory {
	public abstract ConnectToBrokerTask connectToBroker(JMSBroker broker);
	public abstract EnumerateQueuesTask enumerateQueues(JMSBroker broker, String filter);
}
