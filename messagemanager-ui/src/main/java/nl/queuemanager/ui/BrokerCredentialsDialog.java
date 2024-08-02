package nl.queuemanager.ui;

import com.google.inject.Inject;
import nl.queuemanager.core.configuration.CoreConfiguration;
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
	private JTextField alternateUrlField;
	private JLabel alternateUrlLabel;

	private JTextField usernameField;
	private JTextField passwordField;
	private JButton cancelButton;
	private JButton connectButton;
	
	private BasicCredentials returnValue;

	private JLabel errorLabel;
	private CoreConfiguration config;
	private String initialAlternateUrl;

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



	public void enableAlternateUrlOverride(CoreConfiguration config, String alternateUrl) {
		// if this is invoked, we will show the alternate URL field and allow the user to override it
		this.config = config;
		this.initialAlternateUrl = alternateUrl;
		alternateUrlField.setText(alternateUrl);

		// Add the Alternate URL components to the panel
		((BrokerCredentialsPanel) getContentPane().getComponent(0)).addAlternateUrlComponents();

		pack(); // Resize the dialog to fit the new components
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

			alternateUrlLabel = new JLabel("Alternate URL:");
			alternateUrlField = new JTextField();

			c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 2;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Username:"), c);

			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 2;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(usernameField = new JTextField(30), c);

			c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 3;
			c.anchor = GridBagConstraints.LINE_END;
			add(new JLabel("Password:"), c);

			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 3;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(passwordField = new JPasswordField(30), c);

			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 4;
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
			c.gridy = 5;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.LINE_END;
			add(buttonBox, c);
		}

		private void addAlternateUrlComponents() {
			GridBagConstraints c = gridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.anchor = GridBagConstraints.LINE_END;
			add(alternateUrlLabel, c);

			c = gridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			add(alternateUrlField, c);

			// Shift other components down
			Component[] components = getComponents();
			for (Component component : components) {
				if (component != alternateUrlLabel && component != alternateUrlField) {
					GridBagConstraints constraints = ((GridBagLayout) getLayout()).getConstraints(component);
					if (constraints.gridy > 0) {
						constraints.gridy++;
						((GridBagLayout) getLayout()).setConstraints(component, constraints);
					}
				}
			}

			revalidate();
			repaint();
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

		// alternative would be to do this in the listener
		if(config!=null && !initialAlternateUrl.equals(alternateUrlField.getText())){
			// only save the alternate URL if it has changed
			config.setBrokerPref(broker, CoreConfiguration.PREF_BROKER_ALTERNATE_URL, alternateUrlField.getText());
		}
		
		return returnValue;
	}
	
}
