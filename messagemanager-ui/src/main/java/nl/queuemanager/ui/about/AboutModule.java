package nl.queuemanager.ui.about;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

public class AboutModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(AboutTabPanel.class).in(Scopes.SINGLETON);

		MapBinder<String, AboutPanel> mapbinder = MapBinder.newMapBinder(binder(), String.class, AboutPanel.class);
		mapbinder.addBinding("Version").to(VersionPanel.class);
		mapbinder.addBinding("Customer Support").to(SupportPanel.class);
	}
	
}
