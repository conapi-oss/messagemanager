package nl.queuemanager.core.configuration;

import nl.queuemanager.core.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class XmlConfigurationModule extends AbstractModule {

	@Override
	protected void configure() {
		// We use an Xml file for configuration
		bind(XmlConfiguration.class).in(Scopes.SINGLETON);
		bind(Configuration.class).to(XmlConfiguration.class);
	}

}
