package nl.queuemanager.activemq;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.activemq.incompat.VirtualMachineProcessFinder;
import nl.queuemanager.activemq.ui.ConnectionTabPanel;
import nl.queuemanager.activemq.ui.JavaProcessFinder;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public class ActiveMQModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ConnectivityProviderPlugin.class).to(ActiveMQConnectivityProvider.class);
		
		bind(JMSDomain.class).to(ActiveMQDomain.class).in(Scopes.SINGLETON);

		try {
			Class.forName("com.sun.tools.attach.VirtualMachine");
			bind(JavaProcessFinder.class).to(VirtualMachineProcessFinder.class).in(Scopes.SINGLETON);
		} catch (ClassNotFoundException e) {
			// Class not supported on this JVM. Don't load the local process finder.
			bind(JavaProcessFinder.class).to(DummyProcessFinder.class).in(Scopes.SINGLETON);
		}
		
		// Bind the UI tabs specific to AMM
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(10).to(ConnectionTabPanel.class);
		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
	}

}
