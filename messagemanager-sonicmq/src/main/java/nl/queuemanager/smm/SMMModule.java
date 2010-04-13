package nl.queuemanager.smm;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.jms.JMSDomain;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SMMModule extends AbstractModule {

	@Override
	protected void configure() {
		// We use an Xml file for configuration
		bind(XmlConfiguration.class).in(Scopes.SINGLETON);
		bind(Configuration.class).to(XmlConfiguration.class);
		bind(SMMConfiguration.class).to(XmlConfiguration.class);
		
		// The JMSDomain implementation for SonicMQ
		bind(JMSDomain.class).to(Domain.class).in(Scopes.SINGLETON);
	}

}
