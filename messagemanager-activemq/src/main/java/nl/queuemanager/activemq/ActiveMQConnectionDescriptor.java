package nl.queuemanager.activemq;

public class ActiveMQConnectionDescriptor {

	private final String description;
	private final String jmxUrl;
	
	public ActiveMQConnectionDescriptor(String description, String jmxUrl) {
		this.description = description;
		this.jmxUrl = jmxUrl;
	}

	public String getDescription() {
		return description;
	}

	public String getJmxUrl() {
		return jmxUrl;
	}
}
