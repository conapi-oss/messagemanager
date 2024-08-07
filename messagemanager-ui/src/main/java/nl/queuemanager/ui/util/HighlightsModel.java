package nl.queuemanager.ui.util;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HighlightsModel<T> implements TableModelListener, HighlighterListener {
	private final WeakReference<ListTableModel<? extends T>> tableModel;
	private final Highlighter<T> highlighter;
	private final ArrayList<Boolean> highlights = new ArrayList<>();

	public HighlightsModel(ListTableModel<? extends T> tableModel, Highlighter<T> highlighter) {
		this.tableModel = new WeakReference<ListTableModel<? extends T>>(tableModel);
		this.highlighter = highlighter;
		highlighter.addHighlighterListener(this);
		tableModel.addTableModelListener(this);
		highlights.ensureCapacity(tableModel.getRowCount());
	}
	
	public static <T> HighlightsModel<T> with(ListTableModel<? extends T> tableModel, Highlighter<T> highlighter) {
		return new HighlightsModel<>(tableModel, highlighter);
	}
	
	public boolean isHighlighted(int row) {
		if(row < highlights.size())
			return highlights.get(row);
		return false;
	}
	
	@Override
	public void resetHighlights() {
		if(tableModel.get() != null) {
			int rowCount = tableModel.get().getRowCount();
			if (rowCount > 0) {
				tableModel.get().fireTableRowsUpdated(0, rowCount - 1);
			}
		}
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		@SuppressWarnings("unchecked")
		ListTableModel<T> tableModel = (ListTableModel<T>) e.getSource();
		switch(e.getType()) {
		case TableModelEvent.INSERT:
			for (int i=e.getFirstRow(); i<=e.getLastRow(); i++) {
				highlights.add(i, highlighter.shouldHighlight(tableModel.getRowItem(i)));
			}
			break;
		case TableModelEvent.DELETE:
			for (int i=e.getFirstRow(); i<=e.getLastRow(); i++) {
				highlights.remove(i);
			}
			break;
		case TableModelEvent.UPDATE:
			int first = e.getFirstRow();
			int last = e.getLastRow();
			
			if(first == 0 && last > tableModel.getRowCount()) {
				highlights.clear();
				highlights.ensureCapacity(tableModel.getRowCount());
				last = tableModel.getRowCount()-1;
			}
			
			if(tableModel.getRowCount() > 0) {
				for (int i=first; i<=last; i++) {
					T item = tableModel.getRowItem(i);
					if(item != null) {
						if(highlights.size() <= i) {
							highlights.add(i, highlighter.shouldHighlight(item));
						} else {
							highlights.set(i, highlighter.shouldHighlight(item));
						}
					}
				}
			}
			break;
		}

		// now the highlights are up to date, so we can call all listeners
		// call all listeners, so they can update their state and i.e. re-sort the table
		for (TableModelListener listener : listeners) {
			listener.tableChanged(e);
		}
	}

	private List<TableModelListener> listeners = new ArrayList<>();

	public void addTableModelListener(TableModelListener listener) {
		listeners.add(listener);
	}

	public void removeTableModelListener(TableModelListener listener) {
		listeners.remove(listener);
	}
	
}
