package nl.queuemanager.smm;

import nl.queuemanager.core.Configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SMMConfigurationModule extends AbstractModule {

	public void configure() {
		// We use an Xml file for configuration
		bind(XmlConfiguration.class).in(Scopes.SINGLETON);
		bind(Configuration.class).to(XmlConfiguration.class);
		bind(SMMConfiguration.class).to(XmlConfiguration.class);
	}

}
