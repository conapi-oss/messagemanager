package nl.queuemanager.app;

import com.google.common.base.Strings;
import nl.queuemanager.Profile;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.ui.settings.SettingsPanel;

import jakarta.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

@SuppressWarnings("serial")
public class AutoloadProfileSettingsPanel extends JPanel implements SettingsPanel {

	private final CoreConfiguration config;
	private final ProfileManager profileManager;
	
	private final JComboBox<Profile> profileCombo;
	private final JCheckBox autoloadProfileCheckBox;
	
	@Inject
	public AutoloadProfileSettingsPanel(CoreConfiguration config, ProfileManager profileManager) {
		this.config = config;
		this.profileManager = profileManager;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JLabel lblAutomaticallyLoadProfile = new JLabel("Automatically Load Profile:");
		add(lblAutomaticallyLoadProfile);
		
		autoloadProfileCheckBox = new JCheckBox("");
		autoloadProfileCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableDisableCombo();
			}
		});
		add(autoloadProfileCheckBox);
		
		profileCombo = new JComboBox<Profile>();
		add(profileCombo);
	}
	
	private void enableDisableCombo() {
		profileCombo.setEnabled(autoloadProfileCheckBox.isSelected());
	}
	
	public void readSettings() {
		Set<Profile> profiles = profileManager.getAllProfiles();
		profileCombo.removeAllItems();
		for(Profile p: profiles) {
			profileCombo.addItem(p);
		}
		
		final String autoloadProfileId = config.getUserPref(ProfileTabPanel.PREF_AUTOLOAD_PROFILE, null);
		if(!Strings.isNullOrEmpty(autoloadProfileId)) {
			autoloadProfileCheckBox.setSelected(true);
			for(Profile p: profileManager.getAllProfiles()) {
				if(p.getId().equals(autoloadProfileId)) {
					profileCombo.setSelectedItem(p);
				}
			}
		} else {
			autoloadProfileCheckBox.setSelected(false);
		}
		
		enableDisableCombo();
	}
	
	public void saveSettings() {
		if(autoloadProfileCheckBox.isSelected()) {
			config.setUserPref(ProfileTabPanel.PREF_AUTOLOAD_PROFILE, ((Profile)profileCombo.getSelectedItem()).getId());
		} else {
			config.setUserPref(ProfileTabPanel.PREF_AUTOLOAD_PROFILE, null);
		}
	}

	public JComponent getUIPanel() {
		return this;
	}

}
