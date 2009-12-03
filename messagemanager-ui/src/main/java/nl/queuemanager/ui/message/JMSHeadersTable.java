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
import java.text.SimpleDateFormat;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.PairTableModel;


/**
 * This class represents the JMS headers table for a message
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
public class JMSHeadersTable extends JTable {

	private final SimpleDateFormat dateFormatter = 
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
	
	public JMSHeadersTable() {
		super();
		
		PairTableModel<String, String> model = new PairTableModel<String, String>();
		model.setColumnNames(new String[] {"JMS header", "Value"});
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
	public void setMessage(Message message) {
		List<Pair<String, String>> data = CollectionFactory.newArrayList();
		
		if(message != null) {
			try {			
				data.add(Pair.create("JMSDestination", safeToString(message.getJMSDestination())));
				data.add(Pair.create("JMSDeliveryMode", Integer.toString(message.getJMSDeliveryMode())));
				data.add(Pair.create("JMSMessageID", message.getJMSMessageID()));
				data.add(Pair.create("JMSTimestamp", dateFormatter.format(message.getJMSTimestamp())));
				data.add(Pair.create("JMSCorrelationID", message.getJMSCorrelationID()));
				data.add(Pair.create("JMSCorrelationID (hex)", toHex(message.getJMSCorrelationIDAsBytes())));
				data.add(Pair.create("JMSReplyTo", safeToString(message.getJMSReplyTo())));
				data.add(Pair.create("JMSRedelivered", Boolean.toString(message.getJMSRedelivered())));
				data.add(Pair.create("JMSType", message.getJMSType()));
				data.add(Pair.create("JMSExpiration", message.getJMSExpiration() != 0 ? 
						dateFormatter.format(message.getJMSExpiration()) : "No expiration"));
				data.add(Pair.create("JMSPriority", Integer.toString(message.getJMSPriority())));			
			} catch (JMSException e) {
				
			}
		}
		
		((PairTableModel<String, String>)getModel()).setData(data);
	}
	
	private String toHex(byte[] correlationID) {
		if(correlationID == null)
			return "";
		
		StringBuffer result = new StringBuffer();
		
		for(byte b: correlationID) {
			String hex = Integer.toHexString(b&0xff);
			if(hex.length() == 1)
				result.append("0");

			result.append(hex);
			result.append(' ');
		}
		
		return result.toString();
	}

	private String safeToString(Object obj) {
		if(obj != null)
			return obj.toString();
		return "";
	}
}
