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

import com.google.inject.Inject;
import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.core.tasks.FireRefreshRequiredTask.JMSDestinationHolder;
import nl.queuemanager.ui.util.FilteredTableModel;
import nl.queuemanager.ui.util.ObservingListTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a topic subscriber table. It contains JMSTopicSubscriber objects.
 * These objects link a TopicSubscriber to a MessageBuffer and allow the messages
 * to be queried.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class TopicSubscriberTable extends JTable implements Clearable, JMSDestinationHolder {	
	private boolean filterEnabled = false;
	private FilterColumnListener filterColumnListener;
	
	private FilteredTableModel<JMSSubscriber> filteredModel;
	private TopicTableModel realModel; 
	
	@Inject
	public TopicSubscriberTable(JMSDestinationTransferHandlerFactory destinationHandlerFactory) {
		super();
		
		setModel(new TopicTableModel());
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		setColumnWidth(0, 20);
		setColumnWidth(2, 70);
		
		TableCellRenderer renderer = new MessageCountTableCellRenderer();
		setDefaultRenderer(Integer.class, renderer);
		
		setTransferHandler(destinationHandlerFactory.create(this));
		setDragEnabled(true);
		setFilterEnabled(true);
	}

	private void setColumnWidth(final int column, final int width) {
		TableColumn col = getColumnModel().getColumn(column);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
	}
	
	private void setModel(TopicTableModel model) {
		realModel = model;
		filteredModel = new FilteredTableModel<JMSSubscriber>(model, 1);
		super.setModel(filteredModel);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {
		if(column != 0)
			throw new IllegalArgumentException("Only column 0 is editable, not column " + column);
		
		if(value == null)
			throw new IllegalArgumentException("value must not be null");
		
		if(!(value instanceof Boolean))
			throw new IllegalArgumentException("Only Boolean values are accepted, not " + value.getClass().getName());
		
		getRowItem(row).setActive((Boolean)value);
	}
	
	public void ensureRowVisible(int row) {
		scrollRectToVisible(getCellRect(row, 0, true));
	}
	
	public void setData(List<JMSSubscriber> topics) {
		// If the current data is not null, deactivate all entries
		List<JMSSubscriber> currentData = realModel.getData();
		if(currentData != null) {
			for(JMSSubscriber entry: currentData) {
				entry.setActive(false);
			}
		}
		
		realModel.setData(topics);
	}
	
	public void addItem(JMSSubscriber item) {
		if(realModel.getData() == null)
			setData(new ArrayList<JMSSubscriber>());
		realModel.addRow(item);
	}
	
	public void removeItem(JMSSubscriber item) {
		realModel.removeRow(item);
	}
	
	public void clear() {
		setData(null);
	}
	
	public JMSDestination getJMSDestination() {
		JMSSubscriber item = getSelectedItem();
		if(item != null)
			return item.getDestination();
		return null;
	}
	
	public List<JMSDestination> getJMSDestinationList() {
		final ArrayList<JMSDestination> list = CollectionFactory.newArrayList();
		
		int[] selectedRows = getSelectedRows();
		for(int selectedIndex: selectedRows) {
			list.add(getRowItem(selectedIndex).getDestination());
		}
		
		return list;
	}
	
	public void refreshRequired(JMSDestination destination) {
	}
	
	public JMSSubscriber getSelectedItem() {
		if(getSelectedRow() >= 0)
			return realModel.getRowItem(filteredModel.getRealIndexFor(getSelectedRow()));
		else
			return null;
	}
	
	/**
	 * Return the subscriber for a certain destination, if there is one.
	 * 
	 * @param destination
	 * @return
	 */
	public JMSSubscriber getItemForDestination(JMSDestination destination) {
		int rowCount = realModel.getRowCount();
		for(int row=0; row<rowCount; row++) {
			JMSSubscriber item = realModel.getRowItem(row);
			
			if(item.getDestination().equals(destination))
				return item;
		}
		
		return null;
	}
	
	public JMSSubscriber getRowItem(int row) {
		if(row >= 0)
			return realModel.getRowItem(filteredModel.getRealIndexFor(row));
		else
			return null;
	}

	/**
	 * Return the filtered index for the item.
	 * 
	 * @param item
	 * @return The index for the item in the current (filtered) view or -1 if the item is not contained in the model or filtered.
	 */
	public int getItemRow(JMSSubscriber item) {
		if(item == null)
			return -1;
		
		return filteredModel.getMappedIndexFor(realModel.getItemRow(item));
	}
	
	public void setSelectedItem(JMSSubscriber topic) {
		if(topic == null) {
			getSelectionModel().clearSelection();
			return;
		}
		
		int row = filteredModel.getMappedIndexFor(realModel.getItemRow(topic));
		
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
			filterColumnListener = new FilterColumnListener(this, 1);
			getTableHeader().addMouseListener(filterColumnListener);
			getTableHeader().setToolTipText("Click 'Topic name' to filter table");
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
	 * TableModel for the Topic table.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private static class TopicTableModel extends ObservingListTableModel<JMSSubscriber> {
		public TopicTableModel() {
			setColumnNames(new String[] {"", "Name / Pattern", "Messages"});
			setColumnTypes(new Class[] {Boolean.class, String.class, Integer.class});
		}
		
		@Override
		public Object getColumnValue(JMSSubscriber entry, int col) {
			switch(col) {
			case 0:
				return entry.isActive();
				
			case 1:
				return entry.getDestination().toString(); // getName() sometimes holds a URI
				
			case 2:
				int messageCount = entry.getMessageCount();
				if(messageCount > 0) 
					return messageCount;
				
				return entry.isActive() ? messageCount : null;
				
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
	private static class FilterColumnListener extends MouseAdapter {
		
		private final TopicSubscriberTable table;
		private final int column;
		
		private FilterColumnListener(TopicSubscriberTable table, int column) {
			this.table = table;
			this.column = column;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());

			if(columnModelIndex == column) {
				String currentFilterValue = table.getFilterValue();
				String newFilterValue = 
					JOptionPane.showInputDialog(
							"Show only topics matching wildcard (?=one char, *=multiple chars)", 
							currentFilterValue == null ? "" : currentFilterValue);
				if(newFilterValue != null) {
					table.setFilterValue(newFilterValue);
		  		}
	  		}
		}
	}
}
