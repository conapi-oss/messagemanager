package nl.queuemanager.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ConnectionTabPanel extends JPanel implements UITab {
	
	public String getUITabName() {
		return "Connection";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
			ConnectionState.CONNECTED,
			ConnectionState.DISCONNECTED
		};
	}

}
