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

import javax.swing.table.AbstractTableModel;
import java.util.List;

@SuppressWarnings("serial")
public abstract class ListTableModel<T> extends AbstractTableModel {
	protected List<T> data;
	protected String[] columnNames;
	protected Class<?>[] columnTypes;
	
	public List<T> getData() {
		return data;
	}
	
	public void setData(List<T> data) {
		this.data = data;
		fireTableDataChanged();
	}
	
	public void addRow(T item) {
		if(data == null)
			throw new IllegalStateException("Cannot add rows when data == null");
		
		data.add(item);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}
	
	public void removeRow(T item) {
		if(data == null)
			throw new IllegalStateException("Cannot remove rows when data == null");
		
		int row = data.indexOf(item);
		if(row >= 0) {
			data.remove(item);
			fireTableRowsDeleted(row, row);
		} else {
			throw new IllegalStateException("Attempt to remove item that was not in data");
		}
	}
	
	public void setColumnNames(String[] names) {
		columnNames = names;
	}
	
	public void setColumnTypes(Class<?>[] types) {
		columnTypes = types;
	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return (data == null) ? 0 : data.size();
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return columnTypes[col];
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public T getRowItem(int row) {
		return 
			(data == null) ? null :
				(row < 0 || row > data.size()) ? null :
					data.get(row);
	}
	
	public void setRowItem(int row, T item) {
		data.set(row, item);
		fireTableRowsUpdated(row, row);
	}
	
	
	public int getItemRow(T item) {
		if(data.contains(item)) {
			return data.indexOf(item);
		} else {
			return -1;
		}
	}
	
	public Object getValueAt(int row, int col) {
		T item =
			(data == null) ? null :
				(row < 0 || row >= data.size()) ? null :
					(col < 0 || col >= getColumnCount()) ? null :
						data.get(row);
		
		if(item == null)
			return null;
		
		return getColumnValue(item, col);
	}

	public abstract Object getColumnValue(T item, int columnIndex);
}
