package nl.queuemanager.activemq.ui;

import java.awt.GridBagLayout;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import nl.queuemanager.activemq.ActiveMQDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.UITab;

import java.awt.Color;

import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

import com.google.common.eventbus.EventBus;

public class ConnectionTabPanel extends JPanel implements UITab {
	private JTable localProcessTable;
	private JTextField jmxServiceURLField;
	private JTextField descriptionField;
	private JTable remoteProcessTable;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton localProcess;
	private JRadioButton remoteProcess;
	private JButton connectButton;

	private final ActiveMQDomain domain;
	private final TaskExecutor worker;
	private final EventBus eventBus;
	
	/**
	 * Create the panel.
	 */
	@Inject
	public ConnectionTabPanel(JavaProcessFinder processFinder, ActiveMQDomain domain, TaskExecutor worker, EventBus eventBus) {
		this.domain = domain;
		this.worker = worker;
		this.eventBus = eventBus;
		
		System.out.println(domain + " was loaded by " + domain.getClass().getClassLoader());
		
		setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Connect to ActiveMQ Broker", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{136, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);
		
		ActionListener radioButtonChangedAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				localProcessTable.setEnabled(localProcess.isSelected());
				
				jmxServiceURLField.setEnabled(remoteProcess.isSelected());
				descriptionField.setEnabled(remoteProcess.isSelected());
				remoteProcessTable.setEnabled(remoteProcess.isSelected());
			}
		};
		
		localProcess = new JRadioButton("Local Process");
		localProcess.addActionListener(radioButtonChangedAction);
		buttonGroup.add(localProcess);
		GridBagConstraints gbc_localProcess = new GridBagConstraints();
		gbc_localProcess.gridwidth = 2;
		gbc_localProcess.anchor = GridBagConstraints.WEST;
		gbc_localProcess.insets = new Insets(0, 0, 5, 0);
		gbc_localProcess.gridx = 0;
		gbc_localProcess.gridy = 0;
		add(localProcess, gbc_localProcess);
		
		localProcessTable = new JTable();
		localProcessTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridwidth = 2;
		gbc_table.insets = new Insets(0, 25, 5, 0);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 1;
		add(new JScrollPane(localProcessTable), gbc_table);

		DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return false;
			}
		};
		model.setColumnIdentifiers(new String[] {"PID", "Description"});
		for(JavaProcessDescriptor javaProcess: processFinder.find()) {
			model.addRow(new Object[] {
					javaProcess.id(),
					javaProcess.displayName()
			});
		}
		localProcessTable.setModel(model);
		TableColumnAdjuster adjuster = new TableColumnAdjuster(localProcessTable, 15);
		adjuster.adjustColumns();
		
		remoteProcess = new JRadioButton("Remote Process");
		remoteProcess.addActionListener(radioButtonChangedAction);
		buttonGroup.add(remoteProcess);
		GridBagConstraints gbc_remoteProcess = new GridBagConstraints();
		gbc_remoteProcess.gridwidth = 2;
		gbc_remoteProcess.anchor = GridBagConstraints.WEST;
		gbc_remoteProcess.insets = new Insets(0, 0, 5, 0);
		gbc_remoteProcess.gridx = 0;
		gbc_remoteProcess.gridy = 3;
		add(remoteProcess, gbc_remoteProcess);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 4;
		add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionField = new JTextField();
		descriptionField.setColumns(10);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 4;
		add(descriptionField, gbc_textField_1);
				
		JLabel lblNewLabel = new JLabel("JMX Service URL");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 5;
		add(lblNewLabel, gbc_lblNewLabel);
		
		jmxServiceURLField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 5;
		add(jmxServiceURLField, gbc_textField);
		jmxServiceURLField.setColumns(10);
		
		remoteProcessTable = new JTable();
		GridBagConstraints gbc_table_1 = new GridBagConstraints();
		gbc_table_1.gridwidth = 2;
		gbc_table_1.insets = new Insets(0, 25, 5, 0);
		gbc_table_1.fill = GridBagConstraints.BOTH;
		gbc_table_1.gridx = 0;
		gbc_table_1.gridy = 6;
		add(new JScrollPane(remoteProcessTable), gbc_table_1);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connect();
			}
		});
		GridBagConstraints gbc_connectButton = new GridBagConstraints();
		gbc_connectButton.anchor = GridBagConstraints.WEST;
		gbc_connectButton.insets = new Insets(0, 0, 0, 5);
		gbc_connectButton.gridx = 0;
		gbc_connectButton.gridy = 7;
		add(connectButton, gbc_connectButton);
		
		JButton removeConnectionButton = new JButton("Remove Connection");
		GridBagConstraints gbc_removeConnectionButton = new GridBagConstraints();
		gbc_removeConnectionButton.anchor = GridBagConstraints.EAST;
		gbc_removeConnectionButton.gridx = 1;
		gbc_removeConnectionButton.gridy = 7;
		add(removeConnectionButton, gbc_removeConnectionButton);

		localProcess.setSelected(true);
		radioButtonChangedAction.actionPerformed(null);
	}
	
	private void connect() {
		if(localProcess.isSelected()) {
			int row = localProcessTable.getSelectedRow();
			if(row != -1) {
				final String pid = (String)localProcessTable.getModel().getValueAt(row, 0);
				worker.execute(new Task(domain, eventBus) {
					@Override
					public void execute() throws Exception {
						@SuppressWarnings("restriction")
						final String url = sun.management.ConnectorAddressLink.importFrom(Integer.valueOf(pid));
						domain.connect(url);
					}
					@Override
					public String toString() {
						return "Connecting to ActiveMQ on PID " + pid;
					}
				});
			}
		} else
		if(remoteProcess.isSelected()) {
			final String url = jmxServiceURLField.getText();
			if(url.length() > 0) {
				worker.execute(new Task(domain, eventBus) {
					@Override
					public void execute() throws Exception {
						domain.connect(url);
					}
					@Override
					public String toString() {
						return "Connecting to ActiveMQ on " + url;
					}
				});
			}
		}
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
}
