package nl.queuemanager.ui.message;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

class MapMessageContentViewer implements MessageContentViewer {
	
	public JComponent createUI(Message message) {
		MapMessageTable table = new MapMessageTable();
		table.setMessage((MapMessage)message);
		return new JScrollPane(table);
	}

	public boolean supports(Message message) {
		return MapMessage.class.isAssignableFrom(message.getClass());
	}

	public String getDescription(Message message) {
		return "Map";
	}

}
