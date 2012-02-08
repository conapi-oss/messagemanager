package nl.queuemanager.core.tasks;

import nl.queuemanager.jms.JMSBroker;

public interface ConnectToBrokerTaskFactory {
	public abstract ConnectToBrokerTask create(JMSBroker broker);
}
