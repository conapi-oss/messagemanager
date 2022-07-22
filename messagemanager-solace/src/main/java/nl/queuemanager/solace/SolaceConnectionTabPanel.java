package nl.queuemanager.solace;

import nl.queuemanager.core.configuration.Configuration;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.util.ListTableModel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("serial")
class SolaceConnectionTabPanel extends SolaceConnectionTabPanelUI {
	
	private final SolaceConfiguration config;
	private final SempConnectionDescriptorPanel sempConnectionDescriptorPanel;
	private final TaskExecutor worker;
	private final TaskFactory taskFactory;
	
	@Inject
	public SolaceConnectionTabPanel(final TaskExecutor worker, SempConnectionDescriptorPanel solaceConnectionDescriptorPanel, 
			SolaceConfiguration config, TaskFactory taskFactory) {
		super(solaceConnectionDescriptorPanel);
		this.worker = worker;
		this.sempConnectionDescriptorPanel = solaceConnectionDescriptorPanel;

		this.config = config;
		this.taskFactory = taskFactory;
		
		getTable().setModel(new ConnectionTableModel(config.getConnectionsConfigSection()));
		getTable().getSelectionModel().addListSelectionListener(tableSelectionListener);
		getBtnAddConnection().setAction(addConnectionAction);
		getBtnRemoveConnection().setAction(removeConnectionAction);
		
		getSempConnectionDescriptorPanel().connectButton.setAction(connectAction);
		
		// Initially display the first element in the list (if any)
		if(getTable().getRowCount() > 0) {
			getTable().getSelectionModel().setSelectionInterval(0, 0);
		}
		tableSelectionListener.valueChanged(null);
	}
	
	private void displayFields(SempConnectionDescriptor descriptor) {
		// If we are displaying something, update current UI values into the
		// descriptor to make sure we won't lose anything the user did.
		if(getSempConnectionDescriptorPanel().getDescriptor() != null) {
			getSempConnectionDescriptorPanel().updateItem(getSempConnectionDescriptorPanel().getDescriptor());
		}
		try {
			getSempConnectionDescriptorPanel().displayItem(descriptor);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private final ListSelectionListener tableSelectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedRow = getTable().getSelectedRow();
			if(selectedRow >= 0) {
				displayFields(getTableModel().getRowItem(selectedRow));
			} else {
				displayFields(null);
			}
		}
	};
	
	private ConnectionTableModel getTableModel() {
		return (ConnectionTableModel)getTable().getModel();
	}
	
	private final Action connectAction = new AbstractAction() {
		{
			putValue(NAME, "Connect to appliance");
			putValue(SHORT_DESCRIPTION, "Connect to appliance");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int selectedRow = getTable().getSelectedRow();
			if(selectedRow >= 0) {
				SempConnectionDescriptor descriptor = getTableModel().getRowItem(selectedRow);
				sempConnectionDescriptorPanel.updateItem(descriptor);
				descriptor.saveTo(config.getConnectionsConfigSection().sub(descriptor.getKey()));
				worker.execute(taskFactory.connectToAppliance(descriptor));
			}
		}
	};
	
	private final Action addConnectionAction = new AbstractAction() {
		{
			putValue(SMALL_ICON, new ImageIcon(SolaceConnectionTabPanelUI.class.getResource("/icons/16x16/Create.png")));
			putValue(NAME, "Add connection");
			putValue(SHORT_DESCRIPTION, "Add a new connection to a Solace appliance");
		}
		
		public void actionPerformed(ActionEvent e) {
			SempConnectionDescriptor d = new SempConnectionDescriptor();
			// Always start connection ids with a 'c' to ensure they are valid xml element names
			d.setKey("c" + UUID.randomUUID().toString());
			d.setDescription("New connection");
			getTableModel().addRow(d);
		}
	};
	
	private final Action removeConnectionAction = new AbstractAction() {
		{
			putValue(SMALL_ICON, new ImageIcon(SolaceConnectionTabPanelUI.class.getResource("/icons/16x16/Delete.png")));
			putValue(NAME, "Remove connection");
			putValue(SHORT_DESCRIPTION, "Remove an existing connection");
		}
		
		public void actionPerformed(ActionEvent e) {
			int selectedRow = getTable().getSelectedRow();
			if(selectedRow >= 0) {
				SempConnectionDescriptor d = getTableModel().getRowItem(selectedRow);
				getTableModel().removeRow(d);
			}
		}
	};
	
	private static class ConnectionTableModel extends ListTableModel<SempConnectionDescriptor> {
		final Configuration connectionsSection;
		
		private ConnectionTableModel(Configuration section) {
			setColumnNames(new String[] {"Description", "URI"});
			setColumnTypes(new Class[] {String.class, URI.class});
			
			this.connectionsSection = section;
			List<SempConnectionDescriptor> connectionDescriptors = new ArrayList<SempConnectionDescriptor>();
			for(String key: connectionsSection.listKeys()) {
				connectionDescriptors.add(new SempConnectionDescriptor().loadFrom(connectionsSection.sub(key)));
			}
			setData(connectionDescriptors);
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public Object getColumnValue(SempConnectionDescriptor d, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return d.getDescription();
				
			case 1:
				return d.getDisplayName();
			}
			
			return "";
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			SempConnectionDescriptor d = getRowItem(rowIndex);
			switch(columnIndex) {
			case 0:
				d.setDescription((String)aValue);
				d.saveTo(connectionsSection.sub(d.getKey()));
			}
		}

		public void addRow(SempConnectionDescriptor conn) {
			super.addRow(conn);
			conn.saveTo(connectionsSection.sub(conn.getKey()));
		}

		public void removeRow(SempConnectionDescriptor conn) {
			connectionsSection.del(conn.getKey());
			super.removeRow(conn);
		}
	}

}
