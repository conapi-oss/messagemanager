package nl.queuemanager.solace;

import nl.queuemanager.jms.JMSBroker;

import java.net.URI;

class MessageVPN implements JMSBroker {
	private final String name;	
	private final URI connectionUri;
	
	public MessageVPN(String name, URI connectionUri) {
		this.name = name;
		this.connectionUri = connectionUri;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", 
			getName(),
			connectionUri.toString());
	}
	
	@Override
	public int compareTo(JMSBroker o) {
		return getName().compareTo(((MessageVPN)o).getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageVPN other = (MessageVPN) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
