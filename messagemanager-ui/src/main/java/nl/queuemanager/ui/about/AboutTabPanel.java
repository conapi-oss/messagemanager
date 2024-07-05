package nl.queuemanager.ui.about;

import com.google.inject.Inject;
import nl.queuemanager.ui.UITab;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

@SuppressWarnings("serial")
public class AboutTabPanel extends JPanel implements UITab {

	@Inject
	public AboutTabPanel(Map<String, AboutPanel> aboutPages) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		for(Map.Entry<String, AboutPanel> entry: aboutPages.entrySet()) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
			JPanel uiPanel = (JPanel) entry.getValue().getUIPanel();
			panel.add(uiPanel);
		//	panel.add(createActionPanel(entry.getValue()));
			panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
			add(panel);
		}
		
	}
	


	public String getUITabName() {
		return "About";
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
