package nl.queuemanager.ui;

import com.google.inject.Inject;
import nl.queuemanager.core.util.BasicCredentials;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class BrokerCredentialsDialog extends JDialog {
	private JTextField brokerField;
	private JTextField usernameField;
	private JTextField passwordField;
	private JButton cancelButton;
	private JButton connectButton;
	
	private BasicCredentials returnValue;

	private JLabel errorLabel;

	@Inject
	public BrokerCredentialsDialog(JFrame owner) {
		super(owner);
		
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("Enter Broker Credentials");
		setResizable(false);
		
		add(new BrokerCredentialsPanel());
		getRootPane().setDefaultButton(connectButton);
		
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				returnValue = new BasicCredentials(
					usernameField.getText(),
					passwordField.getText());
				setVisible(false);
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				returnValue = null;
				setVisible(false);
			}
		});
		
		pack();
	}
	
	class BrokerCredentialsPanel extends JPanel {
		public BrokerCredentialsPanel() {
			setBorder(new EmptyBorder(10, 10, 10, 10));
			setLayout(new GridBagLayout());
	
			GridBagConstraints c;
			c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Broker:"), c);
			
			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(brokerField = new JTextField(), c);
			brokerField.setEditable(false);
			
			c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Username:"), c);
			
			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(usernameField = new JTextField(30), c);
			
			c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 2;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Password:"), c);
					
			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(passwordField = new JPasswordField(30), c);
	
			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			add(errorLabel = new JLabel(), c);
			errorLabel.setForeground(Color.RED);
			
			Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(connectButton = new JButton("Connect"));
			buttonBox.add(cancelButton = new JButton("Cancel"));
			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 4;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_END;
			add(buttonBox, c);		
		}
	}
	
	private GridBagConstraints gridBagConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 5, 3, 5);
		return c;
	}

	/**
	 * Ask the user for credentials for the specified broker. Returns a new Credentials
	 * object when the user has provided them. When the user cancels, will return null.
	 * 
	 * @param broker
	 * @param def
	 * @return
	 */
	public BasicCredentials getCredentials(JMSBroker broker, Credentials def, Exception exception) {
		if(def != null && !(def instanceof BasicCredentials)) {
			throw new IllegalArgumentException("def");
		}
		
		setLocationRelativeTo(getOwner());
		brokerField.setText(broker.toString());
		usernameField.setText(def != null ? ((BasicCredentials)def).getUsername() : "");
		passwordField.setText(def != null ? ((BasicCredentials)def).getPassword() : "");
		errorLabel.setText(exception.getMessage());
		
		// Show the dialog. It will block until hidden.
		setVisible(true);
		
		return returnValue;
	}
	
}
