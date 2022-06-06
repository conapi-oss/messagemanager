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

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.ui.util.FilteredTableModel;
import nl.queuemanager.ui.util.ListTableModel;
import nl.queuemanager.ui.util.MessageCountComparator;
import nl.queuemanager.ui.util.MiscUtils;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents the Queues table and all information about its markup
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
class QueueTable extends JTable implements Clearable {

	private final Comparator<JMSQueue> messageCountComparator = new MessageCountComparator(true);
	
	private boolean filterEnabled = false;
	private FilterColumnListener filterColumnListener;
	
	private FilteredTableModel<JMSQueue> filteredModel;
	private QueueTableModel realModel; 
	
	@Inject
	public QueueTable(JMSDomain domain) {
		super();
		
		boolean enableMessageSizeColumn = domain.isFeatureSupported(JMSFeature.QUEUE_MESSAGES_SIZE);
		
		setModel(new QueueTableModel(enableMessageSizeColumn));
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		setColumnWidth(1, 70);
		if(enableMessageSizeColumn) {
			setColumnWidth(2, 50);
		}
		
		TableCellRenderer renderer = new MessageCountTableCellRenderer();
		setDefaultRenderer(Integer.class, renderer);
		
		setDragEnabled(true);
		setFilterEnabled(true);
		
		// Add sort column listener on the messages column
		getTableHeader().addMouseListener(new SortColumnListener(1)); 
	}
	
	private void setColumnWidth(int column, int width) {
		TableColumn col = getColumnModel().getColumn(column);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
	}
	
	public void setModel(QueueTableModel model) {
		realModel = model;
		filteredModel = new FilteredTableModel<JMSQueue>(model, 0); 
		super.setModel(filteredModel);
	}
	
	public QueueTableModel getQueueModel() {
		return realModel;
	}
	
	/**
	 * Create a copy of the data list and set that as the data source for the table.
	 * 
	 * @param queues
	 */
	public void setData(List<JMSQueue> queues) {
		realModel.setData(queues == null ? null : CollectionFactory.newArrayList(queues));
	}
	
	/**
	 * Updates existing items, any missing items in the list are added.
	 * 
	 * @param queues
	 */
	public void updateData(List<JMSQueue> queues) {
		if(realModel.getData() == null) {
			setData(queues);
		} else {
			for(JMSQueue q: queues) {
				int row = realModel.getItemRow(q);
				if(row != -1) {
					JMSQueue item = realModel.getRowItem(row);
					if(item.getMessageCount() != q.getMessageCount()
					|| !item.toString().equals(q.toString())) {
						realModel.setRowItem(row, q);
					}
				} else {
					realModel.addRow(q);
				}
			}
		}
	}
	
	public void clear() {
		setData(null);
	}
			
	public JMSQueue getSelectedItem() {
		if(getSelectedRow() >= 0)
			return realModel.getRowItem(filteredModel.getRealIndexFor(getSelectedRow()));
		else
			return null;
	}
	
	public JMSQueue getRowItem(int row) {
		if(row >= 0)
			return realModel.getRowItem(filteredModel.getRealIndexFor(row));
		else
			return null;
	}
	
	public void setSelectedItem(JMSQueue queue) {
		if(queue == null) {
			getSelectionModel().clearSelection();
			return;
		}
		
		int row = filteredModel.getMappedIndexFor(realModel.getItemRow(queue));
		
		if(row == -1)
			getSelectionModel().clearSelection();
		else
			getSelectionModel().setSelectionInterval(row, row);
	}
	
	public void setFilterValue(String filterValue) {
		TableColumn column = getColumnModel().getColumn(0);
			
		if(filterValue == null || filterValue.length()==0) {
			column.setHeaderValue(getModel().getColumnName(0));
		} else {
			column.setHeaderValue(getModel().getColumnName(0) + " (" + filterValue + "*)");
		}
		
		getTableHeader().repaint();
		
		filteredModel.setFilterValue(filterValue);
	}

	public String getFilterValue() {
		return filteredModel.getFilterValue();
	}

	public void setFilterEnabled(boolean filterEnabled) {
		this.filterEnabled = filterEnabled;
		
		if(filterEnabled) {
			// Set up a listener for a click on the tablecolumn to set the
			// filter value
			filterColumnListener = new FilterColumnListener(0);
			getTableHeader().addMouseListener(filterColumnListener);
			getTableHeader().setToolTipText("Click 'Queue name' to filter table; Click 'Messages' to sort by message count.");
			setFilterValue(null);
		} else {
			// Remove the table column listener if there is one
			getTableHeader().removeMouseListener(filterColumnListener);
			
			// Remove the filter value
			setFilterValue(null);
		}
	}

	public boolean isFilterEnabled() {
		return filterEnabled;
	}
	
	/**
	 * TableModel for the Queues table. Works with a List<IQueueData> to prevent
	 * useless copying of data.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private static class QueueTableModel extends ListTableModel<JMSQueue> {
		public QueueTableModel(boolean includeSizeColumn) {
			if(includeSizeColumn) {
				setColumnNames(new String[] {"Queue name", "Messages", "Size"});
				setColumnTypes(new Class[] {String.class, Integer.class, Long.class});
			} else {
				setColumnNames(new String[] {"Queue name", "Messages"});
				setColumnTypes(new Class[] {String.class, Integer.class});
			}
		}
		
		@Override
		public Object getColumnValue(JMSQueue queue, int col) {
			switch(col) {
			case 0:
				return queue.toString();
			case 1:
				return queue.getMessageCount();
			case 2:
				return MiscUtils.humanReadableSize(queue.getMessageSize());
			default:
				return null;					
			}
		}
		
	}
	
	/**
	 * This listener listens for clicks on the name column and sets the filterValue
	 * property whenever the "name" column is clicked after asking the user what
	 * the value should be.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private class FilterColumnListener extends MouseAdapter {
		private final int column;
		
		private FilterColumnListener(int column) {
			this.column = column;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = QueueTable.this.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());

			if(columnModelIndex == column) {
				String currentFilterValue = QueueTable.this.getFilterValue();
				String newFilterValue = 
					JOptionPane.showInputDialog(
							"Show only queues matching wildcard (?=one char, *=multiple chars)", 
							currentFilterValue == null ? "" : currentFilterValue);
				if(newFilterValue != null) {
					QueueTable.this.setFilterValue(newFilterValue);
		  		}
	  		}
		}
	}
	
	private class SortColumnListener extends MouseAdapter {
		private final int column;
		
		private SortColumnListener(int column) {
			this.column = column;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = QueueTable.this.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());

			if(columnModelIndex == column) {
				final Comparator<JMSQueue> cmp = filteredModel.getComparator();
				final TableColumn clickedColumn = getColumnModel().getColumn(column);
				
				if(cmp != messageCountComparator) {
					filteredModel.setComparator(messageCountComparator);
					clickedColumn.setHeaderValue(getModel().getColumnName(column) + "*");
				} else {
					filteredModel.setComparator(null);
					clickedColumn.setHeaderValue(getModel().getColumnName(column));
				}
				
				getTableHeader().repaint();
			}
		}
	}
}
