package nl.queuemanager.ui;

import nl.queuemanager.ui.message.MessageViewerModule;
import nl.queuemanager.ui.util.DesktopHelper;
import nl.queuemanager.ui.util.DesktopHelperJRE5;
import nl.queuemanager.ui.util.DesktopHelperJRE6;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryProvider;

public class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MessageViewerModule());
		
		bind(JMSDestinationTransferHandlerFactory.class).toProvider(FactoryProvider.newFactory(
			JMSDestinationTransferHandlerFactory.class, JMSDestinationTransferHandler.class));
		
		try {
			Class.forName("java.awt.Desktop");
			bind(DesktopHelper.class).to(DesktopHelperJRE6.class);
		} catch (ClassNotFoundException e) {
			bind(DesktopHelper.class).to(DesktopHelperJRE5.class);
		}
		
	}

}
