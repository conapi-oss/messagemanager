package nl.queuemanager.ui.settings;

import javax.swing.JComponent;

public interface SettingsPanel {

	public JComponent getUIPanel();
	
	public void readSettings();
	
	public void saveSettings();
	
}
