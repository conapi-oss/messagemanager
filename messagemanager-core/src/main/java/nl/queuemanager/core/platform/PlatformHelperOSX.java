package nl.queuemanager.core.platform;

import javax.inject.Inject;
import javax.swing.JFrame;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.FullScreenUtilities;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitStrategy;
import com.google.common.eventbus.EventBus;

@SuppressWarnings("restriction")
public class PlatformHelperOSX extends PlatformHelper implements AboutHandler, PreferencesHandler {

	private final Application application;
	private final EventBus eventBus;
	
	@Inject
	public PlatformHelperOSX(EventBus eventBus) {
		this.eventBus = eventBus;
		this.application = Application.getApplication();
		application.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
		application.setAboutHandler(this);
		application.setPreferencesHandler(this);
	}

	/**
	 * Set application menu bar name. Seems not to work. Must be called before touching or even loading 
	 * anything in java.awt or setting Look and Feel. Perhaps a separate boot class that just sets this
	 * property and then calls main will have a chance.
	 * 
	 * Probably cannot exist in this class since it also loads some javax.swing (and thus java.awt) classes.
	 */
	public void setApplicationName(String name) {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Message Manager");
	}
	
	public void setFullScreenEnabled(JFrame frame, boolean enabled) {
		FullScreenUtilities.setWindowCanFullScreen(frame, enabled);
	}

	public void handleAbout(com.apple.eawt.AppEvent.AboutEvent e) {
		eventBus.post(new AboutEvent());
	}

	public void handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent e) {
		eventBus.post(new PreferencesEvent());
	}
	
	public void setBadge(String badge) {
		application.setDockIconBadge(badge);
	}

}
