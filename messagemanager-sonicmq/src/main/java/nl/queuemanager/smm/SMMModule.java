package nl.queuemanager.smm;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.smm.ui.ConnectionTabPanel;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public class SMMModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(ConnectivityProviderPlugin.class).to(SonicMQConnectivityProvider.class);
		
		// The JMSDomain implementation for SonicMQ
		bind(JMSDomain.class).to(Domain.class).in(Scopes.SINGLETON);
		
		// Bind the UI tabs specific to SMM
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(10).to(ConnectionTabPanel.class);
		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
	}
	
}
