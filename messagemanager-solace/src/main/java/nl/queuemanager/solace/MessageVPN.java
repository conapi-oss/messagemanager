package nl.queuemanager.solace;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import nl.queuemanager.jms.JMSBroker;

import java.net.URI;

@Data
@RequiredArgsConstructor
class MessageVPN implements JMSBroker {
	private final String name;	
	private final URI connectionUri;
	
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

}
