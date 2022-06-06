package nl.queuemanager.app;

import nl.queuemanager.ui.util.ListTableModel;

import java.util.ArrayList;

@SuppressWarnings("serial")
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
