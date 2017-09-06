package nl.queuemanager.ui;

import java.util.EventObject;

@SuppressWarnings("serial")
public class GlobalHighlightEvent extends EventObject {

	private final String highlightString;

	public GlobalHighlightEvent(Object source, String highlightString) {
		super(source);
		this.highlightString = highlightString;
	}

	public String getHighlightString() {
		return highlightString;
	}

}
