package nl.queuemanager.ui;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import nl.queuemanager.ui.message.MessageViewerModule;
import nl.queuemanager.ui.util.QueueCountsRefresher;

public class UIModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MessageViewerModule());
		// install(new SettingsModule());

		// Assisted inject for JMSDestinationTransferHandlers
		install(new FactoryModuleBuilder().build(JMSDestinationTransferHandlerFactory.class));
		
		install(new FactoryModuleBuilder().build(JMSSubscriberFactory.class));
				
		// Refresh queue counts from one central object to prevent refreshing the same broker more than once
		bind(QueueCountsRefresher.class).in(Scopes.SINGLETON);
		
//		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
//		tabsBinder.addBinding(20).to(QueuesTabPanel.class);
//		tabsBinder.addBinding(30).to(TopicSubscriberTabPanel.class);
//		tabsBinder.addBinding(40).to(MessageSendTabPanel.class);
	}
	
}
