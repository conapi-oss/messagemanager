package nl.queuemanager.activemq;

import nl.queuemanager.jms.JMSBroker;

import javax.management.ObjectName;
import java.net.URI;

class ActiveMQBroker implements JMSBroker {

	private final ObjectName name;
	private final URI connectionURI;
	
	public ActiveMQBroker(ObjectName name, URI connectionURI) {
		this.name = name;
		this.connectionURI = connectionURI;
	}
	
	public String toString() {
		return String.format("%s (%s)", getObjectName().getKeyProperty("brokerName"), getConnectionURI());
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof ActiveMQBroker
			&& toString().equals(o.toString());
	}
	
	@Override
	public int hashCode() {
		return getObjectName().hashCode();
	}

	public int compareTo(JMSBroker o) {
		return toString().compareTo(o.toString());
	}

	public ObjectName getObjectName() {
		return name;
	}
	
	public URI getConnectionURI() {
		return connectionURI;
	}

}
