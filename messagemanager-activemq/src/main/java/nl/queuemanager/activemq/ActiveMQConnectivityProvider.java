package nl.queuemanager.activemq;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.app.AddUITabEvent;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;

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
