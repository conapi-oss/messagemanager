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
package nl.queuemanager.ui.message;

import java.awt.Dimension;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.PairTableModel;


/**
 * This class represents the message properties table for a message
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class MessagePropertiesTable extends JTable {

	public MessagePropertiesTable() {
		super();
		
		PropertyTableModel model = new PropertyTableModel();
		setModel(model);
		
		setMinimumSize(new Dimension(200, 10));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set the column width and lock the columns
		TableColumn col = getColumnModel().getColumn(0);
		col.setMinWidth(100);
		col.setPreferredWidth(120);
		
		// Lock the table for dragging of columns
		getTableHeader().setReorderingAllowed(false);		
	}
	
	@SuppressWarnings("unchecked")
	public void setMessage(Message message) {
		List<Pair<String, Object>> data = CollectionFactory.newArrayList();

		if(message != null) {
			try {			
				Enumeration<String> names = message.getPropertyNames();
				while(names.hasMoreElements()) {
					String name = names.nextElement();
					data.add(Pair.create(name, message.getObjectProperty(name)));
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		((PropertyTableModel)getModel()).setData(data);
	}	
	
	public void setProperties(Map<String, ? extends Object> props) {
		List<Pair<String, Object>> data = CollectionFactory.newArrayList();
		if(props != null) {
			for(Map.Entry<String, ? extends Object>entry: props.entrySet()) {
				data.add(Pair.create(entry.getKey(), (Object)entry.getValue()));
			}
		}
		((PropertyTableModel)getModel()).setData(data);
	}
	
	static class PropertyTableModel extends PairTableModel<String, Object> {

		public PropertyTableModel() {
			setColumnNames(new String[] {"Name", "Value", "Type"});
			setColumnTypes(new Class[] {String.class, String.class, String.class});
		}
		
 		@Override
		public void setData(List<Pair<String, Object>> data) {
			Collections.sort(data, Pair.compareFirst(String.CASE_INSENSITIVE_ORDER));
			super.setData(data);
		}
		
		@Override
		public Object getColumnValue(Pair<String, Object> item, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return item.first();
			case 1:
				return item.second().toString();
			case 2:
				return item.second().getClass().getSimpleName();
			}
			return null;
		}
		
	}
}
