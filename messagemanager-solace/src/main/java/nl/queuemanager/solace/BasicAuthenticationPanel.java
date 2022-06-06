package nl.queuemanager.solace;

import nl.queuemanager.core.util.BasicCredentials;

import javax.swing.*;
import java.awt.*;

class BasicAuthenticationPanel extends JPanel implements DataPanel<BasicCredentials> {
	JPasswordField passwordField;
	JTextField usernameField;
	
	public BasicAuthenticationPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblUsername = new JLabel("Username:");
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.anchor = GridBagConstraints.WEST;
		gbc_lblUsername.insets = new Insets(0, 5, 5, 5);
		gbc_lblUsername.gridx = 0;
		gbc_lblUsername.gridy = 0;
		add(lblUsername, gbc_lblUsername);
		
		usernameField = new JTextField();
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.insets = new Insets(5, 0, 5, 5);
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.gridx = 1;
		gbc_usernameField.gridy = 0;
		add(usernameField, gbc_usernameField);
		usernameField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(5, 5, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.WEST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		add(lblPassword, gbc_lblPassword);
		
		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 1;
		add(passwordField, gbc_passwordField);
	}

	@Override
	public void displayItem(BasicCredentials item) {
		if(item == null) {
			usernameField.setText("");
			passwordField.setText("");
		} else {
			usernameField.setText(item.getUsername());
			passwordField.setText(item.getPassword());
		}
	}
	
	@Override
	public void updateItem(BasicCredentials item) {
		item.setUsername(usernameField.getText());
		item.setPassword(new String(passwordField.getPassword()));
	}
	
}
