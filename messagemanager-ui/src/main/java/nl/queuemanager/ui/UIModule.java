package nl.queuemanager.ui;

import nl.queuemanager.core.jms.BrokerCredentialsProvider;
import nl.queuemanager.ui.message.MessageViewerModule;
import nl.queuemanager.ui.util.DesktopHelper;
import nl.queuemanager.ui.util.DesktopHelperJRE5;
import nl.queuemanager.ui.util.DesktopHelperJRE6;
import nl.queuemanager.ui.util.QueueCountsRefresher;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MessageViewerModule());

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
		
		try {
			Class.forName("java.awt.Desktop");
			bind(DesktopHelper.class).to(DesktopHelperJRE6.class).in(Scopes.SINGLETON);
		} catch (ClassNotFoundException e) {
			bind(DesktopHelper.class).to(DesktopHelperJRE5.class).in(Scopes.SINGLETON);
		}
		
	}
	
}
