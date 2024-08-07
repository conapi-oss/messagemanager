package nl.queuemanager.ui;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SearchModeChangedEvent extends EventObject {

	private final boolean filterEndabled;

	public SearchModeChangedEvent(Object source, boolean filterEndabled) {
		super(source);
		this.filterEndabled = filterEndabled;
	}

	public boolean isFilterEndabled() {
		return filterEndabled;
	}

}
