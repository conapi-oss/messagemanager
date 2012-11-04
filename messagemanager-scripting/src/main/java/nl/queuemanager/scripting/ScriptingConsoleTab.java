package nl.queuemanager.scripting;
import javax.swing.JComponent;
import javax.swing.JPanel;

import nl.queuemanager.ui.UITab;

public class ScriptingConsoleTab extends JPanel implements UITab {

	public ScriptingConsoleTab() {
		add(new ScriptingPanel());
	}
	
	public String getUITabName() {
		return "Console";
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
