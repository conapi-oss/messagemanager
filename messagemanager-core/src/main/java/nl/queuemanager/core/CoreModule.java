package nl.queuemanager.core;

import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.PlatformHelperOSX;
import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;
import nl.queuemanager.core.tasks.TaskFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class CoreModule extends AbstractModule {

	private final EventBus eventBus = new EventBus("Default EventBus");
	
	@Override
	protected void configure() {
		// Bind the global EventBus and register all Guice created objects with it
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
            public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                typeEncounter.register(new InjectionListener<I>() {
                    public void afterInjection(I i) {
                    	// Only register objects in our own package(s) to prevent accidentally
                    	// sending events to places they shouldn't go
                    	if(i.getClass().getName().startsWith("nl.queuemanager")) {
	                        eventBus.register(i);
	                        System.out.println("Registered " + i.getClass().getName() + "@" + i.hashCode());
                    	}
                    }
                });
            }
        });
		
		install(new MultiQueueTaskExecutorModule());

		// Install the TaskFactory for creating tasks using assisted inject
		install(new FactoryModuleBuilder().build(TaskFactory.class));
		
		bind(GlobalDomainEventListener.class).in(Scopes.SINGLETON);
		bind(DeadEventListener.class).in(Scopes.SINGLETON);
		
		if(isOSX()) {
			bind(PlatformHelper.class).to(PlatformHelperOSX.class).in(Scopes.SINGLETON);
		}
	}

	// From https://developer.apple.com/library/mac/technotes/tn2002/tn2110.html
	private static boolean isOSX() {
	    String osName = System.getProperty("os.name");
	    return osName.contains("OS X");
	}
}
