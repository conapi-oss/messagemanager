package nl.queuemanager.solace;

import nl.queuemanager.core.util.BasicCredentials;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.jms.JMSBroker;

import jakarta.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

@SuppressWarnings("serial")
class SolaceBrokerCredentialsDialog extends SolaceBrokerCredentialsDialogUI {

	private Credentials returnValue;
	
	@Inject
	public SolaceBrokerCredentialsDialog(JFrame owner) {
		super(owner);
		
		setModal(true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("Enter broker credentials");
		setResizable(false);
		
		authenticationSchemeCombo.addItemListener(authenticationSchemeItemListener);
		connectButton.addActionListener(connectActionListener);
		cancelButton.addActionListener(cancelActionListener);
	}
	
	private ItemListener authenticationSchemeItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			AuthenticationScheme scheme = (AuthenticationScheme)e.getItem();
			
			basicAuthenticationPanel.setVisible(scheme == AuthenticationScheme.BASIC);
			clientCertificateAuthenticationPanel.setVisible(scheme == AuthenticationScheme.CLIENT_CERTIFICATE);
			truststorePanel.setVisible(scheme == AuthenticationScheme.CLIENT_CERTIFICATE);
			
			pack();
		}
	};
	
	private ActionListener connectActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch((AuthenticationScheme)authenticationSchemeCombo.getSelectedItem()) {
			case BASIC: {
				BasicCredentials ret = new BasicCredentials();
				basicAuthenticationPanel.updateItem(ret);
				returnValue = ret;
				break;
			}

			case CLIENT_CERTIFICATE: {
				SolaceClientCertificateCredentials ret = new SolaceClientCertificateCredentials();
				clientCertificateAuthenticationPanel.updateItem(ret);
				returnValue = ret;
				break;
			}
			
			default: {
				throw new IllegalStateException(String.format("Authentication scheme %s not supported", authenticationSchemeCombo.getSelectedItem()));
			}}
			
			setVisible(false);
		}
	};
	
	private ActionListener cancelActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			returnValue = null;
			setVisible(false);
		}
	};
	
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
		lblMessageVpn.setText(broker.toString());
		
		if(def instanceof BasicCredentials) {
			authenticationSchemeCombo.setSelectedItem(AuthenticationScheme.BASIC);
			basicAuthenticationPanel.displayItem((BasicCredentials)def);
		} else if(def instanceof SolaceClientCertificateCredentials) {
			authenticationSchemeCombo.setSelectedItem(AuthenticationScheme.CLIENT_CERTIFICATE);
			clientCertificateAuthenticationPanel.displayItem((SolaceClientCertificateCredentials)def);
		} else {
			// Default to basic credentials
			authenticationSchemeCombo.setSelectedItem(AuthenticationScheme.BASIC);
		}
		lblError.setText(exception.getMessage());
		
		// Trigger the listener for the authentication combo to guarantee that the correct things are visible
		authenticationSchemeItemListener.itemStateChanged(new ItemEvent(
				authenticationSchemeCombo, 
				ItemEvent.ITEM_STATE_CHANGED,
				authenticationSchemeCombo.getSelectedItem(), 
				ItemEvent.SELECTED));
		
		// Show the dialog. It will block until hidden.
		getRootPane().setDefaultButton(connectButton);
		pack();
		setVisible(true);

		return returnValue;
	}

}
