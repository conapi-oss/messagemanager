package nl.queuemanager.fakemq;

import nl.queuemanager.jms.JMSBroker;

public class FakeMQBroker implements JMSBroker {

	private final String name;
	
	public FakeMQBroker(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public int compareTo(JMSBroker o) {
		return toString().compareTo(o.toString());
	}

}
