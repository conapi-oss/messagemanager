package nl.queuemanager.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ConnectionTabPanel extends JPanel implements UITab {
	
	@Override
	public String getUITabName() {
		return "Connection";
	}

	@Override
	public JComponent getUITabComponent() {
		return this;
	}

	@Override
	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
			ConnectionState.CONNECTED,
			ConnectionState.DISCONNECTED
		};
	}

}
