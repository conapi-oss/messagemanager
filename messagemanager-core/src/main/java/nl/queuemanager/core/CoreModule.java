package nl.queuemanager.core;

import nl.queuemanager.core.task.MultiQueueTaskExecutor;
import nl.queuemanager.core.task.TaskExecutor;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaskExecutor.class).to(MultiQueueTaskExecutor.class).in(Scopes.SINGLETON);
	}

}
