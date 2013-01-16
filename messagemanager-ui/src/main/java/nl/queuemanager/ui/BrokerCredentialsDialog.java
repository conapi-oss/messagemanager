package nl.queuemanager.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import nl.queuemanager.core.jms.BrokerCredentialsProvider;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class BrokerCredentialsDialog extends JDialog implements BrokerCredentialsProvider {
	private JTextField brokerField;
	private JTextField usernameField;
	private JTextField passwordField;
	private JButton cancelButton;
	private JButton connectButton;
	
	private Credentials returnValue;

	private JLabel errorLabel;

	@Inject
	public BrokerCredentialsDialog(JFrame owner) {
		super(owner);
		
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("Enter broker credentials");
		setResizable(false);
		
		add(new BrokerCredentialsPanel());
		getRootPane().setDefaultButton(connectButton);
		
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				returnValue = new Credentials(
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
	public Credentials getCredentials(JMSBroker broker, Credentials def, Exception exception) {
		setLocationRelativeTo(getOwner());
		brokerField.setText(broker.toString());
		usernameField.setText(def != null ? def.getUsername() : "");
		passwordField.setText(def != null ? def.getPassword() : "");
		errorLabel.setText(exception.getMessage());
		
		// Show the dialog. It will block until hidden.
		setVisible(true);
		
		return returnValue;
	}
	
}
