package nl.queuemanager.app;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import lombok.extern.java.Log;
import nl.queuemanager.app.tasks.TaskFactory;
import nl.queuemanager.core.DebugProperty;
import nl.queuemanager.ui.settings.SettingsPanel;

@Log
public class AppModule extends AbstractModule {
	
	@Override
	protected void configure() {
		if(DebugProperty.developer.isEnabled()) {
			bindListener(Matchers.any(), new TypeListener() {
	            public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
	                typeEncounter.register(new InjectionListener<I>() {
	                    public void afterInjection(I i) {
							if( i.getClass().getName().startsWith("nl.queuemanager") ||
									i.getClass().getName().startsWith("at.conapi") ) {
	                    		log.finest("Guice created " + i.getClass().getSimpleName() + "@" + i.hashCode());
	                    	}
	                    }
	                });
	            }
	        });
		}

		install(new FactoryModuleBuilder().build(TaskFactory.class));
		
		MapBinder<String, SettingsPanel> mapbinder = MapBinder.newMapBinder(binder(), String.class, SettingsPanel.class);
		mapbinder.addBinding("Profile").to(AutoloadProfileSettingsPanel.class);
	}

}
