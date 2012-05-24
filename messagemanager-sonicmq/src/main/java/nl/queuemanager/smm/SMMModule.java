package nl.queuemanager.smm;

import nl.queuemanager.core.jms.JMSDomain;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class SMMModule extends AbstractModule {
	
	@Override
	protected void configure() {
		// The JMSDomain implementation for SonicMQ
		bind(JMSDomain.class).to(Domain.class).in(Scopes.SINGLETON);
	}
	
}
