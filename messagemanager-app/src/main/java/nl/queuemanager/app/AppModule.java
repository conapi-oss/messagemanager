package nl.queuemanager.app;

import nl.queuemanager.app.tasks.TaskFactory;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(TaskFactory.class));
	}

}
