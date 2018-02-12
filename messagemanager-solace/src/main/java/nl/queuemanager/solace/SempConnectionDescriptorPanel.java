package nl.queuemanager.solace;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.extern.java.Log;
import nl.queuemanager.core.util.BasicCredentials;

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
			if(descriptor.getConnectionMethod() == SempConnectionMethod.SEMP_OVER_HTTP 
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
			
			if(scheme == AuthenticationScheme.CLIENT_CERTIFICATE) {
				chkUseTLS.setSelected(true);
			}
			chkUseTLS.setEnabled(scheme != AuthenticationScheme.CLIENT_CERTIFICATE);
		}
	};
	
	private final ItemListener chkUseTLSItemListener = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
			trustStorePanel.setVisible(chkUseTLS.isSelected());
			updateItem(descriptor);
			setDefaultPortNumbers(descriptor);
			displayItem(descriptor);
		}
	};
	
	private void setDefaultPortNumbers(SempConnectionDescriptor descriptor) {
		final int[] defaultPorts = {0, 80, 443, 55443, 55555};

		if(descriptor != null) {
			// If the port numbers in the relevant fields are set to their default values
			// change them to be the defaults for the new value of the "secure" check box.
			if(Arrays.binarySearch(defaultPorts, descriptor.getSmfPort()) >= 0) {
				descriptor.setSmfPort(chkUseTLS.isSelected() ? 55443 : 55555);
			}
			if(Arrays.binarySearch(defaultPorts, descriptor.getHttpPort()) >= 0) {
				descriptor.setHttpPort(chkUseTLS.isSelected() ? 443 : 80);
			}
		}
	}
	
	private final ItemListener connectionMethodItemListener = new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
			switch(e.getStateChange()) {
			case ItemEvent.SELECTED: {
				final SempConnectionMethod method = (SempConnectionMethod) e.getItem();
				authenticationSchemeCombo.setVisible(method == SempConnectionMethod.SEMP_OVER_MESSAGEBUS);
				if(method == SempConnectionMethod.SEMP_OVER_HTTP) {
					authenticationSchemeCombo.setSelectedItem(AuthenticationScheme.BASIC);
				}
				sempOverMessageBusPanel.setVisible(method == SempConnectionMethod.SEMP_OVER_MESSAGEBUS);
				verticalStrut_4.setVisible(method == SempConnectionMethod.SEMP_OVER_MESSAGEBUS);
				
				messagingPropertiesPanel.setVisible(method == SempConnectionMethod.SEMP_OVER_HTTP);
				verticalStrut_3.setVisible(method == SempConnectionMethod.SEMP_OVER_HTTP);
				
				if(descriptor != null && descriptor.getConnectionMethod() != e.getItem()) {
					updateItem(descriptor);
					setDefaultPortNumbers(descriptor);
					displayItem(descriptor);
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
		this.descriptor = descriptor;
		
		if(descriptor == null) {
			connectionMethodCombo.setSelectedItem(SempConnectionMethod.SEMP_OVER_HTTP);
			
			applianceHostField.setText("");
			appliancePortField.setText("");
			
			sempOverMessageBusPanel.displayItem(null);

			authenticationSchemeCombo.setSelectedIndex(0);
			basicAuthenticationPanel.displayItem(null);
			clientCertificateAuthenticationPanel.displayItem(null);
			
			chkUseTLS.setSelected(false);
			trustStorePanel.setFile(null);
			trustStorePanel.setPassword(null);
			
			messagingHostField.setText("");
			messagingPortField.setText("");
		} else {
			setDefaultPortNumbers(descriptor);
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
			
			authenticationSchemeCombo.setSelectedItem(descriptor.getAuthenticationScheme());
			switch(descriptor.getAuthenticationScheme()) {
			case BASIC:
				basicAuthenticationPanel.displayItem((BasicCredentials)descriptor.getCredentials());
				break;
			case CLIENT_CERTIFICATE:
				clientCertificateAuthenticationPanel.displayItem((SolaceClientCertificateCredentials)descriptor.getCredentials());
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
		
		chkUseTLS.setEnabled(enabled);
		trustStorePanel.setEnabled(enabled);
		
		messagingHostField.setEnabled(enabled);
		messagingPortField.setEnabled(enabled);
		
		connectButton.setEnabled(enabled);
	}
	
	public void updateItem(SempConnectionDescriptor descriptor) {
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
		}}

		// TLS
		descriptor.setSecure(chkUseTLS.isSelected());
		descriptor.setTrustStoreFile(trustStorePanel.getFile());
		descriptor.setTrustStorePassword(trustStorePanel.getPassword());
	}
	
}
