package nl.queuemanager.core.platform;

import java.io.File;

import javax.swing.JFrame;

public abstract class PlatformHelper {
	
	public void setApplicationName(String name) {
	}

	public void setFullScreenEnabled(JFrame frame, boolean enabled) {
	}

	public void setBadge(String badge) {
	}

	public abstract File getUserDataFolder();
	
	public File getDataFolder() {
		return new File(getUserDataFolder(), "MessageManager");
	}
	
}
