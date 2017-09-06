package nl.queuemanager.ui.util;

/**
 * Highlighter is an interface that GUI components may choose to take an implementation
 * of to decide which content needs to be highlighted.
 * 
 * @param <T>
 */
public interface Highlighter<T> {
	public boolean shouldHighlight(T obj);

	public void addHighlighterListener(HighlighterListener l);
	public void removeHighlighterListener(HighlighterListener l);
}
