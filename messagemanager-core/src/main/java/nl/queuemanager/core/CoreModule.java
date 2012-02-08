package nl.queuemanager.core;

import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;
import nl.queuemanager.core.tasks.ConnectToBrokerTask;
import nl.queuemanager.core.tasks.ConnectToBrokerTaskFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MultiQueueTaskExecutorModule());

		// Assisted inject for ConnectToBrokerTask objects
		install(new FactoryModuleBuilder()
			.implement(ConnectToBrokerTask.class, ConnectToBrokerTask.class)
			.build(ConnectToBrokerTaskFactory.class));
		
		bind(GlobalDomainEventListener.class).in(Scopes.SINGLETON);
	}

}
