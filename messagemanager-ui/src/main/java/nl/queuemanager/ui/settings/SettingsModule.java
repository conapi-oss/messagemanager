package nl.queuemanager.ui.settings;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public class SettingsModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SettingsTabPanel.class).in(Scopes.SINGLETON);
		
		MapBinder<String, SettingsPanel> mapbinder = MapBinder.newMapBinder(binder(), String.class, SettingsPanel.class);
		mapbinder.addBinding("General").to(GeneralSettingsPanel.class);
		mapbinder.addBinding("Brokers").to(BrokerSettingsPanel.class);
	}
	
}
