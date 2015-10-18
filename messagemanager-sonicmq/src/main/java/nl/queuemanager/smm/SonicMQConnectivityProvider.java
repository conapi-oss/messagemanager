package nl.queuemanager.smm;

import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import nl.queuemanager.AddUITabEvent;
import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;

public class SonicMQConnectivityProvider implements ConnectivityProviderPlugin {

	private final EventBus eventBus;
	private final Map<Integer, UITab> tabs;

	@Inject
	public SonicMQConnectivityProvider(EventBus eventBus, Map<Integer, UITab> tabs) {
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
