package nl.queuemanager.ui.settings;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.ui.util.JIntegerField;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("serial")
class GeneralSettingsPanel extends JPanel implements SettingsPanel {
	
	private final CoreConfiguration config;
	private JIntegerField autoRefreshIntervalField;
	private JIntegerField maxBufferedMessagesField;
	private JTextField txtLicenseKey;
	private JComboBox<LookAndFeelInfo> lafCombo;
	private boolean termsConfirmed = false;
	
	@Inject
	public GeneralSettingsPanel(CoreConfiguration config) {
		setAlignmentY(Component.TOP_ALIGNMENT);
		this.config = config;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel autoRefreshIntervalLabel = new JLabel("Message Count Refresh Interval (ms):");
		autoRefreshIntervalLabel.setToolTipText("How often to refresh the number of messages on queues");
		GridBagConstraints gbc_autoRefreshIntervalLabel = new GridBagConstraints();
		gbc_autoRefreshIntervalLabel.anchor = GridBagConstraints.EAST;
		gbc_autoRefreshIntervalLabel.insets = new Insets(0, 0, 5, 5);
		gbc_autoRefreshIntervalLabel.gridx = 0;
		gbc_autoRefreshIntervalLabel.gridy = 0;
		add(autoRefreshIntervalLabel, gbc_autoRefreshIntervalLabel);
		
		autoRefreshIntervalField = new JIntegerField(10);
		autoRefreshIntervalLabel.setLabelFor(autoRefreshIntervalField);
		GridBagConstraints gbc_autoRefreshIntervalField = new GridBagConstraints();
		gbc_autoRefreshIntervalField.anchor = GridBagConstraints.WEST;
		gbc_autoRefreshIntervalField.insets = new Insets(0, 0, 5, 0);
		gbc_autoRefreshIntervalField.gridx = 1;
		gbc_autoRefreshIntervalField.gridy = 0;
		add(autoRefreshIntervalField, gbc_autoRefreshIntervalField);
		autoRefreshIntervalField.setColumns(10);
		
		JLabel maxBufferedMessagesLabel = new JLabel("Topic Receiver Buffer Size:");
		maxBufferedMessagesLabel.setToolTipText("How many messages to buffer for topic receivers");
		GridBagConstraints gbc_maxBufferedMessagesLabel = new GridBagConstraints();
		gbc_maxBufferedMessagesLabel.anchor = GridBagConstraints.EAST;
		gbc_maxBufferedMessagesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_maxBufferedMessagesLabel.gridx = 0;
		gbc_maxBufferedMessagesLabel.gridy = 1;
		add(maxBufferedMessagesLabel, gbc_maxBufferedMessagesLabel);
		
		maxBufferedMessagesField = new JIntegerField(5);
		maxBufferedMessagesLabel.setLabelFor(maxBufferedMessagesField);
		GridBagConstraints gbc_maxBufferedMessagesField = new GridBagConstraints();
		gbc_maxBufferedMessagesField.insets = new Insets(0, 0, 5, 0);
		gbc_maxBufferedMessagesField.anchor = GridBagConstraints.WEST;
		gbc_maxBufferedMessagesField.gridx = 1;
		gbc_maxBufferedMessagesField.gridy = 1;
		add(maxBufferedMessagesField, gbc_maxBufferedMessagesField);
		maxBufferedMessagesField.setColumns(10);
		
		JLabel lblLookAndFeel = new JLabel("Look and Feel (restart required):");
		GridBagConstraints gbc_lblLookAndFeel = new GridBagConstraints();
		gbc_lblLookAndFeel.anchor = GridBagConstraints.EAST;
		gbc_lblLookAndFeel.insets = new Insets(0, 0, 0, 5);
		gbc_lblLookAndFeel.gridx = 0;
		gbc_lblLookAndFeel.gridy = 2;
		add(lblLookAndFeel, gbc_lblLookAndFeel);

		LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
		lafCombo = new JComboBox<>(lafs);
		lafCombo.setRenderer(new LAFRenderer());
		lblLookAndFeel.setLabelFor(lafCombo);
		GridBagConstraints gbc_lafCombo = new GridBagConstraints();
		gbc_lafCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_lafCombo.gridx = 1;
		gbc_lafCombo.gridy = 2;
		add(lafCombo, gbc_lafCombo);

		// Add label for license key and text field
		JLabel lblLicenseKey = new JLabel("License Key (restart required):");
		GridBagConstraints gbc_lblLicenseKey = new GridBagConstraints();
		gbc_lblLicenseKey.anchor = GridBagConstraints.EAST;
		gbc_lblLicenseKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblLicenseKey.gridx = 0;
		gbc_lblLicenseKey.gridy = 3;
		add(lblLicenseKey, gbc_lblLicenseKey);

		txtLicenseKey = new JTextField();
		lblLicenseKey.setLabelFor(txtLicenseKey);
		GridBagConstraints gbc_txtLicenseKey = new GridBagConstraints();
		gbc_txtLicenseKey.anchor = GridBagConstraints.WEST;
		gbc_txtLicenseKey.insets = new Insets(0, 0, 5, 0);
		gbc_txtLicenseKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLicenseKey.gridx = 1;
		gbc_txtLicenseKey.gridy = 3;
		add(txtLicenseKey, gbc_txtLicenseKey);
		txtLicenseKey.setColumns(10);
	}
	
	private static class LAFRenderer extends DefaultListCellRenderer {
	    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	        Object item = value;

	        if(item instanceof LookAndFeelInfo) {
	            item = ((LookAndFeelInfo)item).getName();
	        }
	        
	        return super.getListCellRendererComponent( list, item, index, isSelected, cellHasFocus);
	    }
	}
	
	public void readSettings() {
		autoRefreshIntervalField.setValue(Integer.parseInt(config.getUserPref(
				CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, 
				CoreConfiguration.DEFAULT_AUTOREFRESH_INTERVAL)));

		maxBufferedMessagesField.setValue(Integer.parseInt(config.getUserPref(
				CoreConfiguration.PREF_MAX_BUFFERED_MSG,
				CoreConfiguration.DEFAULT_MAX_BUFFERED_MSG)));

		String lafClassName = config.getUserPref(
				CoreConfiguration.PREF_LOOK_AND_FEEL,
				UIManager.getSystemLookAndFeelClassName());
		if(!selectLafByClassName(lafClassName)) {
			// If we didn't get a match, select the default native LAF
			selectLafByClassName(UIManager.getSystemLookAndFeelClassName());
		}

		String licenseKey = config.getUserPref(CoreConfiguration.PREF_LICENSE_KEY, "");
		if(Strings.isNullOrEmpty(licenseKey)) {
			if(!confirmTerms(true)){
				// user only wants to use the open source version
				licenseKey = "OSS-Only";
			}
			else{
				termsConfirmed = true;
				licenseKey = "Evaluation";
			}
			txtLicenseKey.setText(licenseKey);
			saveSettings();
		}
		else {
			txtLicenseKey.setText(licenseKey);
		}
	}
	
	private boolean selectLafByClassName(String className) {
		for(LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
			if(info.getClassName().equals(className)) {
				lafCombo.setSelectedItem(info);
				return true;
			}
		}
		return false;
	}
	
	public void saveSettings() {

		config.setUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, Integer.toString(autoRefreshIntervalField.getValue()));
		config.setUserPref(CoreConfiguration.PREF_MAX_BUFFERED_MSG, Integer.toString(maxBufferedMessagesField.getValue()));

		final String previousLicenseKey = config.getUserPref(CoreConfiguration.PREF_LICENSE_KEY, "");
		final String licenseKey = txtLicenseKey.getText();
		if(!previousLicenseKey.equals(licenseKey)) {
			// license key has changed
			if(confirmTerms(true)) {
				// Either the user
				config.setUserPref(CoreConfiguration.PREF_LICENSE_KEY, licenseKey);
			}
			else{
				// show previous value if T&C are not accepted
				txtLicenseKey.setText(previousLicenseKey);
			}
		}

		LookAndFeelInfo laf = (LookAndFeelInfo) lafCombo.getSelectedItem();
		if(laf != null) {
			config.setUserPref(CoreConfiguration.PREF_LOOK_AND_FEEL, laf.getClassName());
		}
	}

	private boolean confirmTerms(boolean showEvalNotice) {

		// don't show the dialog if the user has already agreed to the terms on startup
		if(termsConfirmed && showEvalNotice)
			return true;

		final Object[] options = {"Agree",
				"Cancel"};

        byte[] bytes = null;
		String termsAndConditions = "Unable to load terms and conditions. \nPlease contact support@conapi.at before proceeding.";
        try {
            bytes = Files.readAllBytes(Paths.get("bin/conapi-TERMS-AND-CONDITIONS.txt"));
			termsAndConditions = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(500, 300));

		final JTextArea textArea = new JTextArea(termsAndConditions);
		textArea.setColumns(50);
		textArea.setRows(10);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		//textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
		textArea.setSize(textArea.getPreferredSize().width, 100);
		final JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane, BorderLayout.CENTER);  // Add the JScrollPane to the panel unconditionally

		// show a text below text area if showEvalNotice is true
		final String notice;
		final String title;
		if(showEvalNotice) {
			title = "Terms and Conditions - EVALUATION LICENSE";
			notice = "<html>Please read the terms and conditions before continuing. <br/>" +
					"Non-agreement limits the application to the open source functionality.";
		}
		else{
			title = "Terms and Conditions";
			notice = "Please read the terms and conditions before continuing.";
		}

		JLabel lblEvalNotice = new JLabel(notice);
		lblEvalNotice.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(lblEvalNotice,BorderLayout.SOUTH);

		// show a scroll pane for text area and the above label as content of JOptionPane.showOptionDialog


			int ret = JOptionPane.showOptionDialog(
					null,
					//new JScrollPane(textArea)
					panel
					, title,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[0]
			); //default button title
        return ret == 0;
	}

	public JComponent getUIPanel() {
		return this;
	}
	
}
