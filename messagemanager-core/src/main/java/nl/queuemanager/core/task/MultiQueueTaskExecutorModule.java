package nl.queuemanager.core.task;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MultiQueueTaskExecutorModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TaskExecutor.class).to(MultiQueueTaskExecutor.class).in(Scopes.SINGLETON);
	}

}
