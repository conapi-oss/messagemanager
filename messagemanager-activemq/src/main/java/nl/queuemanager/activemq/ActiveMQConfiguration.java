package nl.queuemanager.activemq;

import java.util.List;

import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.CollectionFactory;

public class ActiveMQConfiguration {

	private final Configuration config;
	
	public ActiveMQConfiguration(Configuration config) {
		this.config = config;
	}
	
	public List<ActiveMQConnectionDescriptor> listConnectionDescriptors() {
		Configuration connections = config.sub("Connections");
		List<String> keys = connections.listKeys();
		List<ActiveMQConnectionDescriptor> res = CollectionFactory.newArrayList();
		for(String key:)
	}

}
