package nl.queuemanager.solace;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.java.Log;
import nl.queuemanager.core.util.BasicCredentials;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static nl.queuemanager.solace.SempConnectionMethod.SEMP_OVER_HTTP;
import static nl.queuemanager.solace.SempConnectionMethod.SEMP_OVER_MESSAGEBUS;

@SuppressWarnings("serial")
@Log
class SempConnectionDescriptorPanel extends SempConnectionDescriptorPanelUI implements DataPanel<SempConnectionDescriptor> {
	@Getter private SempConnectionDescriptor descriptor;

	@Inject
	public SempConnectionDescriptorPanel() {
		connectionMethodCombo.addItemListener(connectionMethodItemListener);
		applianceHostField.addFocusListener(detectMessagingProperties);
		appliancePortField.addFocusListener(detectMessagingProperties);
		basicAuthenticationPanel.usernameField.addFocusListener(detectMessagingProperties);
		basicAuthenticationPanel.passwordField.addFocusListener(detectMessagingProperties);
		authenticationSchemeCombo.addItemListener(authenticationSchemeItemListener);
		chkUseTLS.addItemListener(chkUseTLSItemListener);

		displayItem(null);
	}

	private final FocusAdapter detectMessagingProperties = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			updateItem(descriptor);
			if(descriptor.getConnectionMethod() == SEMP_OVER_HTTP
			&& !Strings.isNullOrEmpty(descriptor.getHttpHost())
			&& descriptor.getHttpPort() > 0
			&& basicAuthenticationPanel.usernameField.getText().length() > 0
			&& Strings.isNullOrEmpty(descriptor.getSmfHost())) {
				detectMessagingProperties(descriptor);
			}
		}
	};
	
	private void detectMessagingProperties(final SempConnectionDescriptor descriptor) {
		log.fine("Detecting messaging properties");
		new SwingWorker<String, Void>() {
			@Override
			protected String doInBackground() throws Exception {
				final URI uri = descriptor.createHttpUri();
				return HttpSEMPConnection.getMessagingIP(uri);
			}

			@Override
			protected void done() {
				try {
					descriptor.setSmfHost(get());
					displayItemIfCurrent(descriptor);
				} catch (InterruptedException | ExecutionException e) {
				}
			}
		}.execute();
		new SwingWorker<Integer, Void>() {
			@Override
			protected Integer doInBackground() throws Exception {
				final URI uri = descriptor.createHttpUri();
				return HttpSEMPConnection.getSmfPort(uri);
			}

			@Override
			protected void done() {
				try {
					descriptor.setSmfPort(get());
					displayItemIfCurrent(descriptor);
				} catch (InterruptedException | ExecutionException e) {
				}
			}
		}.execute();
	}
	
	private ItemListener authenticationSchemeItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			AuthenticationScheme scheme = (AuthenticationScheme)e.getItem();
			
			basicAuthenticationPanel.setVisible(scheme == AuthenticationScheme.BASIC);
			clientCertificateAuthenticationPanel.setVisible(scheme == AuthenticationScheme.CLIENT_CERTIFICATE);
			oauth2AuthenticationPanel.setVisible(scheme == AuthenticationScheme.OAUTH2);
			
			if(scheme == AuthenticationScheme.CLIENT_CERTIFICATE || scheme == AuthenticationScheme.OAUTH2) {
				chkUseTLS.setSelected(true);
			}
			chkUseTLS.setEnabled(scheme != AuthenticationScheme.CLIENT_CERTIFICATE && scheme != AuthenticationScheme.OAUTH2);
		}
	};
	
	private final ItemListener chkUseTLSItemListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			trustStorePanel.setVisible(chkUseTLS.isSelected());
			setDefaultPortNumbers();
		}
	};
	
	private void setDefaultPortNumbers() {
		final int[] defaultPorts = {0, 80, 443, 55443, 55555};

		// If the port numbers in the relevant fields are set to their default values
		// change them to be the defaults for the new value of the "secure" check box.
		if(Arrays.binarySearch(defaultPorts, messagingPortField.getValue()) >= 0) {
			messagingPortField.setValue(chkUseTLS.isSelected() ? 55443 : 55555);
		}

		if(Arrays.binarySearch(defaultPorts, appliancePortField.getValue()) >= 0) {
			var selectedConnectionMethod = connectionMethodCombo.getSelectedItem();
			if(selectedConnectionMethod == SEMP_OVER_HTTP) {
				appliancePortField.setValue(chkUseTLS.isSelected() ? 443 : 80);
			} else if(selectedConnectionMethod == SEMP_OVER_MESSAGEBUS) {
				appliancePortField.setValue(chkUseTLS.isSelected() ? 55443 : 55555);
			}
		}
	}
	
	private final ItemListener connectionMethodItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			switch(e.getStateChange()) {
			case ItemEvent.SELECTED: {
				final SempConnectionMethod method = (SempConnectionMethod) e.getItem();
				authenticationSchemeCombo.setVisible(method == SEMP_OVER_MESSAGEBUS);
				if(method == SEMP_OVER_HTTP) {
					authenticationSchemeCombo.setSelectedItem(AuthenticationScheme.BASIC);
				}
				sempOverMessageBusPanel.setVisible(method == SEMP_OVER_MESSAGEBUS);
				verticalStrut_4.setVisible(method == SEMP_OVER_MESSAGEBUS);
				
				messagingPropertiesPanel.setVisible(method == SEMP_OVER_HTTP);
				verticalStrut_3.setVisible(method == SEMP_OVER_HTTP);
				
				if(descriptor != null && descriptor.getConnectionMethod() != e.getItem()) {
					setDefaultPortNumbers();
				}
			}}
		}
	};

	public void displayItemIfCurrent(SempConnectionDescriptor expected) {
		assert SwingUtilities.isEventDispatchThread();
		
		if(expected == descriptor) {
			displayItem(expected);
		}
	}
	
	public void displayItem(SempConnectionDescriptor descriptor) {
		System.out.printf("displayItem(%s)\n", descriptor);
		this.descriptor = descriptor;
		
		if(descriptor == null) {
			connectionMethodCombo.setSelectedItem(SEMP_OVER_HTTP);
			
			applianceHostField.setText("");
			appliancePortField.setText("");
			
			sempOverMessageBusPanel.displayItem(null);

			authenticationSchemeCombo.setSelectedIndex(0);
			basicAuthenticationPanel.displayItem(null);
			clientCertificateAuthenticationPanel.displayItem(null);
			oauth2AuthenticationPanel.displayItem(null);
			
			chkUseTLS.setSelected(false);
			trustStorePanel.setFile(null);
			trustStorePanel.setPassword(null);
			
			messagingHostField.setText("");
			messagingPortField.setText("");
		} else {
			setDefaultPortNumbers();

			// Set this value only if different to prevent a loop because of the itemStateChanged listener on this combo 
			if(connectionMethodCombo.getSelectedItem() != descriptor.getConnectionMethod()) {
				connectionMethodCombo.setSelectedItem(descriptor.getConnectionMethod());
			}
			
			switch(descriptor.getConnectionMethod()) {
			case SEMP_OVER_HTTP: {
				applianceHostField.setText(descriptor.getHttpHost());
				appliancePortField.setValue(descriptor.getHttpPort());
				break;
			}
			
			case SEMP_OVER_MESSAGEBUS: {
				applianceHostField.setText(descriptor.getSmfHost());
				appliancePortField.setValue(descriptor.getSmfPort());
				break;
			}}
			
			sempOverMessageBusPanel.displayItem(descriptor);

			if(authenticationSchemeCombo.getSelectedItem() != descriptor.getAuthenticationScheme()) {
				authenticationSchemeCombo.setSelectedItem(descriptor.getAuthenticationScheme());
			}
			switch(descriptor.getAuthenticationScheme()) {
			case BASIC:
				basicAuthenticationPanel.displayItem((BasicCredentials)descriptor.getCredentials());
				break;
			case CLIENT_CERTIFICATE:
				clientCertificateAuthenticationPanel.displayItem((SolaceClientCertificateCredentials)descriptor.getCredentials());
				break;
			case OAUTH2:
				oauth2AuthenticationPanel.displayItem((SolaceOAuth2Credentials) descriptor.getCredentials());
				break;
			}
			
			chkUseTLS.setSelected(descriptor.isSecure());
			trustStorePanel.setFile(descriptor.getTrustStoreFile());
			trustStorePanel.setPassword(descriptor.getTrustStorePassword());
			
			messagingHostField.setText(descriptor.getSmfHost());
			messagingPortField.setValue(descriptor.getSmfPort());
		}
		
		connectionMethodItemListener.itemStateChanged(new ItemEvent(
				connectionMethodCombo, 
				ItemEvent.ITEM_STATE_CHANGED,
				connectionMethodCombo.getSelectedItem(), 
				ItemEvent.SELECTED));
		
		enableFields(descriptor != null);
	}
	
	public void enableFields(boolean enabled) {
		connectionMethodCombo.setEnabled(enabled);
		
		applianceHostField.setEnabled(enabled);
		appliancePortField.setEnabled(enabled);
		
		sempOverMessageBusPanel.setEnabled(true);
		
		authenticationSchemeCombo.setEnabled(enabled);
		basicAuthenticationPanel.setEnabled(enabled);
		clientCertificateAuthenticationPanel.setEnabled(enabled);
		oauth2AuthenticationPanel.setEnabled(enabled);
		
		chkUseTLS.setEnabled(enabled);
		trustStorePanel.setEnabled(enabled);
		
		messagingHostField.setEnabled(enabled);
		messagingPortField.setEnabled(enabled);
		
		connectButton.setEnabled(enabled);
	}
	
	public void updateItem(SempConnectionDescriptor descriptor) {
		System.out.printf("updateItem(%s)\n", descriptor);
		descriptor.setConnectionMethod((SempConnectionMethod)connectionMethodCombo.getSelectedItem());
		
		// Appliance
		switch(descriptor.getConnectionMethod()) {
		case SEMP_OVER_HTTP: {
			descriptor.setHttpHost(applianceHostField.getText());
			descriptor.setHttpPort(appliancePortField.getValue());
			
			// Messaging
			descriptor.setSmfHost(messagingHostField.getText());
			descriptor.setSmfPort(messagingPortField.getValue());
			break;
		}
		
		case SEMP_OVER_MESSAGEBUS: {
			descriptor.setSmfHost(applianceHostField.getText());
			descriptor.setSmfPort(appliancePortField.getValue());
			break;
			
		}}

		// SEMP over Message Bus
		sempOverMessageBusPanel.updateItem(descriptor);
		
		// Authentication
		descriptor.setAuthenticationScheme((AuthenticationScheme)authenticationSchemeCombo.getSelectedItem());
		switch(descriptor.getAuthenticationScheme()) {
		case BASIC: {
			BasicCredentials cred = new BasicCredentials();
			basicAuthenticationPanel.updateItem(cred);
			descriptor.setCredentials(cred);
			break;
		}

		case CLIENT_CERTIFICATE: {
			SolaceClientCertificateCredentials cred = new SolaceClientCertificateCredentials();
			clientCertificateAuthenticationPanel.updateItem(cred);
			descriptor.setCredentials(cred);
			break;
		}

		case OAUTH2: {
			SolaceOAuth2Credentials cred = new SolaceOAuth2Credentials();
			oauth2AuthenticationPanel.updateItem(cred);
			descriptor.setCredentials(cred);
			break;
		}

		}

		// TLS
		descriptor.setSecure(chkUseTLS.isSelected());
		descriptor.setTrustStoreFile(trustStorePanel.getFile());
		descriptor.setTrustStorePassword(trustStorePanel.getPassword());
	}
	
}
