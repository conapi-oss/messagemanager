package nl.queuemanager.core.platform;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class PlatformHelper {
	
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
	 * within this application should use the application-specific {@link getDataFolder}.
	 */
	protected File getUserDataFolder() {
		return new File(System.getProperty("user.home"));
	}
	
	public File getDataFolder() {
		return new File(getUserDataFolder(), "MessageManager");
	}
	
}
