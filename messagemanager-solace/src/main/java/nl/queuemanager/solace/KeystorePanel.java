package nl.queuemanager.solace;

import lombok.extern.java.Log;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.ui.util.JFileField;
import nl.queuemanager.ui.util.validation.AbstractValidator;
import nl.queuemanager.ui.util.validation.ValidationStatusListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.Level;

@Log
class KeystorePanel extends JPanel implements ValidationStatusListener {

	private String prefix = "Key";
	private JLabel lblKeystoreFile;
	private JLabel lblKeystorePassword;
	private JFileField fileField;
	private JPasswordField passwordField;
	
	public KeystorePanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lblKeystoreFile = new JLabel("Keystore file:");
		GridBagConstraints gbc_lblKeystoreFile = new GridBagConstraints();
		gbc_lblKeystoreFile.anchor = GridBagConstraints.EAST;
		gbc_lblKeystoreFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeystoreFile.gridx = 0;
		gbc_lblKeystoreFile.gridy = 0;
		add(lblKeystoreFile, gbc_lblKeystoreFile);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		fileField = new JFileField();
		panel.add(fileField);
		fileField.setInputVerifier(new KeystoreFileValidator(this, "Keystore format not recognized"));
		
		btnNewButton = new JButton("");
		btnNewButton.setBorderPainted(false);
		btnNewButton.setHideActionText(true);
		browseAction.setFileField(fileField);
		btnNewButton.setAction(browseAction);
		panel.add(btnNewButton);
		
		lblKeystorePassword = new JLabel("Keystore password:");
		GridBagConstraints gbc_lblKeystorePassword = new GridBagConstraints();
		gbc_lblKeystorePassword.anchor = GridBagConstraints.EAST;
		gbc_lblKeystorePassword.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeystorePassword.gridx = 0;
		gbc_lblKeystorePassword.gridy = 1;
		add(lblKeystorePassword, gbc_lblKeystorePassword);
		
		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 1;
		add(passwordField, gbc_passwordField);
		passwordField.setInputVerifier(new KeystorePasswordValidator(this, "Keystore password is incorrect"));
	}
	
	/**
	 * Validate just the keystore file without a password to make sure it is a keystore
	 */
	private class KeystoreFileValidator extends AbstractValidator {
		public KeystoreFileValidator(ValidationStatusListener parent, String message) {
			super(parent, message);
			setBlockField(false);
		}

		@Override
		protected boolean validationCriteria(JComponent c) {
			if(fileField.getFile() == null) {
				return true;
			}
			return loadKeyStore(fileField.getFile(), null) != null;
		}
	};
	
	/**
	 * Validate the keystore with a password to make sure the password is correct
	 */
	private class KeystorePasswordValidator extends AbstractValidator {
		public KeystorePasswordValidator(ValidationStatusListener parent, String message) {
			super(parent, message);
			setBlockField(false);
		}

		@Override
		protected boolean validationCriteria(JComponent c) {
			char[] password = passwordField.getPassword();
			return loadKeyStore(fileField.getFile(), password.length > 0 ? password : null) != null;
		}
	};
	
	private KeyStore loadKeyStore(File file, char[] password) {
		try {
			KeyStore jks = KeyStore.getInstance("JKS");
			jks.load(new FileInputStream(file), password);
			return jks;
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to load keystore as JKS", e);
		}
		
		try {
			KeyStore jks = KeyStore.getInstance("PKCS12");
			jks.load(new FileInputStream(file), password);
			return jks;
		} catch (Exception e) {
			log.log(Level.WARNING, "Unable to load keystore as PKCS12", e);
		}
		
		return null;
	}
	
	public void setPrefix(String prefix) {
		lblKeystoreFile.setText(prefix + "store file:");
		lblKeystorePassword.setText(prefix + "store password:");
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public File getFile() {
		return fileField.getFile();
	}
	
	public void setFile(File file) {
		fileField.setFile(file);
	}
	
	public String getPassword() {
		return new String(passwordField.getPassword());
	}
	
	public void setPassword(String password) {
		passwordField.setText(password);
	}
	
	private BrowseAction browseAction = new BrowseAction();
	private JPanel panel;
	private JButton btnNewButton;
	private static class BrowseAction extends AbstractAction {
		private JFileField fileField;
		
		public BrowseAction() {
			putValue(SMALL_ICON, new ImageIcon(KeystorePanel.class.getResource("/icons/16x16/Find.png")));
			putValue(NAME, "Browse");
			putValue(SHORT_DESCRIPTION, "Browse for file");
		}
		
		public void setFileField(JFileField fileField) {
			this.fileField = fileField;
		}
		
		public JFileField getFileField() {
			return fileField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File[] files = PlatformHelper.platformHelper.chooseFiles(getFileField(), "Select", false, new AllFilesFilter());
			if (files.length == 1) {
				File file = files[0];
				if (file != null) {
					getFileField().setFile(file);
				}
			}
		}
	};

	private static class AllFilesFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isFile();
		}

		@Override
		public String getDescription() {
			return "All files";
		}
	}

	@Override
	public void validateFailed(JComponent c, String message) {
	}

	@Override
	public void validatePassed(JComponent c) {
	}
}
