package nl.queuemanager.solace;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

public class SolaceModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(TaskFactory.class));
		bind(ConnectivityProviderPlugin.class).to(SolaceConnectivityProvider.class);
		
		bind(JMSDomain.class).to(SolaceDomain.class).in(Scopes.SINGLETON);
		
		// Bind the UI tabs specific to Solace
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(10).to(SolaceConnectionTabPanel.class);
		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
	}
	
}
