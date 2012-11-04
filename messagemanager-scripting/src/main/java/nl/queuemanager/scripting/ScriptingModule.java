package nl.queuemanager.scripting;

import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class ScriptingModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(45).to(ScriptingConsoleTab.class);
	}

}
