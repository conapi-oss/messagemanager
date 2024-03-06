package nl.queuemanager.core.platform;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

import jakarta.inject.Inject;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.io.File;

public class PlatformHelper {
	// FIXME REALLY REALLY REALLY BAD!!
	public static PlatformHelper platformHelper;

	private final EventBus eventBus;

	@Inject
	public PlatformHelper(EventBus eventBus) {
		PlatformHelper.platformHelper = this;

		this.eventBus = eventBus;

		var desktop = Desktop.getDesktop();

		if(desktop.isSupported(Desktop.Action.APP_ABOUT))
			desktop.setAboutHandler(this::handleAbout);

		if(desktop.isSupported(Desktop.Action.APP_PREFERENCES))
			desktop.setPreferencesHandler(this::handlePreferences);

		if(desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER))
			desktop.setQuitHandler(this::handleQuit);
	}

	private void handleAbout(java.awt.desktop.AboutEvent aboutEvent) {
		eventBus.post(new AboutEvent());
	}

	private void handlePreferences(PreferencesEvent preferencesEvent) {
		eventBus.post(new PreferencesEvent());
	}

	private void handleQuit(QuitEvent quitEvent, QuitResponse quitResponse) {
		eventBus.post(new nl.queuemanager.core.platform.QuitEvent() {
			public void quit() {
				quitResponse.performQuit();
			}
		});
	}

	public void setApplicationName(String name) {

	}

	public void setFullScreenEnabled(JFrame frame, boolean enabled) {
	}

	public void setBadge(String badge) {
	}
	
	public File[] chooseFiles(final JComponent parent, final String approveButtonText, final boolean allowMultiple, final FileFilter filter) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(allowMultiple);
		if(fileChooser.showDialog(parent, approveButtonText) == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFiles();
		}
		
		return null;
	}

	/**
	 * Return the non application specific directory where programs can store data 
	 * (AppData on Windows, ~/Library/Application Support on Mac, etc). Consumers
	 * within this application should use the application-specific {@link #getDataFolder}.
	 */
	protected File getUserDataFolder() {
		String configPath = System.getenv("XDG_CONFIG_DIR");
		if(Strings.isNullOrEmpty(configPath)) {
			configPath = System.getenv("APPDATA");
		}

		if(Strings.isNullOrEmpty(configPath)) {
			File userHome = new File(System.getProperty("user.home"));
			return new File(userHome, ".config");
		} else {
			return new File(configPath);
		}
	}
	
	public File getDataFolder() {
		return new File(getUserDataFolder(), "MessageManager");
	}
	
}
