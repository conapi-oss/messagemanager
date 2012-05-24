package nl.queuemanager.ui.settings;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import com.google.inject.Inject;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.jms.JMSBroker;

@SuppressWarnings("serial")
public class BrokerSettingsPanel extends JPanel implements SettingsPanel {

	private final Configuration configuration;
	private final JList<JMSBroker> brokerList;

	@Inject
	public BrokerSettingsPanel(Configuration config) {
		this.configuration = config;
		
		DefaultListModel<JMSBroker> model = new DefaultListModel<JMSBroker>();
		brokerList = new JList<JMSBroker>(model);
		add(brokerList);
	}
	
	public JComponent getUIPanel() {
		return this;
	}

	public void readSettings() {
		List<JMSBroker> brokers = configuration.listBrokers();
		for(JMSBroker broker: brokers) {
			((DefaultListModel<JMSBroker>)brokerList.getModel()).addElement(broker);
		}
	}

	public void saveSettings() {
		// TODO Auto-generated method stub
		
	}

}
