package nl.queuemanager.ui.settings;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.ui.util.JIntegerField;
import nl.queuemanager.ui.util.SpringUtilities;

import com.google.inject.Inject;

@SuppressWarnings("serial")
class GeneralSettingsPanel extends JPanel implements SettingsPanel {

	private final CoreConfiguration config;
	private JIntegerField maxBufferedMessagesField;
	private JIntegerField refreshIntervalField;
	
	@Inject
	public GeneralSettingsPanel(CoreConfiguration config) {
		this.config = config;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		add(createForm());
		add(Box.createHorizontalGlue());
	}

	public void readSettings() {
		refreshIntervalField.setValue(Integer.parseInt(config.getUserPref(
				CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, 
				CoreConfiguration.DEFAULT_AUTOREFRESH_INTERVAL)));
		maxBufferedMessagesField.setValue(Integer.parseInt(config.getUserPref(
				CoreConfiguration.PREF_MAX_BUFFERED_MSG,
				CoreConfiguration.DEFAULT_MAX_BUFFERED_MSG)));
	}
	
	public void saveSettings() {
		config.setUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, Integer.toString(refreshIntervalField.getValue()));
		config.setUserPref(CoreConfiguration.PREF_MAX_BUFFERED_MSG, Integer.toString(maxBufferedMessagesField.getValue()));
	}

	public JComponent getUIPanel() {
		return this;
	}

	private JComponent createForm() {
		JPanel panel = new JPanel();
		panel.setLayout(new SpringLayout());
		
		refreshIntervalField = new JIntegerField(10);
		panel.add(createLabelFor(refreshIntervalField, "Message count refresh interval (ms)"));
		panel.add(refreshIntervalField);
		
		maxBufferedMessagesField = new JIntegerField(5);
		panel.add(createLabelFor(maxBufferedMessagesField, "Maximum number of messages to buffer for topic receivers"));
		panel.add(maxBufferedMessagesField);
		
		SpringUtilities.makeCompactGrid(panel, 2, 2, 0, 0, 5, 5);
		
		return panel;
	}
	
	private Component createLabelFor(JComponent parent, String text) {
		JLabel label = new JLabel(text);
		label.setLabelFor(parent);
		return label;
	}
	
}
