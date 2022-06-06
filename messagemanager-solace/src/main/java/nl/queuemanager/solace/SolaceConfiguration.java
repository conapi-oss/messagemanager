package nl.queuemanager.solace;

import com.google.inject.Inject;
import nl.queuemanager.core.configuration.Configuration;

class SolaceConfiguration {

	private static final String SOLACE_CONFIG_ROOT = "Solace";
	private static final String CONNECTIONS_SECTION = "Connections";
	
	private final Configuration config;
	
	@Inject
	public SolaceConfiguration(Configuration config) {
		this.config = config.sub(SOLACE_CONFIG_ROOT);
	}
	
	public Configuration getConnectionsConfigSection() {
		return config.sub(CONNECTIONS_SECTION);
	}

}
