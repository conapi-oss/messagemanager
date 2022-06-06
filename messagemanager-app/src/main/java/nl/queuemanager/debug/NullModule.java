package nl.queuemanager.debug;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import nl.queuemanager.core.jms.JMSDomain;

public class NullModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(JMSDomain.class).to(NullDomain.class).in(Scopes.SINGLETON);
	}

}
