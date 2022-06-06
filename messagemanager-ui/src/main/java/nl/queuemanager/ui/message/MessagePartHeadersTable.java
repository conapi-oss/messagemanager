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
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.ui.util.PairTableModel;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;

/**
 * This class represents the message headers table for a message
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
class MessagePartHeadersTable extends JTable {

	public MessagePartHeadersTable() {
		super();
		
		PairTableModel<String, String> model = new PairTableModel<String, String>();
		model.setColumnNames(new String[] {"Header name", "Value"});
		model.setColumnTypes(new Class[] {String.class, String.class});
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
	public void setMessagePart(JMSPart messagePart) {
		List<Pair<String, String>> data = CollectionFactory.newArrayList();

		if(messagePart != null) {
			Enumeration<String> names = messagePart.getHeaderFieldNames();
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				data.add(Pair.create(name, messagePart.getHeaderField(name)));
			}
		}
		
		((PairTableModel<String, String>)getModel()).setData(data);
	}	
}
