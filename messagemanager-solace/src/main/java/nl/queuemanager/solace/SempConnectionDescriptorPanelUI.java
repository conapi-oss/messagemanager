package nl.queuemanager.solace;

import nl.queuemanager.ui.util.JIntegerField;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

@SuppressWarnings("serial")
class SempConnectionDescriptorPanelUI extends JPanel {
	protected JTextField applianceHostField;
	protected JIntegerField appliancePortField;
	protected JCheckBox chkUseTLS;
	protected JButton connectButton;
	protected JTextField messagingHostField;
	protected JIntegerField messagingPortField;
		
	protected JComboBox<SempConnectionMethod> connectionMethodCombo;
	protected SempOverMessageBusPanel sempOverMessageBusPanel;
	protected JComboBox<AuthenticationScheme> authenticationSchemeCombo;
	protected BasicAuthenticationPanel basicAuthenticationPanel;
	protected ClientCertificateAuthenticationPanel clientCertificateAuthenticationPanel;
	protected OAuth2AuthenticationPanel oauth2AuthenticationPanel;
	protected JPanel messagingPropertiesPanel;
	protected KeystorePanel trustStorePanel;
	protected Component verticalStrut_3;
	protected Component verticalStrut_4;
	
	public SempConnectionDescriptorPanelUI() {
		setBorder(new EmptyBorder(0, 0, 0, 10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		Component verticalStrut = Box.createVerticalStrut(15);
		add(verticalStrut);
		
		connectionMethodCombo = new JComboBox<SempConnectionMethod>();
		connectionMethodCombo.setModel(new DefaultComboBoxModel<SempConnectionMethod>(SempConnectionMethod.values()));
		add(connectionMethodCombo);
		
		JPanel appliancePropertiesPanel = new JPanel();
		add(appliancePropertiesPanel);
		appliancePropertiesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Appliance Properties", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagLayout gbl_appliancePropertiesPanel = new GridBagLayout();
		gbl_appliancePropertiesPanel.columnWidths = new int[]{10, 63, 0};
		gbl_appliancePropertiesPanel.rowHeights = new int[]{26, 0, 0};
		gbl_appliancePropertiesPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_appliancePropertiesPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		appliancePropertiesPanel.setLayout(gbl_appliancePropertiesPanel);
		
		applianceHostField = new JTextField();
		GridBagConstraints gbc_applianceHostField = new GridBagConstraints();
		gbc_applianceHostField.fill = GridBagConstraints.HORIZONTAL;
		gbc_applianceHostField.anchor = GridBagConstraints.NORTH;
		gbc_applianceHostField.insets = new Insets(5, 0, 5, 5);
		gbc_applianceHostField.gridx = 1;
		gbc_applianceHostField.gridy = 0;
		appliancePropertiesPanel.add(applianceHostField, gbc_applianceHostField);
		applianceHostField.setColumns(10);
		
		JLabel lblApplianceHostName = new JLabel("Appliance hostname:");
		lblApplianceHostName.setLabelFor(applianceHostField);
		GridBagConstraints gbc_lblApplianceHostName = new GridBagConstraints();
		gbc_lblApplianceHostName.insets = new Insets(0, 5, 5, 5);
		gbc_lblApplianceHostName.anchor = GridBagConstraints.WEST;
		gbc_lblApplianceHostName.gridx = 0;
		gbc_lblApplianceHostName.gridy = 0;
		appliancePropertiesPanel.add(lblApplianceHostName, gbc_lblApplianceHostName);
		
		JLabel lblAppliancePort = new JLabel("Appliance port:");
		GridBagConstraints gbc_lblAppliancePort = new GridBagConstraints();
		gbc_lblAppliancePort.anchor = GridBagConstraints.WEST;
		gbc_lblAppliancePort.insets = new Insets(0, 5, 5, 5);
		gbc_lblAppliancePort.gridx = 0;
		gbc_lblAppliancePort.gridy = 1;
		appliancePropertiesPanel.add(lblAppliancePort, gbc_lblAppliancePort);
		
		appliancePortField = new JIntegerField(5);
		lblAppliancePort.setLabelFor(appliancePortField);
		GridBagConstraints gbc_appliancePortField = new GridBagConstraints();
		gbc_appliancePortField.insets = new Insets(0, 0, 5, 5);
		gbc_appliancePortField.fill = GridBagConstraints.HORIZONTAL;
		gbc_appliancePortField.anchor = GridBagConstraints.NORTH;
		gbc_appliancePortField.gridx = 1;
		gbc_appliancePortField.gridy = 1;
		appliancePropertiesPanel.add(appliancePortField, gbc_appliancePortField);
		appliancePortField.setColumns(10);
		appliancePropertiesPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{applianceHostField, appliancePortField, lblApplianceHostName, lblAppliancePort}));
		
		Component verticalStrut_6 = Box.createVerticalStrut(20);
		add(verticalStrut_6);
		
		sempOverMessageBusPanel = new SempOverMessageBusPanel();
		sempOverMessageBusPanel.setVisible(false);
		add(sempOverMessageBusPanel);
		
		verticalStrut_4 = Box.createVerticalStrut(20);
		add(verticalStrut_4);
		
		JPanel authenticationPanel = new JPanel();
		authenticationPanel.setBorder(new TitledBorder(null, "Authentication", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		add(authenticationPanel);
		authenticationPanel.setLayout(new BoxLayout(authenticationPanel, BoxLayout.Y_AXIS));
		
		authenticationSchemeCombo = new JComboBox<AuthenticationScheme>();
		authenticationSchemeCombo.setVisible(false);
		authenticationSchemeCombo.setModel(new DefaultComboBoxModel<AuthenticationScheme>(AuthenticationScheme.values()));
		authenticationPanel.add(authenticationSchemeCombo);
		
		basicAuthenticationPanel = new BasicAuthenticationPanel();
		authenticationPanel.add(basicAuthenticationPanel);

		clientCertificateAuthenticationPanel = new ClientCertificateAuthenticationPanel();
		clientCertificateAuthenticationPanel.setVisible(false);
		authenticationPanel.add(clientCertificateAuthenticationPanel);

		oauth2AuthenticationPanel = new OAuth2AuthenticationPanel();
		oauth2AuthenticationPanel.setVisible(false);
		authenticationPanel.add(oauth2AuthenticationPanel);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		add(verticalStrut_1);
		
		JPanel tlsPropertiesPanel = new JPanel();
		add(tlsPropertiesPanel);
		tlsPropertiesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "TLS Properties", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		tlsPropertiesPanel.setLayout(new BoxLayout(tlsPropertiesPanel, BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		tlsPropertiesPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		chkUseTLS = new JCheckBox("Use Secure Session");
		panel.add(chkUseTLS);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		panel.add(horizontalGlue);
		
		trustStorePanel = new KeystorePanel();
		trustStorePanel.setVisible(false);
		
		Component verticalStrut_5 = Box.createVerticalStrut(5);
		tlsPropertiesPanel.add(verticalStrut_5);
		trustStorePanel.setPrefix("Trust");
		trustStorePanel.setBorder(null);
		tlsPropertiesPanel.add(trustStorePanel);
				
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		add(verticalStrut_2);
		
		messagingPropertiesPanel = new JPanel();
		messagingPropertiesPanel.setBorder(new TitledBorder(null, "Messaging properties", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		add(messagingPropertiesPanel);
		GridBagLayout gbl_messagingPropertiesPanel = new GridBagLayout();
		gbl_messagingPropertiesPanel.columnWidths = new int[]{61, 0, 0};
		gbl_messagingPropertiesPanel.rowHeights = new int[]{16, 0, 0};
		gbl_messagingPropertiesPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_messagingPropertiesPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		messagingPropertiesPanel.setLayout(gbl_messagingPropertiesPanel);
		
		JLabel lblMessagingHost = new JLabel("Messaging host:");
		GridBagConstraints gbc_lblMessagingHost = new GridBagConstraints();
		gbc_lblMessagingHost.insets = new Insets(0, 5, 5, 5);
		gbc_lblMessagingHost.anchor = GridBagConstraints.WEST;
		gbc_lblMessagingHost.gridx = 0;
		gbc_lblMessagingHost.gridy = 0;
		messagingPropertiesPanel.add(lblMessagingHost, gbc_lblMessagingHost);
		
		messagingHostField = new JTextField();
		lblMessagingHost.setLabelFor(messagingHostField);
		GridBagConstraints gbc_messagingHostField = new GridBagConstraints();
		gbc_messagingHostField.insets = new Insets(5, 0, 5, 5);
		gbc_messagingHostField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messagingHostField.gridx = 1;
		gbc_messagingHostField.gridy = 0;
		messagingPropertiesPanel.add(messagingHostField, gbc_messagingHostField);
		messagingHostField.setColumns(10);
		
		JLabel lblMessagingPort = new JLabel("Messaging port:");
		GridBagConstraints gbc_lblMessagingPort = new GridBagConstraints();
		gbc_lblMessagingPort.anchor = GridBagConstraints.WEST;
		gbc_lblMessagingPort.insets = new Insets(0, 5, 5, 5);
		gbc_lblMessagingPort.gridx = 0;
		gbc_lblMessagingPort.gridy = 1;
		messagingPropertiesPanel.add(lblMessagingPort, gbc_lblMessagingPort);
		
		messagingPortField = new JIntegerField(5);
		lblMessagingPort.setLabelFor(messagingPortField);
		GridBagConstraints gbc_messagingPortField = new GridBagConstraints();
		gbc_messagingPortField.insets = new Insets(0, 0, 5, 5);
		gbc_messagingPortField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messagingPortField.gridx = 1;
		gbc_messagingPortField.gridy = 1;
		messagingPropertiesPanel.add(messagingPortField, gbc_messagingPortField);
		messagingPortField.setColumns(10);
		
		verticalStrut_3 = Box.createVerticalStrut(20);
		add(verticalStrut_3);
		
		connectButton = new JButton("Connect to appliance");
		add(connectButton);
		connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalGlue.setPreferredSize(new Dimension(0, 32768));
		add(verticalGlue);
	}
	
}
