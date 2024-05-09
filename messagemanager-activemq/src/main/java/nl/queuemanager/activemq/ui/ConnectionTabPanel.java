package nl.queuemanager.activemq.ui;

import com.google.common.eventbus.EventBus;
import nl.queuemanager.activemq.ActiveMQConfiguration;
import nl.queuemanager.activemq.ActiveMQConnectionDescriptor;
import nl.queuemanager.activemq.ActiveMQDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.util.ListTableModel;

import jakarta.inject.Inject;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConnectionTabPanel extends JPanel implements UITab {

	private JTextField jmxServiceURLField;
	private JTextField descriptionField;
	private JTable remoteProcessTable;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private JRadioButton remoteProcess;
	private JButton connectButton;

	private final ActiveMQDomain domain;
	private final TaskExecutor worker;
	private final EventBus eventBus;
	private final ActiveMQConfiguration config;
	
	/**
	 * Create the panel.
	 */
	@Inject
	public ConnectionTabPanel(ActiveMQDomain domain, TaskExecutor worker, EventBus eventBus, ActiveMQConfiguration myconfig) {
		this.domain = domain;
		this.worker = worker;
		this.eventBus = eventBus;
		this.config = myconfig;
		
		setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Connect to ActiveMQ Broker", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{136, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);
		
		ActionListener radioButtonChangedAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				jmxServiceURLField.setEnabled(remoteProcess.isSelected());
				descriptionField.setEnabled(remoteProcess.isSelected());
				remoteProcessTable.setEnabled(remoteProcess.isSelected());
			}
		};

		
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
		descriptionField.setText("localhost");
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
		jmxServiceURLField.setText("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
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
		
		// Add contents to the table above
		final ListTableModel<ActiveMQConnectionDescriptor> remoteProcessModel = new ListTableModel<ActiveMQConnectionDescriptor>() {
			{ // "constructor" for the anonymous inner class
				setColumnNames(new String[] {"Description", "JMX Url"});
				setColumnTypes(new Class[] {String.class, String.class});
			}
			
			@Override
			public Object getColumnValue(ActiveMQConnectionDescriptor item, int columnIndex) {
				switch(columnIndex) {
				case 0: return item.getDescription();
				case 1: return item.getJmxUrl();
				default: return null;
				}
			}
		};
		remoteProcessModel.setData(config.listConnectionDescriptors());
		remoteProcessTable.setModel(remoteProcessModel);
		TableColumnAdjuster adjuster = new TableColumnAdjuster(remoteProcessTable, 15);
		adjuster.adjustColumns();

		remoteProcessTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) { return; }
				
				int row = remoteProcessTable.getSelectedRow();
				if(row == -1) return;
				ActiveMQConnectionDescriptor item = remoteProcessModel.getRowItem(row);
				if(item == null) return;
				descriptionField.setText(item.getDescription());
				jmxServiceURLField.setText(item.getJmxUrl());
			}
		});
		remoteProcessTable.addMouseListener(new MouseAdapter() {
			@Override
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {
		            int row = remoteProcessTable.rowAtPoint(e.getPoint());
		            if(row == -1) { return; }
		            
		            ActiveMQConnectionDescriptor item = remoteProcessModel.getRowItem(row); 
		            if(item != null) {
		            	connectButton.doClick();
		            }
		         }
		    }
		});
		
		JButton removeConnectionButton = new JButton("Remove Connection");
		removeConnectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = remoteProcessTable.getSelectedRow();
				if(row >= 0) {
					ActiveMQConnectionDescriptor item = remoteProcessModel.getRowItem(row);
					config.deleteConnectionDescriptor(item.getKey());
					remoteProcessModel.setData(config.listConnectionDescriptors());
				}
			}
		});
		GridBagConstraints gbc_removeConnectionButton = new GridBagConstraints();
		gbc_removeConnectionButton.anchor = GridBagConstraints.WEST;
		gbc_removeConnectionButton.gridx = 0;
		gbc_removeConnectionButton.gridy = 7;
		add(removeConnectionButton, gbc_removeConnectionButton);

		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = remoteProcessTable.getSelectedRow();
				if(row > -1) {
					ActiveMQConnectionDescriptor item = remoteProcessModel.getRowItem(row);
					if(item != null) {
						if(item.getDescription().equals(descriptionField.getText())
						&& item.getJmxUrl().equals(jmxServiceURLField.getText())) {
							// Connecting to existing item
							connect();
							return;
						}
					}
				}
				
				// Connecting to new item
				ActiveMQConnectionDescriptor cd = new ActiveMQConnectionDescriptor(descriptionField.getText(), jmxServiceURLField.getText());
				config.saveConnectionDescriptor(cd);
				connect();
			}
		});
		GridBagConstraints gbc_connectButton = new GridBagConstraints();
		gbc_connectButton.anchor = GridBagConstraints.EAST;
		gbc_connectButton.insets = new Insets(0, 0, 0, 5);
		gbc_connectButton.gridx = 1;
		gbc_connectButton.gridy = 7;
		add(connectButton, gbc_connectButton);
		

		radioButtonChangedAction.actionPerformed(null);
	}
	
	private void connect() {

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
