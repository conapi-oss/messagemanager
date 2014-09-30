package nl.queuemanager;

import nl.queuemanager.core.jms.JMSDomain;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class NullModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(JMSDomain.class).to(NullDomain.class).in(Scopes.SINGLETON);
	}

}
