package nl.queuemanager.solace;

import java.net.Authenticator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import nl.queuemanager.AddUITabEvent;
import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;

class SolaceConnectivityProvider implements ConnectivityProviderPlugin {

	private final EventBus eventBus;
	private final Map<Integer, UITab> tabs;

	@Inject
	public SolaceConnectivityProvider(EventBus eventBus, Map<Integer, UITab> tabs) {
		this.eventBus = eventBus;
		this.tabs = tabs;
	}
	
	@Override
	public void initialize() {
		// Disable the JVM built-in authenticator because we want to handle
		// 401 responses ourselves
		Authenticator.setDefault(null);
		
		for(Entry<Integer, UITab> entry: tabs.entrySet()) {
			eventBus.post(new AddUITabEvent(entry.getKey(), entry.getValue()));
		}
	}
	
}
