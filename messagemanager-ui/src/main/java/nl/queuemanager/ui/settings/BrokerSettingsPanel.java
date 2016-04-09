package nl.queuemanager.ui.settings;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import com.google.inject.Inject;

import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.jms.JMSBroker;

@SuppressWarnings("serial")
public class BrokerSettingsPanel extends JPanel implements SettingsPanel {

	private final CoreConfiguration configuration;
	private final JList brokerList;

	@Inject
	public BrokerSettingsPanel(CoreConfiguration config) {
		this.configuration = config;
		
		DefaultListModel model = new DefaultListModel();
		brokerList = new JList(model);
		JPanel brokerPanel = new JPanel();
		brokerPanel.setBorder(BorderFactory.createTitledBorder("Brokers"));
		brokerPanel.add(brokerList);
		add(brokerPanel);
	}
	
	public JComponent getUIPanel() {
		return this;
	}

	public void readSettings() {
		DefaultListModel model = (DefaultListModel)brokerList.getModel();
		model.clear();
		
		List<JMSBroker> brokers = configuration.listBrokers();
		for(JMSBroker broker: brokers) {
			model.addElement(broker);
		}
	}

	public void saveSettings() {
		// TODO Auto-generated method stub
		
	}

}
