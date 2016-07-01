package nl.queuemanager.ui;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import nl.queuemanager.ui.util.DesktopHelper;
import nl.queuemanager.ui.util.DesktopHelperJRE6;

public class PreconnectUIModule extends AbstractModule {

	@Override
	protected void configure() {
		// To display errors to the user, we need a global error listener for the task executor
		bind(TaskErrorListener.class).in(Scopes.SINGLETON);
				
		bind(DesktopHelper.class).to(DesktopHelperJRE6.class).in(Scopes.SINGLETON);
	}
	
}
