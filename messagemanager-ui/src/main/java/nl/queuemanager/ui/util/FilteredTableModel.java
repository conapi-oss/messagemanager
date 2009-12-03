/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import nl.queuemanager.core.util.CollectionFactory;

/**
 * A tablemodel that is able to filter it's backing model and only display
 * the rows that match the filter.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class FilteredTableModel<T extends Comparable<? super T>> implements TableModel, TableModelListener {
	private final ListTableModel<T> realModel;
	private final int filterColumn;
	private final List<TableModelListener> listeners;
	
	private Comparator<T> comparator; 
	private String filterValue;
	private String regex;
	private int[] indexMap;
	
	/**
	 * Create a new FilteredTableModel with the given model as the backing
	 * model and a certain filter column.
	 * 
	 * @param model The backing model
	 * @param column The column number to filter on (0 based)
	 */
	public FilteredTableModel(final ListTableModel<T> model, final int column) {
		this.realModel = model;
		this.filterColumn = column;
		this.listeners = CollectionFactory.newArrayList();
		
		setComparator(null); // Will rebuild index
		realModel.addTableModelListener(this);
	}
	
	/**
	 * Apply the filter to the backing model and rebuild the internal index. This
	 * may be useful if the backing model has been changed without raising an
	 * event to indicate that.
	 */
	private void rebuildIndex() {
		ArrayList<Integer> tempMap = CollectionFactory.newArrayList();
		
		// Build a list of rows that meet the filter criteria
		for(int realIndex=0; realIndex < realModel.getRowCount(); realIndex++) {
			if(accept(realIndex))
				tempMap.add(realIndex);
		}
		
		// Sort the indexes based on comparing the real objects
		Collections.sort(tempMap, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return comparator.compare(
					realModel.getRowItem(o1),
					realModel.getRowItem(o2));
			}
		});
		
		// Convert to array and set the new map as the indexmap
		int[] newMap = new int[tempMap.size()];
		int newIndex = 0;
		for(Integer realIndex: tempMap) {
			newMap[newIndex++] = realIndex;
		}
		indexMap = newMap;
	}
	
	/**
	 * Determine if a single item meets the filter criteria.
	 * 
	 * @param row The index from the backing model to check. 
	 */ 
	protected boolean accept(int row) {
		if(regex == null)
			return true;
		
		Object value = realModel.getValueAt(row, filterColumn);
		
		if(value == null)
			return false;
		
		// Convert the filter with wildcards to a regex
		return value.toString().toLowerCase().matches(regex);
	}
	
	/**
	 * Sets the Filter value and compiles it to a regular expression.
	 * 
	 * @param filterValue
	 */
	public void setFilterValue(String filterValue) {
		this.filterValue = (filterValue == null ? null : filterValue);
		this.regex = filterValue == null ? null : filterValue.toLowerCase();
		
		// Escape all characters that are to be taken literally in the filter
		if(regex != null) {
			regex = regex.replace("\\", "\\\\");
			regex = regex.replace("[", "\\[");
			regex = regex.replace("]", "\\]");
			regex = regex.replace("^", "\\^");
			regex = regex.replace("$", "\\$");
			regex = regex.replace("{", "\\{");
			regex = regex.replace("}", "\\}");
			regex = regex.replace(".", "\\.");
			regex = regex.replace("*", ".*");
			regex = regex.replace("?", ".");
			
			// startsWith
			regex = regex + ".*";
		}
		
		rebuildIndex();
		fireTableChanged();
	}

	/**
	 * Return the original filter value (not the regular expression)
	 * 
	 * @return
	 */
	public String getFilterValue() {
		return filterValue;
	}
	
	/**
	 * Get the comparator used to sort the entries
	 * 
	 * @return
	 */
	public Comparator<T> getComparator() {
		return comparator;
	}

	/**
	 * Set the comparator to sort the entries.
	 * 
	 * @param comparator
	 */
	public void setComparator(Comparator<T> comparator) {
		if(comparator != null) {
			this.comparator = comparator; 
		} else {
			this.comparator = new DefaultComparator<T>();
		}
		rebuildIndex();
		fireTableChanged();
	}

	/**
	 * Get the row number after filtering for the row in the backing model
	 *  
	 * @param row
	 * @return
	 */
	public int getMappedIndexFor(int row) {
		for(int mappedIndex=0; mappedIndex < indexMap.length; mappedIndex++) {
			if(indexMap[mappedIndex] == row) {
				return mappedIndex;
			}
		}
		
		return -1;
	}
	
	/**
	 * Get the real index (from the backing model) from a filtered index.
	 * 
	 * @param index
	 * @return
	 */
	public int getRealIndexFor(int index) {
		return indexMap[index];
	}

	/****************************************************
	 * Implementing TableModel, but translating indexes *
	 ***************************************************/
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public Class<?> getColumnClass(int columnIndex) {
		return realModel.getColumnClass(columnIndex);
	}

	public int getColumnCount() {
		return realModel.getColumnCount();
	}

	public String getColumnName(int columnIndex) {
		return realModel.getColumnName(columnIndex);
	}

	public int getRowCount() {
		return indexMap.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return realModel.getValueAt(getRealIndexFor(rowIndex), columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return realModel.isCellEditable(getRealIndexFor(rowIndex), columnIndex);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		realModel.setValueAt(value, getRealIndexFor(rowIndex), columnIndex);
	}
	
	/**
	 * Underlying model changed. Rebuid the index if required and warn the listeners. 
	 */
	public void tableChanged(TableModelEvent e) {
		// Is this a whole table refresh or row add/remove? Rebuild the index. 
		if(e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.DELETE
		||(e.getFirstRow() == 0 && e.getLastRow() > realModel.getRowCount())) {
			rebuildIndex();
			fireTableChanged();
			return;
		}
		
		// Try to determine whether the changed rows are included in the filter
		int firstRow = getMappedIndexFor(e.getFirstRow());
		int lastRow = getMappedIndexFor(e.getLastRow());
				
		// Event is entirely within the filtered results, map the event
		if(firstRow != -1 && lastRow != -1) {
			TableModelEvent event = new TableModelEvent(this, firstRow, lastRow);
			fireTableChanged(event);
			return;
		}
		
		// Event is entirely outside the filtered results. Ignore it.
		if(firstRow == -1 && lastRow == -1) {
			return;
		}
		
		// Event is partially in the filtered results. fire whole table change	
		if(firstRow == -1 || lastRow == -1) {
			rebuildIndex();
			fireTableChanged();
			return;
		}
	}
	
	protected void fireTableChanged() {
		fireTableChanged(new TableModelEvent(this));
	}
	
	protected void fireTableChanged(TableModelEvent event) {
		for(int i = listeners.size()-1; i >= 0; i--) {
			listeners.get(i).tableChanged(event);
		}
	}
	
	private class DefaultComparator<U extends Comparable<? super U>> implements Comparator<U> {
		public int compare(U o1, U o2) {
			return o1.compareTo(o2);
		}
	}
}
