package nl.queuemanager.core;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import nl.queuemanager.core.tasks.PreconnectTaskFactory;

public class PreconnectCoreModule extends AbstractModule {
	
	@Override
	protected void configure() {
		
		install(new FactoryModuleBuilder().build(PreconnectTaskFactory.class));

	}

}
