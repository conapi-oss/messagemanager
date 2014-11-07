package nl.queuemanager.core.configuration;

import javax.inject.Singleton;

import nl.queuemanager.core.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class XmlConfigurationModule extends AbstractModule {

	private final String configFile;
	private final String namespaceUri;
	
	public XmlConfigurationModule(final String configFile, final String namespaceUri) {
		this.configFile = configFile;
		this.namespaceUri = namespaceUri;
	}
	
	public void configure() {
	}
	
	@Provides @Singleton
	public Configuration createConfiguration() {
		return new XmlConfiguration(configFile, namespaceUri);
	}

}
