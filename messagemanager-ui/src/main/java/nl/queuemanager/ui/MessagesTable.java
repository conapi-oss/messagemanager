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
package nl.queuemanager.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.JMSMultipartMessage;
import nl.queuemanager.core.jms.JMSQueue;
import nl.queuemanager.core.jms.JMSTopic;
import nl.queuemanager.core.jms.JMSXMLMessage;
import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.ListTableModel;


/**
 * This class represents the Messages table and all information about its markup
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class MessagesTable extends JTable implements Clearable {
	private JMSDestination currentDestination;
	
	public MessagesTable() {
		super();
		
		setModel(new MessageTableModel());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		getTableHeader().setReorderingAllowed(false);
		
		{
			TableColumn col = getColumnModel().getColumn(0);
			col.setMinWidth(40);
			col.setMaxWidth(40);
			col.setPreferredWidth(40);
		}{
			TableColumn col = getColumnModel().getColumn(3);
			col.setMinWidth(45);
			col.setMaxWidth(45);
			col.setPreferredWidth(45);
		}
		
		setDefaultRenderer(Date.class, new DateTableCellRenderer());
		
		// Drag and drop support
		setTransferHandler(new MessageExportTransferHandler());
		setDragEnabled(true);
	}
	
	public void setData(JMSDestination destination, List<Message> data) {
		this.currentDestination = destination;
		((MessageTableModel)getModel()).setData(data);
	}
	
	public JMSDestination getCurrentDestination() {
		return currentDestination;
	}
		
	public void clear() {
		setData(null, null);
	}
	
	public void addItem(Message item) {
		((MessageTableModel)getModel()).addRow(item);
	}
	
	public void removeItem(Message item) {
		((MessageTableModel)getModel()).removeRow(item);
	}

	public Message getSelectedItem() {
		if(getSelectedRow() >= 0)
			return ((MessageTableModel)getModel()).getRowItem(getSelectedRow());
		else
			return null;
	}

	public Message getRowItem(int row) {
		return ((MessageTableModel)getModel()).getRowItem(row);
	}
	
	private static class MessageTableModel extends ListTableModel<Message> {
		public MessageTableModel() {
			setColumnNames(new String[] {"#", "Timestamp", "Correlation ID", "Type"});
			setColumnTypes(new Class[] {Integer.class, Date.class, String.class, String.class});
		}
		
		@Override
		public Object getColumnValue(Message message, int columnIndex) {
			try {
				switch(columnIndex) {
				case 0:
					return this.getItemRow(message) + 1;
				case 1:
					return new Date(message.getJMSTimestamp());
				case 2:
					return message.getJMSCorrelationID();
				case 3:
					if (message instanceof JMSXMLMessage) {
						return "XML";
					} else if (message instanceof TextMessage) {
						return "Text";
					} else if (message instanceof BytesMessage) {
						return "Bytes";
					} else if (message instanceof JMSMultipartMessage) {
						return "Multi";
					} else if (message instanceof ObjectMessage) {
						return "Object";
					} else if (message instanceof StreamMessage) {
						return "Stream";
					} else {
						return "Message";
					}
				}
			} catch (JMSException je) {
				je.printStackTrace();
			}
			
			return null;
		}		
	}
	
	private static class DateTableCellRenderer extends DefaultTableCellRenderer {
		private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
	    @Override
		public void setValue(Object value) {
	        if ((value != null) && (value instanceof Date)) {
	        	
	        	value = formatter.format((Date)value);
	        } 
	        super.setValue(value);
	    }
	}
	
	private class MessageExportTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			if(!(c instanceof MessagesTable))
				return null;
			
			MessagesTable table = (MessagesTable)c;
			try {
				List<Message> messages = CollectionFactory.newArrayList();
				int[] selectedRows = table.getSelectedRows();
				for(int i: selectedRows)
					messages.add(table.getRowItem(i));
				
				if(currentDestination instanceof JMSQueue) {
					return MessageListTransferable.createFromJMSMessageList((JMSQueue)currentDestination, messages);
				} else if(currentDestination instanceof JMSTopic) {
					return MessageListTransferable.copyFromJMSMessageList(messages);
				}
				
				throw new IllegalStateException("currentDestination is not a JMSQueue and also not a JMSTopic. This is a bug!");
			} catch (JMSException e) {
				return null;
			}
		}

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}
	}		
}
