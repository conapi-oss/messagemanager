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


	/**
	 * Create the panel.
	 */


public class ConnectionTabPanel extends JPanel implements UITab {

		private JTextField jmxServiceURLField;
		private JTextField descriptionField;
		private JTable remoteProcessTable;
		private JButton connectButton;

		private final ActiveMQDomain domain;
		private final TaskExecutor worker;
		private final EventBus eventBus;
		private final ActiveMQConfiguration config;

		@Inject
		public ConnectionTabPanel(ActiveMQDomain domain, TaskExecutor worker, EventBus eventBus, ActiveMQConfiguration myconfig) {
			this.domain = domain;
			this.worker = worker;
			this.eventBus = eventBus;
			this.config = myconfig;

			setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
					new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
							"Connect to ActiveMQ Broker", TitledBorder.LEADING, TitledBorder.TOP, null,
							UIManager.getColor("label.foreground"))));

			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(5, 5, 5, 5);
			gbc.fill = GridBagConstraints.HORIZONTAL;

			// Description
			gbc.gridx = 0;
			gbc.gridy = 0;
			add(new JLabel("Description"), gbc);

			gbc.gridx = 1;
			gbc.weightx = 1.0;
			descriptionField = new JTextField("localhost");
			add(descriptionField, gbc);

			// JMX Service URL
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 0.0;
			add(new JLabel("JMX Service URL"), gbc);

			gbc.gridx = 1;
			gbc.weightx = 1.0;
			jmxServiceURLField = new JTextField("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
			add(jmxServiceURLField, gbc);

			// Remote Process Table
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.BOTH;
			remoteProcessTable = new JTable();
			JScrollPane scrollPane = new JScrollPane(remoteProcessTable);
			add(scrollPane, gbc);

			// Buttons
			gbc.gridy = 3;
			gbc.weighty = 0.0;
			gbc.fill = GridBagConstraints.NONE;
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

			JButton removeConnectionButton = new JButton("Remove Connection");
			buttonPanel.add(removeConnectionButton);

			connectButton = new JButton("Connect");
			buttonPanel.add(connectButton);

			add(buttonPanel, gbc);

			initializeTable();
			initializeListeners(removeConnectionButton);
		}

		private void initializeTable() {
			final ListTableModel<ActiveMQConnectionDescriptor> remoteProcessModel = new ListTableModel<ActiveMQConnectionDescriptor>() {
				{
					setColumnNames(new String[]{"Description", "JMX Url"});
					setColumnTypes(new Class[]{String.class, String.class});
				}

				@Override
				public Object getColumnValue(ActiveMQConnectionDescriptor item, int columnIndex) {
					switch (columnIndex) {
						case 0:
							return item.getDescription();
						case 1:
							return item.getJmxUrl();
						default:
							return null;
					}
				}
			};
			remoteProcessModel.setData(config.listConnectionDescriptors());
			remoteProcessTable.setModel(remoteProcessModel);
			TableColumnAdjuster adjuster = new TableColumnAdjuster(remoteProcessTable, 15);
			adjuster.adjustColumns();
		}

		private void initializeListeners(JButton removeConnectionButton) {
			remoteProcessTable.getSelectionModel().addListSelectionListener(e -> {
				if (e.getValueIsAdjusting()) return;
				int row = remoteProcessTable.getSelectedRow();
				if (row == -1) return;
				ActiveMQConnectionDescriptor item = ((ListTableModel<ActiveMQConnectionDescriptor>) remoteProcessTable.getModel()).getRowItem(row);
				if (item == null) return;
				descriptionField.setText(item.getDescription());
				jmxServiceURLField.setText(item.getJmxUrl());
			});

			remoteProcessTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						int row = remoteProcessTable.rowAtPoint(e.getPoint());
						if (row == -1) return;
						ActiveMQConnectionDescriptor item = ((ListTableModel<ActiveMQConnectionDescriptor>) remoteProcessTable.getModel()).getRowItem(row);
						if (item != null) {
							connectButton.doClick();
						}
					}
				}
			});

			removeConnectionButton.addActionListener(e -> {
				int row = remoteProcessTable.getSelectedRow();
				if (row >= 0) {
					ActiveMQConnectionDescriptor item = ((ListTableModel<ActiveMQConnectionDescriptor>) remoteProcessTable.getModel()).getRowItem(row);
					config.deleteConnectionDescriptor(item.getKey());
					((ListTableModel<ActiveMQConnectionDescriptor>) remoteProcessTable.getModel()).setData(config.listConnectionDescriptors());
				}
			});

			connectButton.addActionListener(e -> connect());
		}

		private void connect() {
			int row = remoteProcessTable.getSelectedRow();
			if (row > -1) {
				ActiveMQConnectionDescriptor item = ((ListTableModel<ActiveMQConnectionDescriptor>) remoteProcessTable.getModel()).getRowItem(row);
				if (item != null) {
					if (item.getDescription().equals(descriptionField.getText())
							&& item.getJmxUrl().equals(jmxServiceURLField.getText())) {
						// Connecting to existing item
						connectToJMX(jmxServiceURLField.getText());
						return;
					}
				}
			}

			// Connecting to new item
			ActiveMQConnectionDescriptor cd = new ActiveMQConnectionDescriptor(descriptionField.getText(), jmxServiceURLField.getText());
			config.saveConnectionDescriptor(cd);
			connectToJMX(jmxServiceURLField.getText());
		}

		private void connectToJMX(String url) {
			if (url.length() > 0) {
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
