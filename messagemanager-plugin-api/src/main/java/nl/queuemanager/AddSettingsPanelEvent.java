package nl.queuemanager;


import nl.queuemanager.ui.settings.SettingsPanel;
import nl.queuemanager.ui.settings.SettingsPanelProvider;

public class AddSettingsPanelEvent implements SettingsPanelProvider {
	private final String name;
	private final SettingsPanel panel;

	public AddSettingsPanelEvent(String name, SettingsPanel panel) {
		this.name = name;
		this.panel = panel;
	}

	public String getName() {
		return name;
	}
	public SettingsPanel getPanel() {
		return panel;
	}

	public String toString() {
		return getClass().getName() + "(" + name + ")";
	}
}

