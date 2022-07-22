package nl.queuemanager.solace;

import javax.swing.*;
import java.awt.*;

class OAuth2AuthenticationPanel extends JPanel implements DataPanel<SolaceOAuth2Credentials> {
	JPasswordField accessTokenField;
	JPasswordField idTokenField;

	public OAuth2AuthenticationPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblAccessToken = new JLabel("Access Token:");
		GridBagConstraints gbc_lblAccessToken = new GridBagConstraints();
		gbc_lblAccessToken.anchor = GridBagConstraints.WEST;
		gbc_lblAccessToken.insets = new Insets(0, 5, 5, 5);
		gbc_lblAccessToken.gridx = 0;
		gbc_lblAccessToken.gridy = 0;
		add(lblAccessToken, gbc_lblAccessToken);
		
		accessTokenField = new JPasswordField();
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.insets = new Insets(5, 0, 5, 5);
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.gridx = 1;
		gbc_usernameField.gridy = 0;
		add(accessTokenField, gbc_usernameField);
		accessTokenField.setColumns(10);
		
		JLabel lblIdToken = new JLabel("ID Token:");
		GridBagConstraints gbc_lblIdToken = new GridBagConstraints();
		gbc_lblIdToken.insets = new Insets(5, 5, 5, 5);
		gbc_lblIdToken.anchor = GridBagConstraints.WEST;
		gbc_lblIdToken.gridx = 0;
		gbc_lblIdToken.gridy = 1;
		add(lblIdToken, gbc_lblIdToken);
		
		idTokenField = new JPasswordField();
		GridBagConstraints gbc_idTokenField = new GridBagConstraints();
		gbc_idTokenField.insets = new Insets(0, 0, 5, 5);
		gbc_idTokenField.fill = GridBagConstraints.HORIZONTAL;
		gbc_idTokenField.gridx = 1;
		gbc_idTokenField.gridy = 1;
		add(idTokenField, gbc_idTokenField);
	}

	@Override
	public void displayItem(SolaceOAuth2Credentials item) {
		if(item == null) {
			accessTokenField.setText("");
			idTokenField.setText("");
		} else {
			accessTokenField.setText(item.getAccessToken());
			idTokenField.setText(item.getIdToken());
		}
	}
	
	@Override
	public void updateItem(SolaceOAuth2Credentials item) {
		item.setAccessToken(new String(accessTokenField.getPassword()));
		item.setIdToken(new String(idTokenField.getPassword()));
	}
	
}
