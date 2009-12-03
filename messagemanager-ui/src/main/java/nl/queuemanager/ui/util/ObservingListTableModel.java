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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * A ListTableModel that observes it's contents and fires the appropriate events when
 * any Observable entry changes.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public abstract class ObservingListTableModel<T extends Observable> extends ListTableModel<T> implements Observer {

	@Override
	public void setData(List<T> data) {
		// If the data array is not empty, unregister ourselves from all observers in it.
		List<T> prevData = getData();
		if(prevData != null) {
			for(T o: prevData) {
				o.deleteObserver(this);
			}
		}

		// Register ourselves with all observers in the new data array
		if(data != null) {
			for(Observable o: data) {
				o.addObserver(this);
			}
		}
		
		super.setData(data);
	}
	
	@Override
	public void addRow(T item) {		
		item.addObserver(this);
		
		super.addRow(item);
	}	

	@Override
	public void setRowItem(int row, T item) {
		T prevItem = getRowItem(row);
		if(prevItem != null)
			prevItem.deleteObserver(this);
		
		item.addObserver(this);
		
		super.setRowItem(row, item);
	}
	
	@SuppressWarnings("unchecked")
	public void update(Observable observable, Object obj) {
		int row = getItemRow((T)observable);
		fireTableRowsUpdated(row, row);
	}

}
