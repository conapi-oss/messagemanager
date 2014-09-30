package nl.queuemanager.ui;

import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.jms.JMSDestination;

public interface JMSSubscriberFactory {
	public abstract JMSSubscriber newSubscriber(JMSDestination destination, MessageBuffer buffer);
}
