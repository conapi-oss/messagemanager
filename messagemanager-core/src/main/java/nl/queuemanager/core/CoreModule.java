package nl.queuemanager.core;

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
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
            public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                typeEncounter.register(new InjectionListener<I>() {
                    public void afterInjection(I i) {
                        eventBus.register(i);
                    }
                });
            }
        });
		
		install(new MultiQueueTaskExecutorModule());

		// Install the TaskFactory for creating tasks using assisted inject
		install(new FactoryModuleBuilder().build(TaskFactory.class));
		
		bind(GlobalDomainEventListener.class).in(Scopes.SINGLETON);
	}

}
