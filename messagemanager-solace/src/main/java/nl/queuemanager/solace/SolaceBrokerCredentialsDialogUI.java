package nl.queuemanager.solace;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

class SolaceBrokerCredentialsDialogUI extends JDialog {
	private JPanel panel;
	protected JLabel lblMessageVpn;
	protected JLabel lblError;
	protected JComboBox<AuthenticationScheme> authenticationSchemeCombo;
	protected BasicAuthenticationPanel basicAuthenticationPanel;
	protected ClientCertificateAuthenticationPanel clientCertificateAuthenticationPanel;
	protected KeystorePanel truststorePanel;
	private Component verticalStrut;
	private Component verticalStrut_1;
	private Box buttonBox;
	protected JButton connectButton;
	protected JButton cancelButton;
	private Component horizontalGlue;

	public SolaceBrokerCredentialsDialogUI(JFrame owner) {
		super(owner);
		setModal(true);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JLabel lblPleaseEnterYour = new JLabel("Please enter your credentials to connect to:");
		panel.add(lblPleaseEnterYour);
		lblPleaseEnterYour.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblPleaseEnterYour.setHorizontalAlignment(SwingConstants.CENTER);
		
		verticalStrut = Box.createVerticalStrut(5);
		panel.add(verticalStrut);
		
		lblMessageVpn = new JLabel("Message VPN");
		panel.add(lblMessageVpn);
		lblMessageVpn.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblMessageVpn.setHorizontalAlignment(SwingConstants.CENTER);
		
		verticalStrut_1 = Box.createVerticalStrut(10);
		panel.add(verticalStrut_1);
		
		authenticationSchemeCombo = new JComboBox<AuthenticationScheme>();
		panel.add(authenticationSchemeCombo);
		authenticationSchemeCombo.setModel(new DefaultComboBoxModel<AuthenticationScheme>(AuthenticationScheme.values()));
		
		basicAuthenticationPanel = new BasicAuthenticationPanel();
		panel.add(basicAuthenticationPanel);
		
		clientCertificateAuthenticationPanel = new ClientCertificateAuthenticationPanel();
		panel.add(clientCertificateAuthenticationPanel);
		
		truststorePanel = new KeystorePanel();
		truststorePanel.setPrefix("Trust");
		panel.add(truststorePanel);
		
		buttonBox = Box.createHorizontalBox();
		panel.add(buttonBox);
		
		horizontalGlue = Box.createHorizontalGlue();
		buttonBox.add(horizontalGlue);
		
		connectButton = new JButton("Connect");
		buttonBox.add(connectButton);
		
		cancelButton = new JButton("Cancel");
		buttonBox.add(cancelButton);
		
		lblError = new JLabel("");
		panel.add(lblError);
		lblError.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblError.setForeground(Color.RED);
		lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
		clientCertificateAuthenticationPanel.setVisible(false);
	}
	
}
