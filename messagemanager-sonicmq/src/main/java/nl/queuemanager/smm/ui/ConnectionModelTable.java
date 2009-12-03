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
package nl.queuemanager.smm.ui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import nl.queuemanager.smm.ConnectionModel;
import nl.queuemanager.ui.util.ListTableModel;


class ConnectionModelTable extends JTable {

	public ConnectionModelTable() {
		super();
	
		setMinimumSize(new Dimension(300, 200));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setModel(new ConnectionModelTableModel());
		
		setAutoCreateColumnsFromModel(true);		
	}
	
	public void setData(List<ConnectionModel> models) {
		((ConnectionModelTableModel)getModel()).setData(models);
	}
	
	public ConnectionModel getSelectedItem() {
		if(getSelectedRow() >= 0)
			return ((ConnectionModelTableModel)getModel()).getRowItem(getSelectedRow());
		else
			return null;
	}
	
	private static class ConnectionModelTableModel extends ListTableModel<ConnectionModel> {

		public ConnectionModelTableModel() {
			setColumnNames(new String[] {"Connection", "Domain", "Username", "Url"});
			setColumnTypes(new Class[] {String.class, String.class, String.class, String.class});
		}
		
		@Override
		public Object getColumnValue(ConnectionModel item, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return item.getConnectionName();
			case 1:
				return item.getDomainName();
			case 2:
				return item.getUserName();
			case 3:
				return item.getUrl();
			default:
				return null;
			}
		}
		
	}
}
