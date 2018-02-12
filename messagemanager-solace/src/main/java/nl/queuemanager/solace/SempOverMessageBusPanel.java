package nl.queuemanager.solace;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Color;

class SempOverMessageBusPanel extends JPanel implements DataPanel<SempConnectionDescriptor> {
	private JTextField messageVpnField;
//	private JTextField applianceNameField;
	
	public SempOverMessageBusPanel() {
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "SEMP over Message Bus", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblMessageVpn = new JLabel("Message VPN:");
		GridBagConstraints gbc_lblMessageVpn = new GridBagConstraints();
		gbc_lblMessageVpn.anchor = GridBagConstraints.EAST;
		gbc_lblMessageVpn.insets = new Insets(5, 5, 5, 5);
		gbc_lblMessageVpn.gridx = 0;
		gbc_lblMessageVpn.gridy = 0;
		add(lblMessageVpn, gbc_lblMessageVpn);
		
		messageVpnField = new JTextField();
		GridBagConstraints gbc_messageVpnField = new GridBagConstraints();
		gbc_messageVpnField.insets = new Insets(5, 0, 5, 5);
		gbc_messageVpnField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageVpnField.gridx = 1;
		gbc_messageVpnField.gridy = 0;
		add(messageVpnField, gbc_messageVpnField);
		messageVpnField.setColumns(10);
		
//		JLabel lblApplianceName = new JLabel("Appliance name:");
//		GridBagConstraints gbc_lblApplianceName = new GridBagConstraints();
//		gbc_lblApplianceName.insets = new Insets(5, 5, 5, 5);
//		gbc_lblApplianceName.gridx = 0;
//		gbc_lblApplianceName.gridy = 1;
//		add(lblApplianceName, gbc_lblApplianceName);
//		
//		applianceNameField = new JTextField();
//		applianceNameField.setToolTipText("The hostname configured on the appliance. This may be\ndifferent from the host name you are using to connect to it.");
//		GridBagConstraints gbc_applianceNameField = new GridBagConstraints();
//		gbc_applianceNameField.insets = new Insets(5, 0, 5, 5);
//		gbc_applianceNameField.fill = GridBagConstraints.HORIZONTAL;
//		gbc_applianceNameField.gridx = 1;
//		gbc_applianceNameField.gridy = 1;
//		add(applianceNameField, gbc_applianceNameField);
//		applianceNameField.setColumns(10);
	}
	
	@Override
	public void displayItem(SempConnectionDescriptor d) {
		if(d == null) {
			messageVpnField.setText("");
//			applianceNameField.setText("");
		} else {
			messageVpnField.setText(d.getMessageVpn());
//			applianceNameField.setText(d.getApplianceName());
		} 
	}
	
	@Override
	public void updateItem(SempConnectionDescriptor d) {
		d.setMessageVpn(messageVpnField.getText());
//		d.setApplianceName(applianceNameField.getText());
	}

}
