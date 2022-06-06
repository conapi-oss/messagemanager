package nl.queuemanager.ui;

import javax.swing.*;

public interface UITab {
	
	public enum ConnectionState {CONNECTED, DISCONNECTED}

	public String getUITabName();
	
	public JComponent getUITabComponent();
	
	public ConnectionState[] getUITabEnabledStates();
	
}
