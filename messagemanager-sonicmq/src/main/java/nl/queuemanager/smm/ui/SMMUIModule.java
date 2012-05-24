package nl.queuemanager.smm.ui;

import nl.queuemanager.ui.UITab;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

public class SMMUIModule extends AbstractModule {

	@Override
	protected void configure() {
		/**
		 * Make this a singleton because it's requested from Main and the MapBinder below.
		 * This should really be replaced by some sort of event publishing mechanism during
		 * a major refactoring of the events mechanism in the application.
		 */
		bind(ConnectionTabPanel.class).in(Scopes.SINGLETON);
		
		// Bind the UI tabs specific to SMM
		MapBinder<Integer, UITab> tabsBinder = MapBinder.newMapBinder(binder(), Integer.class, UITab.class);
		tabsBinder.addBinding(0).to(ConnectionTabPanel.class);
		tabsBinder.addBinding(Integer.MAX_VALUE).to(HelpTabPanel.class);
		
		/**
		 * The SMMFrame is the frame needed by the Sonic Management gui code to bind to
		 * SMMFrame extends JMAFrame to make those widgets happy. We register the frame
		 * here to prevent from having to pass a reference to it around.
		 *
		 * We bind SMMFrame directly instead of JMAFrame because Guice is exact in its bindings.
		 * When we bind JMAFrame and someone wants an SMMFrame, Guice will create another instance.
		 * Because we only want one instance, we choose not to bind JMAFrame at all, causing this
		 * kind of request to crash instead of introduce weird bugs.
		 */
		bind(SMMFrame.class).in(Scopes.SINGLETON);
		
		/** 
		 * The connection dialog cannot be bound directly because we aren't able to add the @Inject
		 * annotation to it, we don't have the sourcecode. Therefore we use this provider instead.
		 */
		bind(JDomainConnectionDialog.class).toProvider(ConnectionDialogProvider.class);
	}

}
