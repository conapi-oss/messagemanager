package nl.queuemanager.ui;

import nl.queuemanager.ui.util.DesktopHelper;
import nl.queuemanager.ui.util.DesktopHelperJRE5;
import nl.queuemanager.ui.util.DesktopHelperJRE6;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class PreconnectUIModule extends AbstractModule {

	@Override
	protected void configure() {
		// To display errors to the user, we need a global error listener for the task executor
		bind(TaskErrorListener.class).in(Scopes.SINGLETON);
				
		try {
			Class.forName("java.awt.Desktop");
			bind(DesktopHelper.class).to(DesktopHelperJRE6.class).in(Scopes.SINGLETON);
		} catch (ClassNotFoundException e) {
			bind(DesktopHelper.class).to(DesktopHelperJRE5.class).in(Scopes.SINGLETON);
		}
		
	}
	
}
