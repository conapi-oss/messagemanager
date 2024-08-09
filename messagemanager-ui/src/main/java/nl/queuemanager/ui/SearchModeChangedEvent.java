package nl.queuemanager.ui;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SearchModeChangedEvent extends EventObject {
	public enum SearchMode {
		FILTER, NO_FILTER, INVERSE_FILTER
	}

	private final SearchMode mode;

	public SearchModeChangedEvent(Object source, SearchMode mode) {
		super(source);
		this.mode = mode;
	}

	public SearchMode getMode() {
		return mode;
	}

}
