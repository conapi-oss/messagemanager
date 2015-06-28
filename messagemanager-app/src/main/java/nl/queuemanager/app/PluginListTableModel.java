package nl.queuemanager.app;

import java.util.ArrayList;

import nl.queuemanager.ui.util.ListTableModel;

public class PluginListTableModel extends ListTableModel<PluginDescriptor> {

	public PluginListTableModel() {
		setColumnNames(new String[] {"Name", "Description"});
		setColumnTypes(new Class[] {String.class, String.class});
		setData(new ArrayList<PluginDescriptor>());
	}

	@Override
	public Object getColumnValue(PluginDescriptor plugin, int columnIndex) {
		switch(columnIndex) {
		case 0:
			return plugin.getName();
		case 1:
			return plugin.getDescription();
		}
		
		return null;
	}
	
}
