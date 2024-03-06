package nl.queuemanager.activemq;

import com.google.common.eventbus.EventBus;
import nl.queuemanager.AddUITabEvent;
import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.ui.UITab;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Map.Entry;

public class ActiveMQConnectivityProvider implements ConnectivityProviderPlugin {

	private final EventBus eventBus;
	private final Map<Integer, UITab> tabs;

	@Inject
	public ActiveMQConnectivityProvider(EventBus eventBus, Map<Integer, UITab> tabs) {
		this.eventBus = eventBus;
		this.tabs = tabs;
	}
	
	@Override
	public void initialize() {
		for(Entry<Integer, UITab> entry: tabs.entrySet()) {
			eventBus.post(new AddUITabEvent(entry.getKey(), entry.getValue()));
		}
	}
	
}
