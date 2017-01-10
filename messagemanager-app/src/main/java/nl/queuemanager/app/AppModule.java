package nl.queuemanager.app;

import nl.queuemanager.app.tasks.TaskFactory;
import nl.queuemanager.ui.settings.SettingsPanel;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(TaskFactory.class));
		
		MapBinder<String, SettingsPanel> mapbinder = MapBinder.newMapBinder(binder(), String.class, SettingsPanel.class);
		mapbinder.addBinding("Profile").to(AutoloadProfileSettingsPanel.class);
	}

}
