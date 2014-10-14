package nl.queuemanager.activemq;

import javax.jms.JMSException;
import javax.management.ObjectName;

import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

class ActiveMQQueue implements JMSQueue {

	private final ActiveMQBroker broker;
	private final ObjectName name;
	private final long queueSize;
	
	public ActiveMQQueue(ActiveMQBroker broker, ObjectName name, long queueSize) {
		this.broker = broker;
		this.name = name;
		this.queueSize = queueSize;
	}

	public JMSBroker getBroker() {
		return broker;
	}

	public TYPE getType() {
		return TYPE.QUEUE;
	}

	public String getName() {
		return name.getKeyProperty("destinationName");
	}
	
	public ObjectName getObjectName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof JMSDestination) 
			&& ((JMSDestination)o).getType().equals(getType())
			&& ((JMSDestination)o).getName().equals(getName());
	}
	
	public int compareTo(JMSDestination o) {
		return getName().compareTo(o.getName());
	}
	
	public String getQueueName() throws JMSException {
		return getName();
	}

	public int getMessageCount() {
		return (int)queueSize;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
