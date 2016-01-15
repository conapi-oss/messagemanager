package nl.queuemanager.smm;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.smm.ui.ConnectionDialogProvider;
import nl.queuemanager.smm.ui.ConnectionTabPanel;
import nl.queuemanager.smm.ui.SMMFrame;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

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
		
		/**
		 * The SMMFrame is the frame needed by the Sonic Management gui code to bind to.
		 * SMMFrame extends JMAFrame to make those widgets happy. We register the frame
		 * here to prevent from having to pass a reference to it around.
		 *
		 * We bind SMMFrame directly instead of JMAFrame because Guice is exact in its bindings.
		 * When we bind JMAFrame and someone wants an SMMFrame, Guice will create another instance.
		 * Because we only want one instance, we choose not to bind JMAFrame at all, causing this
		 * kind of request to crash instead of introduce weird bugs.
		 */
		bind(SMMFrame.class).in(Scopes.SINGLETON);
		
		/** 
		 * The connection dialog cannot be bound directly because we aren't able to add the @Inject
		 * annotation to it, we don't have the source code. Therefore we use this provider instead.
		 */
		bind(JDomainConnectionDialog.class).toProvider(ConnectionDialogProvider.class).in(Scopes.SINGLETON);
	}
	
}
