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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.BrokerDestinations;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.EnumerateMessagesTask.QueueBrowserEvent;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.core.tasks.FireRefreshRequiredTask.JMSDestinationHolder;
import nl.queuemanager.ui.message.MessageViewerPanel;
import nl.queuemanager.ui.message.SearchPanel;
import nl.queuemanager.ui.util.Holder;
import nl.queuemanager.ui.util.QueueCountsRefresher;

import javax.jms.Message;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class QueuesTabPanel extends JSplitPane implements UITab, MessageTableActions {
	private JComboBox<JMSBroker> brokerCombo;
	private QueueTable queueTable;
	private MessagesTable messageTable;
	private MessageViewerPanel messageViewer;

	private final JMSDomain domain;
	private final TaskExecutor worker;
	private final CoreConfiguration config;
	private final QueueBrowserEventListener qbel;
	private final TaskFactory taskFactory;
	private final QueueCountsRefresher qcRefresher;
	private final EventBus eventBus;
	
	@Inject
	public QueuesTabPanel(
			JMSDomain domain,
			TaskExecutor worker,
			CoreConfiguration config,
			QueueTable queueTable,
			JMSDestinationTransferHandlerFactory jmsDestinationTransferHandlerFactory,
			MessageViewerPanel messageViewer,
			TaskFactory taskFactory,
			QueueCountsRefresher refresher,
			MessageHighlighter messageHighlighter,
			EventBus eventBus)
	{
		this.domain = domain;
		this.worker = worker;
		this.config = config;
		this.queueTable = configureQueueTable(queueTable, jmsDestinationTransferHandlerFactory);
		this.taskFactory = taskFactory;
		this.qcRefresher = refresher;
		this.eventBus = eventBus;
		
		this.messageViewer = messageViewer;
		messageViewer.setDragEnabled(true);
		
		messageTable = CommonUITasks.createMessageTable(messageHighlighter, eventBus, domain, this);
		
		// Panel for the connection selector combobox
		JPanel connectionPanel = new JPanel();
		connectionPanel.setLayout(new BoxLayout(connectionPanel, BoxLayout.X_AXIS));
		connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
		
		// Panel for the broker combobox
		JPanel brokerPanel = new JPanel();
		brokerPanel.setLayout(new BoxLayout(brokerPanel, BoxLayout.X_AXIS));
		brokerPanel.setBorder(BorderFactory.createTitledBorder("Broker"));
		brokerCombo = createBrokerCombo();
		brokerPanel.add(brokerCombo, null);

		// Wrap the queues table in a scrollpane
		JScrollPane queuesTableScrollPane = new JScrollPane(queueTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		queuesTableScrollPane.setPreferredSize(new Dimension(350, 100));
		queuesTableScrollPane.setViewportView(queueTable);
		
		// Create a panel for the queues actions
		JPanel queuesActionPanel = createQueuesActionPanel();
		
		// Create the panel for the queues table and refresh panel
		JPanel queuesTablePanel = new JPanel();
		queuesTablePanel.setLayout(new BoxLayout(queuesTablePanel, BoxLayout.Y_AXIS));
		queuesTablePanel.setBorder(BorderFactory.createTitledBorder("Queues"));
		queuesTablePanel.add(brokerCombo);
		queuesTablePanel.add(queuesTableScrollPane);
		queuesTablePanel.add(queuesActionPanel);

		// Wrap the messages table in a JScrollPane
		JScrollPane messageTableScrollPane = new JScrollPane(messageTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		messageTableScrollPane.setPreferredSize(new Dimension(350, 100));
		
		JPanel messagesActionPanel = createMessagesActionPanel();

		JPanel messagesTablePanel = new JPanel();
		// To make the JScrollPane auto resize
		messagesTablePanel.setLayout(new BoxLayout(messagesTablePanel, BoxLayout.Y_AXIS));
		messagesTablePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
		messagesTablePanel.add(messageTableScrollPane);
		messagesTablePanel.add(messagesActionPanel);

		JSplitPane horizontalSplitPane = new JSplitPane();
		horizontalSplitPane.setDividerLocation(350);
		horizontalSplitPane.setResizeWeight(.5D);
		horizontalSplitPane.setContinuousLayout(true);
		horizontalSplitPane.setBorder(null);

		JPanel messageViewerPanel = new JPanel();
		messageViewerPanel.setLayout(new BoxLayout(messageViewerPanel, BoxLayout.Y_AXIS));
		messageViewerPanel.add(messagesTablePanel, null);
		
		horizontalSplitPane.setTopComponent(queuesTablePanel);
		horizontalSplitPane.setBottomComponent(messageViewerPanel);

		// Upper area
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(0, 1));
		topPanel.add(horizontalSplitPane, null);

		// Lower area
		JPanel messageViewPanel = new JPanel();
		messageViewPanel.setLayout(new BoxLayout(messageViewPanel, BoxLayout.Y_AXIS));
		messageViewPanel.setBorder(BorderFactory.createTitledBorder("Message"));
		messageViewPanel.setToolTipText("Select Body and drag/drop the message to another instance of SMM or drag/drop the message into an application.");
		messageViewPanel.add(messageViewer, null);

		setDividerLocation(300);
		setContinuousLayout(true);
		setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(messageViewPanel, JSplitPane.BOTTOM);
		add(topPanel, JSplitPane.TOP);
		
		this.qbel = new QueueBrowserEventListener();
	}

	private JPanel createMessagesActionPanel() {
		// Create a panel for the refresh messages button
		JPanel messagesActionPanel = new JPanel();
		messagesActionPanel.setLayout(new BoxLayout(messagesActionPanel, BoxLayout.X_AXIS));
		messagesActionPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		
		// Refresh button
		JButton refreshButton = createButton("Refresh", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				enumerateMessages();
			}
		});
		CommonUITasks.makeSegmented(refreshButton, Segmented.FIRST);
		messagesActionPanel.add(refreshButton);
		
		// Delete button
		JButton deleteButton = createButton("Delete", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedMessages();
			}
		});
		CommonUITasks.makeSegmented(deleteButton, Segmented.MIDDLE);
		messagesActionPanel.add(deleteButton);

		// Save button
		JButton saveButton = createButton("Save", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveSelectedMessages();
			}
		});
		CommonUITasks.makeSegmented(saveButton, Segmented.LAST);
		messagesActionPanel.add(saveButton);
		messagesActionPanel.add(new SearchPanel(messageTable, eventBus));
		return messagesActionPanel;
	}
	
	/**
	 * Create the panel that contains the actions for the queues table. 
	 * Segmented buttons on Mac OS X for Looks++
	 */
	private JPanel createQueuesActionPanel() {
		JPanel queuesActionPanel = new JPanel();
		queuesActionPanel.setLayout(new BoxLayout(queuesActionPanel, BoxLayout.X_AXIS));
		queuesActionPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		
		// Refresh button
		JButton refreshQueuesButton = createButton("Refresh", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(brokerCombo.getSelectedIndex() >= 0)
					enumerateQueues((JMSBroker)brokerCombo.getSelectedItem());
			}
		});
		queuesActionPanel.add(refreshQueuesButton);
		
		// Clear messages button
		final JButton clearMessagesButton = createButton("Clear Messages", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRows = queueTable.getSelectedRows();
				List<JMSQueue> queueList = CollectionFactory.newArrayList();

				String queueNames = Arrays.stream(selectedRows)
						.mapToObj(row -> queueTable.getRowItem(row).toString())
						.collect(Collectors.joining(", "));

				// show a confirmation dialog
				int option = JOptionPane.showConfirmDialog(QueuesTabPanel.this, "Are you sure you want to clear all messages in the selected queues? \nQueues: " + queueNames, "Clear Messages", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION) {
					for (int row : selectedRows) {
						queueList.add(queueTable.getRowItem(row));
					}
					deleteQueueMessages(queueList);
					messageTable.clear();
				}
			}
		});
		CommonUITasks.makeSegmented(refreshQueuesButton, Segmented.FIRST);
		CommonUITasks.makeSegmented(clearMessagesButton, Segmented.LAST);
		queuesActionPanel.add(clearMessagesButton);

		// Enable/disable the clear messages button when the domain connects
		eventBus.register(new Object() {
			@Subscribe
			public void handleDomainEvent(DomainEvent event) {
				if(event.getId() == DomainEvent.EVENT.JMX_CONNECT) {
					clearMessagesButton.setEnabled(domain.isFeatureSupported(JMSFeature.QUEUE_CLEAR_MESSAGES));
				}
			}
		});
		
		return queuesActionPanel;
	}

	private QueueTable configureQueueTable(QueueTable table, JMSDestinationTransferHandlerFactory transferHandlerfactory) {
		table.setTransferHandler(transferHandlerfactory.create(new InternalDestinationHolder()));
		
		final Holder<Boolean> shouldBrowse = new Holder<Boolean>();
		shouldBrowse.setValue(Boolean.TRUE);
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				
				if(shouldBrowse.getValue())
					enumerateMessages();
			}
		});
		
		// This listener takes care of enableing and disabling the shouldBrowse flag
		// whenever the mouse is over the table while performing a drag operation. This
		// prevents the multiple "Browsing..." messages that interrupt the drag.
		DropTargetListener dtl = new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				shouldBrowse.setValue(Boolean.FALSE);
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				shouldBrowse.setValue(Boolean.TRUE);
			}

			public void drop(DropTargetDropEvent dtde) {
				shouldBrowse.setValue(Boolean.TRUE);
			}
		};
		
		try {
			table.getDropTarget().addDropTargetListener(dtl);
		} catch (TooManyListenersException e1) {
		}
				
		return table;
	}


	private JComboBox<JMSBroker> createBrokerCombo() {
		JComboBox<JMSBroker> cmb = new JComboBox<JMSBroker>();
//		cmb.setMinimumSize(new Dimension(370, 30));
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setAlignmentX(Component.CENTER_ALIGNMENT);
		cmb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getID() != ItemEvent.ITEM_STATE_CHANGED)
					return;
				
				switch(e.getStateChange()) {
				case ItemEvent.DESELECTED: {
					JMSBroker previouslySelectedBroker = (JMSBroker)e.getItem();
					if(previouslySelectedBroker != null)
						qcRefresher.unregisterInterest(previouslySelectedBroker);
				} break;
				
				case ItemEvent.SELECTED: {
						JMSBroker selectedBroker = (JMSBroker)e.getItem();
						
						queueTable.clear();
						messageTable.clear();
						
						qcRefresher.registerInterest(selectedBroker);
						connectToBroker(selectedBroker);
						enumerateQueues(selectedBroker);

						config.setUserPref(CoreConfiguration.PREF_LAST_SELECTED_BROKER, selectedBroker.toString());
				} break;
				}
			}
		});
		cmb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		return cmb;
	}

	private JButton createButton(String caption, ActionListener actionListener) {
		JButton deleteButton = new JButton();
		deleteButton.setText(caption);
		deleteButton.setMinimumSize(new Dimension(80, 30));
		deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		deleteButton.addActionListener(actionListener);
		return deleteButton;
	}
			
	private void populateBrokerCombo(final List<JMSBroker> brokers) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				brokerCombo.removeAllItems();

				if(!brokers.isEmpty()) {
					// copy to avoid concurrent modification exception
					final List<JMSBroker> sortedBrokers = new ArrayList<>(brokers);
					Collections.sort(sortedBrokers);

					// get previously selected broker before adding new brokers which will trigger a selection event
					String previouslySelectedBroker = config.getUserPref(CoreConfiguration.PREF_LAST_SELECTED_BROKER, null);

					for(JMSBroker broker: sortedBrokers) {
						brokerCombo.addItem(broker);
					}

					// Set the previously selected broker
					if(brokerCombo.getItemCount()>0) {
						if(previouslySelectedBroker != null) {
							for(JMSBroker broker: sortedBrokers) {
								if(broker.toString().equals(previouslySelectedBroker)) {
									brokerCombo.setSelectedItem(broker);
									break;
								}
							}
						} else {
							brokerCombo.setSelectedIndex(0);
						}
					}
				}
			}
		});
	}
	
	private void connectToBroker(final JMSBroker broker) {
		// Connect to the broker
		worker.execute(taskFactory.connectToBroker(broker));
	}

	private void enumerateQueues(final JMSBroker broker) {		
		// Get the queue list from the broker
		worker.execute(taskFactory.enumerateQueues(broker, null));
	}

	private void populateQueueTable(final List<JMSQueue> queues) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				queueTable.updateData(queues);
			}
		});
	}
	
	private void enumerateMessages() {		
		JMSQueue queue = queueTable.getSelectedItem();
				
		/* Clear the messages table (set to an empty list to prevent
		 * asynchronous calls to addRow from encountering NPEs). */
		messageTable.setData(null, new ArrayList<Message>());
		
		if(queue != null) {
			enumerateMessages(queue);
		}
	}
	
	private void enumerateMessages(final JMSQueue queue) {
		// Cancel any running browser task and start a new one
		qbel.cancel();
		worker.execute(taskFactory.enumerateMessages(queue, qbel));
	}
		
	public void displaySelectedMessage() {
		if(messageTable.getSelectedRow() == -1) {
			displayMessage(null);
		} else {
			displayMessage(messageTable.getSelectedItem());
		}
	}
	
	private void saveSelectedMessages() {
		List<Message> messages = CollectionFactory.newArrayList();
		
		int[] selectedRows = messageTable.getSelectedRows();
		for(int i: selectedRows) {
			messages.add(messageTable.getRowItem(i));
		}
		
		CommonUITasks.saveMessages(this, messages, worker, taskFactory, config);
	}	
	
	private void displayMessage(final Message message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageViewer.setMessage(message);
			}
		});
	}
	
	private void deleteQueueMessages(final List<JMSQueue> queueList) {
		if(queueList.size() == 0)
			return;

		// Cancel any active queue browsing event
		qbel.cancel();
		
		worker.executeInOrder(
			taskFactory.clearQueues(queueList),
			taskFactory.enumerateQueues((JMSBroker)brokerCombo.getSelectedItem(), null));
	}
	
	public void deleteSelectedMessages() {
		final JMSQueue queue = queueTable.getSelectedItem();
		final List<Message> messages = CollectionFactory.newArrayList();
		ListSelectionModel lsm = messageTable.getSelectionModel();
		
		if(lsm.isSelectionEmpty()) {
			return;
		}
		
		int[] selectedIndexes = messageTable.getSelectedRows();
		final int firstSelectedIndex = selectedIndexes[0];

		// Gather the messages to be removed from the queue.
		for(int i: selectedIndexes) {
			messages.add(messageTable.getRowItem(i));
		}

		// Remove messages from the UI
		for(Message m: messages) {
			messageTable.removeItem(m);
		}

		// Select next message
		int rowCount = messageTable.getRowCount();
		SwingUtilities.invokeLater(() -> {
			// Select next row
			if (rowCount > 0) {
				int selectIndex = firstSelectedIndex;
				if (selectIndex >= rowCount) {
					selectIndex = rowCount - 1;
				}

				messageTable.setRowSelectionInterval(selectIndex,selectIndex);
				messageTable.scrollRectToVisible(messageTable.getCellRect(selectIndex, 0, true));
			}
		});

		// Submit the removal task to the worker
		worker.executeInOrder(
			taskFactory.deleteMessages(queue, messages),
			taskFactory.enumerateQueues((JMSBroker)brokerCombo.getSelectedItem(), null));
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void handleDomainEvent(DomainEvent event) {
		if(event == null) 
			throw new IllegalArgumentException("event must not be null");
		
		switch(event.getId()) {
		case BROKERS_ENUMERATED:
			populateBrokerCombo((List<JMSBroker>)event.getInfo());
			break;
			
		case QUEUES_ENUMERATED:
			final List<JMSQueue> queueList = ((BrokerDestinations) event.getInfo()).getDestinations();
			if(queueList.size() > 0 && queueList.get(0).getBroker().equals(brokerCombo.getSelectedItem())) {
				populateQueueTable(queueList);
			}
			break;
			
		case BROKER_DISCONNECT:
			Object info = event.getInfo();
			if(info != null && info.equals(brokerCombo.getSelectedItem())) {
				CommonUITasks.clear(messageTable);
				CommonUITasks.clear(queueTable);
				populateBrokerCombo(new ArrayList<JMSBroker>());
			}
			break;
		}
	}
	
	public String getUITabName() {
		return "Queue Browser";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {ConnectionState.CONNECTED};
	}

	private class InternalDestinationHolder implements JMSDestinationHolder {
		public JMSDestination getJMSDestination() {
			return queueTable.getSelectedItem();
		}
		
		public List<JMSDestination> getJMSDestinationList() {
			final List<JMSDestination> list = CollectionFactory.newArrayList();
			
			int[] selectedRows = queueTable.getSelectedRows();
			for(int selectedIndex: selectedRows) {
				list.add(queueTable.getRowItem(selectedIndex));
			}
			
			return list;
		}

		public void refreshRequired(JMSDestination destination) {
			if(!destination.equals(queueTable.getSelectedItem())) {
				queueTable.setSelectedItem((JMSQueue)destination);
			} else {
				enumerateMessages((JMSQueue)destination);
			}
		}
	}
		
	private class QueueBrowserEventListener implements EventListener<QueueBrowserEvent> {
		private volatile boolean canceled;
		
		public void processEvent(final QueueBrowserEvent event) {			
			switch(event.getId()) {
			case BROWSING_STARTED:
				canceled = false;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						messageTable.clear((JMSDestination)event.getInfo());
					}
				});
				break;
				
			case MESSAGE_FOUND:
				/* If the task has been canceled (the user selected another queue
				 * while this one was not done browsing yet), cancel the browsing
				 * task. */
				if(canceled) {
					canceled = false;
					CancelableTask task = ((CancelableTask)event.getSource());
					task.cancel();
				} else {				
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							messageTable.addItem((Message)event.getInfo());
						}
					});
				}
				break;
				
			case BROWSING_COMPLETE:
				canceled = false;
				break;
			}
		}
		
		public void cancel() {
			canceled = true;
		}
	}
}
