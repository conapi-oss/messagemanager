package nl.queuemanager.fakemq;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

public class FakeMQModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(JMSDomain.class).to(FakeMQDomain.class).in(Scopes.SINGLETON);
		bind(ConnectivityProviderPlugin.class).to(FakeMQConnectivityProvider.class);
		
		/**
		 * Make this a singleton because it's requested from Main and the MapBinder below.
		 * This should really be replaced by some sort of event publishing mechanism during
		 * a major refactoring of the events mechanism in the application.
		 */
		bind(ConnectionTabPanel.class).in(Scopes.SINGLETON);
		
		// Bind the UI tabs specific to SMM
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(10).to(ConnectionTabPanel.class);
		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
	}

}
