package nl.queuemanager.activemq;

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public class ActiveMQModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(JMSDomain.class).to(ActiveMQDomain.class).in(Scopes.SINGLETON);
		
		/**
		 * Make this a singleton because it's requested from Main and the MapBinder below.
		 * This should really be replaced by some sort of event publishing mechanism during
		 * a major refactoring of the events mechanism in the application.
		 */
		bind(ConnectionTabPanel.class).in(Scopes.SINGLETON);
		
		// Bind the UI tabs specific to AMM
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(0).to(ConnectionTabPanel.class);
	}

}
