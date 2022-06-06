package nl.queuemanager.ui.message;

import nl.queuemanager.core.Pair;
import nl.queuemanager.ui.util.HighlightsModel;
import nl.queuemanager.ui.util.ListTableModel;

import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.swing.*;

class MapMessageContentViewer implements MessageContentViewer {

	private final PairValueHighlighter highlighter;
	
	@Inject
	public MapMessageContentViewer(PairValueHighlighter highlighter) {
		this.highlighter = highlighter;
	}
	
	public JComponent createUI(Message message) {
		MapMessageTable table = new MapMessageTable();
		table.setHighlightsModel(HighlightsModel.with(
				(ListTableModel<Pair<?, ?>>)table.getModel(), 
				highlighter));
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
