package nl.queuemanager.solace;

import lombok.extern.java.Log;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.ui.util.JFileField;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.logging.Level;

@SuppressWarnings("serial")
@Log
class ClientCertificateAuthenticationPanel extends JPanel implements DataPanel<SolaceClientCertificateCredentials> {
	private JFileField keyStoreFileField;
	private JPasswordField keyStorePasswordField;
	private JComboBox<String> privateKeyAliasCombo;
	private JPasswordField privateKeyPasswordField;
	
	public ClientCertificateAuthenticationPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Keystore file:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(5, 0, 5, 0);
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		keyStoreFileField = new JFileField();
		panel.add(keyStoreFileField);
		keyStoreFileField.setInputVerifier(checkKeystore);
		
		browseButton = new JButton("");
		browseButton.setHideActionText(true);
		browseButton.setBorderPainted(false);
		browseAction.setFileField(keyStoreFileField);
		browseButton.setAction(browseAction);
		panel.add(browseButton);
		
		JLabel lblKeystorePassword = new JLabel("Keystore password:");
		GridBagConstraints gbc_lblKeystorePassword = new GridBagConstraints();
		gbc_lblKeystorePassword.anchor = GridBagConstraints.WEST;
		gbc_lblKeystorePassword.insets = new Insets(0, 5, 5, 5);
		gbc_lblKeystorePassword.gridx = 0;
		gbc_lblKeystorePassword.gridy = 1;
		add(lblKeystorePassword, gbc_lblKeystorePassword);
		
		keyStorePasswordField = new JPasswordField();
		GridBagConstraints gbc_keyStorePasswordField = new GridBagConstraints();
		gbc_keyStorePasswordField.insets = new Insets(0, 0, 5, 0);
		gbc_keyStorePasswordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_keyStorePasswordField.gridx = 1;
		gbc_keyStorePasswordField.gridy = 1;
		add(keyStorePasswordField, gbc_keyStorePasswordField);
		keyStorePasswordField.setInputVerifier(checkKeystore);
		
		JLabel lblPrivateKeyAlias = new JLabel("Private key alias");
		GridBagConstraints gbc_lblPrivateKeyAlias = new GridBagConstraints();
		gbc_lblPrivateKeyAlias.anchor = GridBagConstraints.WEST;
		gbc_lblPrivateKeyAlias.insets = new Insets(0, 5, 5, 5);
		gbc_lblPrivateKeyAlias.gridx = 0;
		gbc_lblPrivateKeyAlias.gridy = 2;
		add(lblPrivateKeyAlias, gbc_lblPrivateKeyAlias);
		
		privateKeyAliasCombo = new JComboBox<String>();
		GridBagConstraints gbc_privateKeyAliasCombo = new GridBagConstraints();
		gbc_privateKeyAliasCombo.insets = new Insets(0, 0, 5, 0);
		gbc_privateKeyAliasCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_privateKeyAliasCombo.gridx = 1;
		gbc_privateKeyAliasCombo.gridy = 2;
		add(privateKeyAliasCombo, gbc_privateKeyAliasCombo);
		
		JLabel lblPrivateKeyPassword = new JLabel("Private key password");
		GridBagConstraints gbc_lblPrivateKeyPassword = new GridBagConstraints();
		gbc_lblPrivateKeyPassword.insets = new Insets(0, 5, 5, 5);
		gbc_lblPrivateKeyPassword.anchor = GridBagConstraints.WEST;
		gbc_lblPrivateKeyPassword.gridx = 0;
		gbc_lblPrivateKeyPassword.gridy = 3;
		add(lblPrivateKeyPassword, gbc_lblPrivateKeyPassword);
		
		privateKeyPasswordField = new JPasswordField();
		GridBagConstraints gbc_privateKeyPasswordField = new GridBagConstraints();
		gbc_privateKeyPasswordField.insets = new Insets(0, 0, 5, 0);
		gbc_privateKeyPasswordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_privateKeyPasswordField.gridx = 1;
		gbc_privateKeyPasswordField.gridy = 3;
		add(privateKeyPasswordField, gbc_privateKeyPasswordField);
	}

	private InputVerifier checkKeystore = new InputVerifier() {
		@Override
		public boolean verify(JComponent input) {
			char[] password = keyStorePasswordField.getPassword();
			return checkKeystore(keyStoreFileField.getFile(), password.length == 0 ? null : password);
		}
	};
	private JPanel panel;
	private JButton browseButton;
	
	private boolean checkKeystore(File file, char[] password) {
		Object selected = privateKeyAliasCombo.getSelectedItem();
		privateKeyAliasCombo.removeAllItems();
		
		KeyStore ks = loadKeyStore(file, password);
		if(ks != null) try {
			for(Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements();) {
				String alias = aliases.nextElement();
				if(ks.isKeyEntry(alias)) {
					privateKeyAliasCombo.addItem(alias);
				}
			}
			
			if(privateKeyAliasCombo.getItemCount() == 1) {
				privateKeyAliasCombo.setSelectedIndex(0);
			} else {
				privateKeyAliasCombo.setSelectedItem(selected);
			}
		} catch (KeyStoreException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return ks != null;
	}
	
	private KeyStore loadKeyStore(File file, char[] password) {
		if(file == null || !file.canRead()) {
			return null;
		}
		
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
	
	@Override
	public void displayItem(SolaceClientCertificateCredentials item) {
		if(item == null) {
			keyStoreFileField.setFile(null);
			keyStorePasswordField.setText("");
			privateKeyAliasCombo.setSelectedItem(null);
			privateKeyPasswordField.setText("");
		} else {
			keyStoreFileField.setFile(item.getKeyStoreFile());
			keyStorePasswordField.setText(item.getKeyStorePassword());
			privateKeyAliasCombo.setSelectedItem(item.getPrivateKeyAlias());
			privateKeyPasswordField.setText(item.getPrivateKeyPassword());
		}
	}

	@Override
	public void updateItem(SolaceClientCertificateCredentials item) {
		item.setKeyStoreFile(keyStoreFileField.getFile());
		item.setKeyStorePassword(new String(keyStorePasswordField.getPassword()));
		item.setPrivateKeyAlias((String)privateKeyAliasCombo.getSelectedItem());
		item.setPrivateKeyPassword(new String(privateKeyPasswordField.getPassword()));
	}
	
	private BrowseAction browseAction = new BrowseAction();
	@SuppressWarnings("serial")
	private static class BrowseAction extends AbstractAction {
		private JFileField fileField;
		
		public BrowseAction() {
			putValue(SMALL_ICON, new ImageIcon(ClientCertificateAuthenticationPanel.class.getResource("/icons/16x16/Find.png")));
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
			var maybeFiles = Optional.ofNullable(
					PlatformHelper.platformHelper.chooseFiles(
							getFileField(), "Select", false, new AllFilesFilter()));
			maybeFiles
				.flatMap(files -> Arrays.stream(files).findFirst())
				.ifPresent(getFileField()::setFile);
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

}
