package nl.queuemanager.ui.settings;

import com.google.inject.Inject;
import nl.queuemanager.ui.UITab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

@SuppressWarnings("serial")
public class SettingsTabPanel extends JPanel implements UITab {

	@Inject
	public SettingsTabPanel(Map<String, SettingsPanel> settingsPages) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		for(Map.Entry<String, SettingsPanel> entry: settingsPages.entrySet()) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
			JPanel uiPanel = (JPanel) entry.getValue().getUIPanel();
			panel.add(uiPanel);
			panel.add(createActionPanel(entry.getValue()));
			panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
			add(panel);
		}
		
	}
	
	private JPanel createActionPanel(final SettingsPanel settingsPanel) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settingsPanel.saveSettings();
			}
		});
		JButton revertButton = new JButton("Revert");
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settingsPanel.readSettings();
			}
		});
		
		// Initially read settings
		settingsPanel.readSettings();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(saveButton);
		panel.add(revertButton);
		return panel;
	}

	public String getUITabName() {
		return "Settings";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
				ConnectionState.CONNECTED,
				ConnectionState.DISCONNECTED
		};
	}
	
}
