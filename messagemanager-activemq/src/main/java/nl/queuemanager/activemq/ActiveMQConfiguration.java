package nl.queuemanager.activemq;

import java.util.List;

import com.google.inject.Inject;

import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.util.CollectionFactory;

public class ActiveMQConfiguration {

	private static final String CONNECTIONS_SECTION = "Connections";
	
	private final Configuration config;
	
	@Inject
	public ActiveMQConfiguration(Configuration config) {
		this.config = config;
	}

	public void saveConnectionDescriptor(ActiveMQConnectionDescriptor con) {
		Configuration sec = config.sub(CONNECTIONS_SECTION);
		Configuration cd = sec.sub(con.getKey());
		cd.setValue(ActiveMQConnectionDescriptor.DESCRIPTION, con.getDescription());
		cd.setValue(ActiveMQConnectionDescriptor.URL, con.getDescription());
	}
	
	public List<ActiveMQConnectionDescriptor> listConnectionDescriptors() {
		Configuration connections = config.sub(CONNECTIONS_SECTION);
		List<String> keys = connections.listKeys();
		List<ActiveMQConnectionDescriptor> res = CollectionFactory.newArrayList();
		for(String key: config.listKeys()) {
			Configuration subSection = config.sub(key);
			ActiveMQConnectionDescriptor cd = new ActiveMQConnectionDescriptor(key,
					subSection.getValue(ActiveMQConnectionDescriptor.DESCRIPTION, null), 
					subSection.getValue(ActiveMQConnectionDescriptor.URL, null));
			res.add(cd);
		}
		return res;
	}

}
