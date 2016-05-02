package nl.queuemanager.ui.settings;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.ui.util.JIntegerField;

import com.google.inject.Inject;

@SuppressWarnings("serial")
class GeneralSettingsPanel extends JPanel implements SettingsPanel {

	private final CoreConfiguration config;
	private JIntegerField refreshIntervalField;
	
	@Inject
	public GeneralSettingsPanel(CoreConfiguration config) {
		this.config = config;
		
		add(createForm());
	}

	public void readSettings() {
		refreshIntervalField.setValue(Integer.parseInt(config.getUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, "5000")));
	}
	
	public void saveSettings() {
		config.setUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, Integer.toString(refreshIntervalField.getValue()));
	}

	public JComponent getUIPanel() {
		return this;
	}

	private JComponent createForm() {
		JPanel panel = new JPanel();
		
		refreshIntervalField = new JIntegerField(10);
		panel.add(createLabelFor(refreshIntervalField, "Message count refresh interval (ms)"));
		panel.add(refreshIntervalField);
		
		return panel;
	}
	
	private Component createLabelFor(JComponent parent, String text) {
		JLabel label = new JLabel(text);
		label.setLabelFor(parent);
		return label;
	}
	
}
