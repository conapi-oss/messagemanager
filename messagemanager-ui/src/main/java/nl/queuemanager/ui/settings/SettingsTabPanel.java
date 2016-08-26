package nl.queuemanager.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.util.ListTableModel;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class SettingsTabPanel extends JSplitPane implements UITab {

	private SettingsPagesTableModel model;

	@Inject
	public SettingsTabPanel(Map<String, SettingsPanel> settingsPages) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		setLeftComponent(createPagesList(settingsPages));
	}

	private JTable createPagesList(Map<String, SettingsPanel> settingsPages) {
		model = new SettingsPagesTableModel();
		model.setData(new ArrayList<Map.Entry<String, SettingsPanel>>(settingsPages.entrySet()));
		
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;
				int index = table.getSelectedRow();
				Map.Entry<String, SettingsPanel> entry = model.getRowItem(index);
				entry.getValue().readSettings();
				
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
				panel.add(entry.getValue().getUIPanel());
				panel.add(createActionPanel(entry.getValue()));
				setRightComponent(panel);
			}
		});
		table.getSelectionModel().setSelectionInterval(0, 0);
		
		return table;
	}
	
	private JPanel createActionPanel(final SettingsPanel settingsPanel) {
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settingsPanel.saveSettings();
			}
		});
		JButton revertButton = new JButton("Revert");
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				settingsPanel.readSettings();
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(saveButton);
		panel.add(revertButton);
		return panel;
	}

	private class SettingsPagesTableModel extends ListTableModel<Map.Entry<String, SettingsPanel>> {

		public SettingsPagesTableModel() {
			setColumnNames(new String[] {"Category"});
			setColumnTypes(new Class[] {String.class});
		}
		
		@Override
		public Object getColumnValue(Map.Entry<String, SettingsPanel> item, int columnIndex) {
			switch(columnIndex) {
			case 0:
				return item.getKey();
			case 1:
				return item.getValue();
			}
			
			return null;
		}
		
	}

	public String getUITabName() {
		return "Settings";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
				ConnectionState.CONNECTED,
				ConnectionState.DISCONNECTED
		};
	}
	
}
