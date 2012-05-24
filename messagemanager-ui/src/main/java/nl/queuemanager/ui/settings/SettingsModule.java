package nl.queuemanager.ui.settings;

import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class SettingsModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(50).to(SettingsTabPanel.class);
		
		MapBinder<String, SettingsPanel> mapbinder = MapBinder.newMapBinder(binder(), String.class, SettingsPanel.class);
		mapbinder.addBinding("General").to(GeneralSettingsPanel.class);
		mapbinder.addBinding("Brokers").to(BrokerSettingsPanel.class);
	}
	
}
