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

import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSDestination.TYPE;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.core.tasks.FireRefreshRequiredTask.JMSDestinationHolder;
import nl.queuemanager.ui.util.FilteredTableModel;
import nl.queuemanager.ui.util.ListTableModel;

import jakarta.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a jms destination table. It displays JMSDestination objects.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class JMSDestinationTable extends JTable implements Clearable, JMSDestinationHolder {	
	private boolean filterEnabled = false;
	private FilterColumnListener filterColumnListener;	
	private FilteredTableModel<JMSDestination> filteredModel;
	private int filterColumnIndex = 1;
	
	private JMSDestinationTableModel realModel;
	
	@Inject
	public JMSDestinationTable(JMSDestinationTransferHandlerFactory jdthFactory) {
		super();
		
		setModel(new JMSDestinationTableModel());
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getTableHeader().setReorderingAllowed(false);
		setColumnWidth(0, 50);
		setColumnWidth(2, 70);
		
		setDefaultRenderer(Integer.class, new MessageCountTableCellRenderer());
		
		setTransferHandler(jdthFactory.create(this));
		setDragEnabled(true);
		setFilterEnabled(true);
	}

	private void setColumnWidth(final int column, final int width) {
		TableColumn col = getColumnModel().getColumn(column);
		col.setMinWidth(width);
		col.setMaxWidth(width);
		col.setPreferredWidth(width);
	}
	
	private void setModel(JMSDestinationTableModel model) {
		realModel = model;
		filteredModel = new FilteredTableModel<JMSDestination>(model, filterColumnIndex); 
		super.setModel(filteredModel);
	}
	
	public void ensureRowVisible(int row) {
		scrollRectToVisible(getCellRect(row, 0, true));
	}
			
	/**
	 * Create a copy of the data list and set that as the data source for the table.
	 * 
	 * @param destinations
	 */
	public void setData(List<? extends JMSDestination> destinations) {
		realModel.setData(destinations == null ? null : CollectionFactory.newArrayList(destinations));
	}
	
	/**
	 * Updates existing items, any other items in the list are added to the table.
	 * 
	 * @param destinations
	 */
	public void updateData(List<? extends JMSDestination> destinations) {
		if(realModel.getData() == null) {
			setData(destinations);
		} else {
			for(JMSDestination d: destinations) {
//				if(TYPE.QUEUE != d.getType())
//					continue;
				
				int row = realModel.getItemRow(d);
				if(row != -1) {
					// already exists
					if(TYPE.QUEUE == d.getType()) {
						JMSQueue item = (JMSQueue) realModel.getRowItem(row);
						if (item.getMessageCount() != ((JMSQueue) d).getMessageCount()) {
							realModel.setRowItem(row, d);
						}
					} else {
						// topic
						realModel.setRowItem(row, d);
					}
				} else {
					realModel.addRow(d);
				}
			}
		}
	}
	
	public void addItem(JMSDestination item) {
		realModel.addRow(item);
	}
	
	public void removeItem(JMSDestination item) {
		realModel.removeRow(item);
	}
	
	public void clear() {
		setData(null);
	}
	
	public JMSDestination getJMSDestination() {
		return getSelectedItem();
	}
	
	public List<JMSDestination> getJMSDestinationList() {
		final ArrayList<JMSDestination> list = CollectionFactory.newArrayList();
		
		int[] selectedRows = getSelectedRows();
		for(int selectedIndex: selectedRows) {
			list.add(getRowItem(selectedIndex));
		}
		
		return list;
	}
	
	public void refreshRequired(JMSDestination destination) {
	}
	
	public JMSDestination getSelectedItem() {
		if(getSelectedRow() >= 0)
			return realModel.getRowItem(filteredModel.getRealIndexFor(getSelectedRow()));
		else
			return null;
	}
	
	public JMSDestination getRowItem(int row) {
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
	public int getItemRow(JMSDestination item) {
		if(item == null)
			return -1;
		
		return filteredModel.getMappedIndexFor(realModel.getItemRow(item));
	}
	
	public void setSelectedItem(JMSDestination JMSDestination) {
		if(JMSDestination == null) {
			getSelectionModel().clearSelection();
			return;
		}
		
		int row = filteredModel.getMappedIndexFor(realModel.getItemRow(JMSDestination));
		
		if(row == -1)
			getSelectionModel().clearSelection();
		else
			getSelectionModel().setSelectionInterval(row, row);
	}
	
	public void setFilterValue(String filterValue) {
		TableColumn column = getColumnModel().getColumn(filterColumnIndex);
			
		if(filterValue == null || filterValue.length()==0) {
			column.setHeaderValue(getModel().getColumnName(filterColumnIndex));
		} else {
			column.setHeaderValue(getModel().getColumnName(filterColumnIndex) + " (" + filterValue + "*)");
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
			filterColumnListener = new FilterColumnListener(this, filterColumnIndex);
			getTableHeader().addMouseListener(filterColumnListener);
			getTableHeader().setToolTipText("Click 'Name' to filter table");
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
	 * TableModel for the JMSDestination table.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private static class JMSDestinationTableModel extends ListTableModel<JMSDestination> {
		public JMSDestinationTableModel() {
			setColumnNames(new String[] {"Type", "Name", "Messages"});
			setColumnTypes(new Class[] {String.class, String.class, Integer.class});
		}
		
		@Override
		public Object getColumnValue(JMSDestination entry, int col) {
			switch(col) {
			case 0:
				return entry.getType();
				
			case 1:
				return entry.toString();

			case 2:
				return (TYPE.QUEUE == entry.getType()) ? ((JMSQueue)entry).getMessageCount() : null;
				
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
		
		private final JMSDestinationTable table;
		private final int column;
		
		private FilterColumnListener(JMSDestinationTable table, int column) {
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
							"Show only items matching wildcard (?=one char, *=multiple chars)", 
							currentFilterValue == null ? "" : currentFilterValue);
				if(newFilterValue != null) {
					table.setFilterValue(newFilterValue);
		  		}
	  		}
		}
	}
}
