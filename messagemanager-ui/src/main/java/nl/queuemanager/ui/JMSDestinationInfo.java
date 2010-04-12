package nl.queuemanager.ui;

import java.io.Serializable;

import nl.queuemanager.core.jms.JMSBroker;
import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.JMSDestination.TYPE;
import nl.queuemanager.core.jms.JMSDomain;

/**
 * Holds name and type of a JMSDestination, for serialization support.
 * 
 * @author Gerco Dries
 *
 */
public class JMSDestinationInfo implements Serializable {
	private static final long serialVersionUID = -8934608597429470626L;
	
	private final String name;
	private final TYPE type;

	public JMSDestinationInfo(JMSDestination destination) {
		this.name = destination.getName();
		this.type = destination.getType();
	}

	public String getName() {
		return name;
	}

	public TYPE getType() {
		return type;
	}
	
	public JMSDestination create(JMSDomain domain, JMSBroker broker) {
		if(getType() == TYPE.QUEUE)
			return domain.createQueue(broker, name);
		
		if(getType() == TYPE.TOPIC)
			return domain.createTopic(broker, name);
		
		throw new RuntimeException("Unable to create JMSDestination of type " + getType());
	}
}
