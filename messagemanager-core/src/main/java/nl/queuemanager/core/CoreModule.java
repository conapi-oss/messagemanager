package nl.queuemanager.core;

import nl.queuemanager.core.task.MultiQueueTaskExecutorModule;

import com.google.inject.AbstractModule;

public class CoreModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MultiQueueTaskExecutorModule());
	}

}
