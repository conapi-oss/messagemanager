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

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.MMJTable;
import nl.queuemanager.ui.util.PairTableModel;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * This class represents the table to display a MapMessage
 * 
 * @author Gerco Dries (gerco@gdries.nl)
 *
 */
@SuppressWarnings("serial")
public class MapMessageTable extends MMJTable {

	public MapMessageTable() {
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
	public void setMessage(MapMessage message) {
		List<Pair<String, Object>> data = CollectionFactory.newArrayList();

		if(message != null) {
			try {			
				Enumeration<String> names = message.getMapNames();
				while(names.hasMoreElements()) {
					String name = names.nextElement();
					data.add(Pair.create(name, message.getObject(name)));
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		((PropertyTableModel)getModel()).setData(data);
	}	
	
	public void setProperties(Map<String, ? extends Object> props) {
		List<Pair<String, Object>> data = CollectionFactory.newArrayList();
		if(props != null) {
			for(String key: props.keySet()) {
				data.add(Pair.create(key, (Object)props.get(key)));
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
		public Object getColumnValue(Pair<String, Object> item, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return item.first();
			case 1:
				return getValueString(item.second());
			case 2:
				return item.second().getClass().getSimpleName();
			}
			return null;
		}

		private static Object getValueString(Object obj) {
			if(obj instanceof Byte) {
				return toHex(new byte[] {((Byte)obj).byteValue()});
			}
			
			if(obj instanceof byte[]) {
				return toHex((byte[])obj);
			}
			
			return obj.toString();
		}
			
		private static String toHex(byte[] bytes) {
			if(bytes == null)
				return "";
			
			String result = "";
			
			for(byte b: bytes) {
				String hex = Integer.toHexString(b&0xff);
				if(hex.length() == 1)
					result += "0" + hex + " ";
				else
					result += hex + " ";
			}
			
			return result;
		}
		
	}
}
