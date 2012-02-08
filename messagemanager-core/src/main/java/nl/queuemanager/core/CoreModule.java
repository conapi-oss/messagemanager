package nl.queuemanager.core;

import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;
import nl.queuemanager.core.tasks.TaskFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MultiQueueTaskExecutorModule());

		// Install the TaskFactory for creating tasks using assisted inject
		install(new FactoryModuleBuilder().build(TaskFactory.class));
		
		bind(GlobalDomainEventListener.class).in(Scopes.SINGLETON);
	}

}
