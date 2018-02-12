package nl.queuemanager.solace;

import java.awt.BorderLayout;
import java.awt.Rectangle;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import nl.queuemanager.ui.UITab;

@SuppressWarnings("serial")
class SolaceConnectionTabPanelUI extends JSplitPane implements UITab {
	private final SempConnectionDescriptorPanel sempConnectionDescriptorPanel;
	
	private final JPanel leftPane = new JPanel();
	private final JTable table;

	private final JButton btnAddConnection;
	private final JButton btnRemoveConnection;
	
	@Inject
	public SolaceConnectionTabPanelUI(final SempConnectionDescriptorPanel sempConnectionDescriptorPanel) {
		this.sempConnectionDescriptorPanel = sempConnectionDescriptorPanel;
		setRequestFocusEnabled(false);
		setContinuousLayout(true);
		setBounds(new Rectangle(0, 0, 400, 500));
		
		JScrollPane scrollPane = new JScrollPane();
		leftPane.setBorder(new EmptyBorder(22, 5, 5, 5));
		setLeftComponent(leftPane);
		leftPane.setLayout(new BorderLayout(0, 0));
		leftPane.add(scrollPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		scrollPane.setViewportView(table);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		leftPane.add(toolBar, BorderLayout.SOUTH);
		
		btnAddConnection = new JButton("+");
		getBtnAddConnection().setHideActionText(true);
		toolBar.add(getBtnAddConnection());
		
		btnRemoveConnection = new JButton("-");
		getBtnRemoveConnection().setHideActionText(true);
		toolBar.add(getBtnRemoveConnection());
		setRightComponent(sempConnectionDescriptorPanel);
	}
		
	@Override
	public String getUITabName() {
		return "Connect";
	}

	@Override
	public JComponent getUITabComponent() {
		return this;
	}

	@Override
	public ConnectionState[] getUITabEnabledStates() {
		return ConnectionState.values();
	}
	
	protected JTable getTable() {
		return table;
	}

	public SempConnectionDescriptorPanel getSempConnectionDescriptorPanel() {
		return sempConnectionDescriptorPanel;
	}

	public JButton getBtnAddConnection() {
		return btnAddConnection;
	}

	public JButton getBtnRemoveConnection() {
		return btnRemoveConnection;
	}
	
	
}
