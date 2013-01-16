package nl.queuemanager.ui;

import nl.queuemanager.core.jms.BrokerCredentialsProvider;
import nl.queuemanager.ui.message.MessageViewerModule;
import nl.queuemanager.ui.settings.SettingsModule;
import nl.queuemanager.ui.util.DesktopHelper;
import nl.queuemanager.ui.util.DesktopHelperJRE5;
import nl.queuemanager.ui.util.DesktopHelperJRE6;
import nl.queuemanager.ui.util.QueueCountsRefresher;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

public class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MessageViewerModule());
		install(new SettingsModule());

		// Assisted inject for JMSDestinationTransferHandlers
		install(new FactoryModuleBuilder()
			.implement(JMSDestinationTransferHandler.class, JMSDestinationTransferHandler.class)
			.build(JMSDestinationTransferHandlerFactory.class));
		
		// The broker credentials provider
		bind(BrokerCredentialsProvider.class).to(BrokerCredentialsDialog.class).in(Scopes.SINGLETON);
		
		// To display errors to the user, we need a global error listener for the task executor
		bind(TaskErrorListener.class).in(Scopes.SINGLETON);
		
		// Refresh queue counts from one central object to prevent refreshing the same broker more than once
		bind(QueueCountsRefresher.class).in(Scopes.SINGLETON);
		
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		//tabsBinder.addBinding(10).to(ConnectionTabPanel.class);
		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
		
		try {
			Class.forName("java.awt.Desktop");
			bind(DesktopHelper.class).to(DesktopHelperJRE6.class).in(Scopes.SINGLETON);
		} catch (ClassNotFoundException e) {
			bind(DesktopHelper.class).to(DesktopHelperJRE5.class).in(Scopes.SINGLETON);
		}
		
	}
	
}
