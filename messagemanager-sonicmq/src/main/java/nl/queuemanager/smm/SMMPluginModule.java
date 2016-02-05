package nl.queuemanager.smm;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.smm.ui.ConnectionTabPanel;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

/**
 * Module for the plugin configuration of this project
 */
public class SMMPluginModule extends AbstractModule {
	
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
		
		/** 
		 * The connection dialog cannot be bound directly because we aren't able to add the @Inject
		 * annotation to it, we don't have the source code. Therefore we use this provider instead.
		 */
		bind(JDomainConnectionDialog.class).toProvider(ConnectionDialogProvider.class).in(Scopes.SINGLETON);
	}

	/**
	 * This provider class only exists because in the SonicMQ-only version of the program we cannot
	 * directly bind to JDomainConnectionDialog. We need delayed resolution because we need the JMAFrame
	 * to create this object properly. The plugin-based version of the program doesn't use a JMAFrame
	 * so we don't need this workaround anymore.
	 */
	static class ConnectionDialogProvider implements Provider<JDomainConnectionDialog> {
		@Override
		public JDomainConnectionDialog get() {
			return new JDomainConnectionDialog(null);
		}
	}
	
}
