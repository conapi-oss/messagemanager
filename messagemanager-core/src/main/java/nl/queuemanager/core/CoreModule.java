package nl.queuemanager.core;

import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;
import nl.queuemanager.core.tasks.ConnectToBrokerTask;
import nl.queuemanager.core.tasks.ConnectToBrokerTaskFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryProvider;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MultiQueueTaskExecutorModule());
		
		bind(ConnectToBrokerTaskFactory.class).toProvider(FactoryProvider.newFactory(
				ConnectToBrokerTaskFactory.class, ConnectToBrokerTask.class));
		
		bind(GlobalDomainEventListener.class).in(Scopes.SINGLETON);
	}

}
