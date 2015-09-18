package nl.queuemanager.core.platform;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Inject;
import javax.swing.JFrame;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.google.common.eventbus.EventBus;

//@SuppressWarnings("restriction")
public class PlatformHelperOSX extends PlatformHelper implements AboutHandler, PreferencesHandler {

	public static final short kUserDomain = -32763; /* Read/write. Resources that are private to the user.*/
	public static final short kSystemDomain = -32766; /* Read-only system hierarchy.*/
	public static final short kLocalDomain = -32765; /* All users of a single machine have access to these resources.*/
	public static final short kNetworkDomain = -32764; /* All users configured to use a common network server has access to these resources.*/
	
	public static final String kSystemFolderType = "macs"; /* the system folder */
	public static final String kDesktopFolderType = "desk"; /* the desktop folder; objects in this folder show on the desk top. */
	public static final String kSystemDesktopFolderType = "sdsk"; /* the desktop folder at the root of the hard drive, never the redirected user desktop folder */
	public static final String kTrashFolderType = "trsh"; /* the trash folder; objects in this folder show up in the trash */
	public static final String kSystemTrashFolderType = "strs"; /* the trash folder at the root of the drive, never the redirected user trash folder */
	public static final String kWhereToEmptyTrashFolderType = "empt"; /* the "empty trash" folder; Finder starts empty from here down */
	public static final String kPrintMonitorDocsFolderType = "prnt"; /* Print Monitor documents */
	public static final String kStartupFolderType = "strt"; /* Finder objects (applications, documents, DAs, aliases, to...) to open at startup go here */
	public static final String kShutdownFolderType = "shdf"; /* Finder objects (applications, documents, DAs, aliases, to...) to open at shutdown go here */
	public static final String kAppleMenuFolderType = "amnu"; /* Finder objects to put into the Apple menu go here */
	public static final String kControlPanelFolderType = "ctrl"; /* Control Panels go here (may contain INITs) */
	public static final String kSystemControlPanelFolderType = "sctl"; /* System control panels folder - never the redirected one, always "Control Panels" inside the System Folder */
	public static final String kExtensionFolderType = "extn"; /* System extensions go here */
	public static final String kFontsFolderType = "font"; /* Fonts go here */
	public static final String kPreferencesFolderType = "pref"; /* preferences for applications go here */
	public static final String kSystemPreferencesFolderType = "sprf"; /* System-type Preferences go here - this is always the system's preferences folder, never a logged in user's */
	public static final String kTemporaryFolderType = "temp"; /* temporary files go here (deleted periodically, but don't rely on it.) */
	public static final String kApplicationSupportFolderType = "asup"; /* third-party items and folders */
	
	private final Application application;
	private final EventBus eventBus;
	
	@Inject
	public PlatformHelperOSX(EventBus eventBus) {
		this.eventBus = eventBus;
		this.application = Application.getApplication();
		//application.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
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
		// FullScreenUtilities.setWindowCanFullScreen(frame, enabled);
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
	
	public File getUserDataFolder() {
		try {
			return new File(com.apple.eio.FileManager.findFolder(kUserDomain, getIntegerFromOsType(kApplicationSupportFolderType)));
		} catch (FileNotFoundException e) {
			return new File(".messagemanager");
		}
	}

	public static int getIntegerFromOsType(String typeStr) {
		if (typeStr == null || typeStr.length() != 4) {
			return 0;
		}
		
		int type = 0;
		byte[] bytes = typeStr.getBytes();
		for (int i = 0; i < 4; i++) {
			int t = (int) bytes[i];
			t &= 0xFF;
			t <<= (3 - i) * 8;
			type |= t;

		}
		
		return type;
	}

}
