package nl.queuemanager.ui.util;

import java.util.HashSet;
import java.util.Set;

public abstract class HighlighterSupport<T> implements Highlighter<T> {

	private final Set<HighlighterListener> listeners = new HashSet<>();
	
	protected void resetHighlights() {
		for(HighlighterListener l: listeners) {
			l.resetHighlights();
		}
	}
	
	@Override
	public void addHighlighterListener(HighlighterListener l) {
		listeners.add(l);
	}

	@Override
	public void removeHighlighterListener(HighlighterListener l) {
		listeners.remove(l);
	}

}
