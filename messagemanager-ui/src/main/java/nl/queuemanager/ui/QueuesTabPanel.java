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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import javax.jms.Message;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.EnumerateMessagesTask;
import nl.queuemanager.core.tasks.EnumerateMessagesTask.QueueBrowserEvent;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.JMSDestinationTransferHandler.JMSDestinationHolder;
import nl.queuemanager.ui.message.MessageViewerPanel;
import nl.queuemanager.ui.util.Holder;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class QueuesTabPanel extends JSplitPane {
	private JComboBox brokerCombo;
	private QueueTable queueTable;
	private MessagesTable messageTable;
	private MessageViewerPanel messageViewer;

	private final JMSDomain domain;
	private final TaskExecutor worker;
	private final Configuration config;
	private final QueueBrowserEventListener qbel;
	private       Timer autoRefreshTimer;
	private final TaskFactory taskFactory;
	
	@Inject
	public QueuesTabPanel(
			JMSDomain domain, 
			TaskExecutor worker,
			Configuration config,
			JMSDestinationTransferHandlerFactory jmsDestinationTransferHandlerFactory,
			MessageViewerPanel messageViewer,
			TaskFactory taskFactory)
	{
		this.domain = domain;
		this.worker = worker;
		this.config = config;
		this.queueTable = createQueueTable(jmsDestinationTransferHandlerFactory);
		this.taskFactory = taskFactory;
		
		this.messageViewer = messageViewer;
		messageViewer.setDragEnabled(true);
		
		messageTable = createMessageTable();
		
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
				
		domain.addListener(new DomainEventListener());
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
		CommonUITasks.makeSegmented(refreshQueuesButton, Segmented.FIRST);
		queuesActionPanel.add(refreshQueuesButton);
		
		// Clear messages button
		JButton clearMessagesButton = createButton("Clear messages", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedRows = queueTable.getSelectedRows();
				List<JMSQueue> queueList = CollectionFactory.newArrayList();
				
				for(int row: selectedRows) {
					queueList.add(queueTable.getRowItem(row));
				}
				
				deleteQueueMessages(queueList);
				messageTable.clear();
			}
		});
		CommonUITasks.makeSegmented(clearMessagesButton, Segmented.LAST);
		queuesActionPanel.add(clearMessagesButton);
				
		return queuesActionPanel;
	}

	private QueueTable createQueueTable(JMSDestinationTransferHandlerFactory transferHandlerfactory) {
		final QueueTable table = new QueueTable();
		
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

	private MessagesTable createMessageTable() {
		// Create the message table
		MessagesTable table = new MessagesTable();

		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				
				displaySelectedMessage();
			}
		});
		
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_DELETE) {
					deleteSelectedMessages();
				} else {
					super.keyTyped(e);
				}
			}
		});
		
		table.setDragEnabled(true);
		
		return table;
	}
	
	private JComboBox createBrokerCombo() {
		JComboBox cmb = new JComboBox();
//		cmb.setMinimumSize(new Dimension(370, 30));
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setAlignmentX(Component.CENTER_ALIGNMENT);
		cmb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getID() == ItemEvent.ITEM_STATE_CHANGED
				&& e.getStateChange() == ItemEvent.SELECTED) {
					JMSBroker selectedBroker = (JMSBroker)e.getItem();
					
					queueTable.clear();
					messageTable.clear();
					
					connectToBroker(selectedBroker);
					enumerateQueues(selectedBroker);
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
				
				Collections.sort(brokers);
				
				for(JMSBroker broker: brokers) {
					brokerCombo.addItem(broker);
				}
				
				if(brokerCombo.getItemCount()>0) {
					brokerCombo.setSelectedIndex(0);
				}
			}
		});
	}
	
	private void connectToBroker(final JMSBroker broker) {
		// Connect to the broker
		worker.execute(taskFactory.connectToBroker(broker));
	}

	public void initAutorefreshTimer() {
		if(autoRefreshTimer != null) {
			autoRefreshTimer.cancel();
			autoRefreshTimer = null;
		}
		
		int autoRefreshInterval = Integer.parseInt(config.getUserPref(
				Configuration.PREF_AUTOREFRESH_INTERVAL, "5000"));
		
		this.autoRefreshTimer = new Timer();
		autoRefreshTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				enumerateQueues((JMSBroker)brokerCombo.getSelectedItem());
			}
		}, autoRefreshInterval, autoRefreshInterval);
	}
	
	public void stopAutoRefreshTimer() {
		if(autoRefreshTimer != null) {
			autoRefreshTimer.cancel();
		}
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
		worker.execute(new EnumerateMessagesTask(queue, domain, qbel));
	}
		
	private void displaySelectedMessage() {
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
		
		CommonUITasks.saveMessages(this, messages, worker, config);
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
		
		worker.executeInOrder(new Task(queueList.get(0).getBroker()) {
			@Override
			public void execute() throws Exception {
				domain.deleteMessages(queueList);
			}
			@Override
			public String toString() {
				if(queueList.size()==1)
					return "Deleting all messages from " + queueList.get(0);
				else
					return "Deleting all messages from " + queueList.size() + " queue(s)";
			}
		},
		taskFactory.enumerateQueues((JMSBroker)brokerCombo.getSelectedItem(), null));
	}
	
	private void deleteSelectedMessages() {
		final JMSQueue queue = queueTable.getSelectedItem();
		final List<Message> messages = CollectionFactory.newArrayList();
		ListSelectionModel lsm = messageTable.getSelectionModel();
		
		if(lsm.isSelectionEmpty()) {
			return;
		}
		
		int[] selectedIndexes = messageTable.getSelectedRows();
		
		// Gather the messages to be removed from the queue.
		for(int i: selectedIndexes) {
			messages.add(messageTable.getRowItem(i));				
		}
		
		// Remove messages from the UI
		for(Message m: messages) {
			messageTable.removeItem(m);
		}

		// Submit the removal task to the worker
		worker.executeInOrder(new Task(queue.getBroker()) {
			@Override
			public void execute() throws Exception {
				domain.deleteMessages(queue, messages);
			}
			@Override
			public String toString() {
				return 
					"Deleting " + messages.size() + " messages from " + queue;
			}
		},
		taskFactory.enumerateQueues((JMSBroker)brokerCombo.getSelectedItem(), null));
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
	
	private class DomainEventListener implements EventListener<DomainEvent> {
		@SuppressWarnings("unchecked")
		public void processEvent(DomainEvent event) {
			if(event == null) 
				throw new IllegalArgumentException("event must not be null");
			
			switch(event.getId()) {
			case BROKER_CONNECT:
				if(event.getInfo().equals(brokerCombo.getSelectedItem())) {
					initAutorefreshTimer();
				}
				break;
				
			case BROKERS_ENUMERATED:
				populateBrokerCombo((List<JMSBroker>)event.getInfo());
				break;
				
			case QUEUES_ENUMERATED:
				final List<JMSQueue> queueList = (List<JMSQueue>)event.getInfo();
				if(queueList.size() > 0 && queueList.get(0).getBroker().equals(brokerCombo.getSelectedItem()))
					populateQueueTable(queueList);
				break;
				
			case BROKER_DISCONNECT:
				Object info = event.getInfo();
				if(info != null && info.equals(brokerCombo.getSelectedItem())) {
					stopAutoRefreshTimer();
					CommonUITasks.clear(messageTable);
					CommonUITasks.clear(queueTable);
					populateBrokerCombo(new ArrayList<JMSBroker>());
				}
				break;
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
						messageTable.setData((JMSDestination)event.getInfo(), new ArrayList<Message>());
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
