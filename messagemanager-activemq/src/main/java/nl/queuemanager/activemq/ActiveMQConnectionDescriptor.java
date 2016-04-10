package nl.queuemanager.activemq;

import java.util.UUID;

public class ActiveMQConnectionDescriptor {

	public static final String DESCRIPTION = "description";
	public static final String URL = "url";
	
	private final String key;
	private final String description;
	private final String jmxUrl;
	
	public ActiveMQConnectionDescriptor(String description, String jmxUrl) {
		this(UUID.randomUUID().toString(), description, jmxUrl);
	}
	
	public ActiveMQConnectionDescriptor(String key, String description, String jmxUrl) {
		this.key = key;
		this.description = description;
		this.jmxUrl = jmxUrl;
	}
	
	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	public String getJmxUrl() {
		return jmxUrl;
	}
}
