package nl.queuemanager.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;

import com.google.common.eventbus.EventBus;

import nl.queuemanager.ui.util.DocumentAdapter;

public class SearchFieldPublisher extends DocumentAdapter {
	private final Object searchField;
	private EventBus eventBus;

	public SearchFieldPublisher(EventBus eventBus, Object searchField) {
		this.eventBus = eventBus;
		this.searchField = searchField;
	}

	@Override
	public void updated(DocumentEvent e) {
		try {
			int length = e.getDocument().getLength();
			String text = e.getDocument().getText(0, length);
			eventBus.post(new GlobalHighlightEvent(searchField, text));
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}
}
