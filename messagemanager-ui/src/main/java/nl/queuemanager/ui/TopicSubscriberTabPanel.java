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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.Message;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.core.MessageEvent;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSTopic;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.UITab.ConnectionState;
import nl.queuemanager.ui.message.MessageViewerPanel;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This class implements the topic subscriber panel. It has a table of configured topics,
 * a message list for the currently selected topic and a message viewer panel to display
 * messages.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class TopicSubscriberTabPanel extends JSplitPane implements UITab {
	private JComboBox brokerCombo;
	private final JMSDomain domain;
	private final TaskExecutor worker;
	private final Configuration config;
	
	private final TopicSubscriberTable subscriberTable;
	private final MessagesTable messageTable;
	private final MessageViewerPanel messageViewer;
	
	private final MessageEventListener messageEventListener;
	
	private final Injector injector;
	
	@Inject
	public TopicSubscriberTabPanel(Injector injector) {
		// FIXME This is against DI best practices
		this.injector = injector;
		this.domain = injector.getInstance(JMSDomain.class);
		this.worker = injector.getInstance(TaskExecutor.class);
		this.config = injector.getInstance(Configuration.class);
		
		subscriberTable = createTopicTable(injector);
		messageTable = createMessageTable();
		messageViewer = injector.getInstance(MessageViewerPanel.class);
		messageViewer.setDragEnabled(true);
		
		brokerCombo = createBrokerCombo();
		
		//Topic Panel
		JScrollPane topicTableScrollPane = new JScrollPane(subscriberTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		topicTableScrollPane.setPreferredSize(new Dimension(350, 100));
		topicTableScrollPane.setViewportView(subscriberTable);
		
		JPanel topicActionPanel = createTopicActionPanel();
		
		JPanel topicTablePanel = new JPanel();
		topicTablePanel.setLayout(new BoxLayout(topicTablePanel, BoxLayout.Y_AXIS));
		topicTablePanel.setBorder(BorderFactory.createTitledBorder("Topic subscribers"));
		topicTablePanel.add(brokerCombo);
		topicTablePanel.add(topicTableScrollPane);
		topicTablePanel.add(topicActionPanel);
		
		JScrollPane messageTableScrollPane = new JScrollPane(messageTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		messageTableScrollPane.setPreferredSize(new Dimension(350, 100));

		JPanel messagesTablePanel = new JPanel();
		// To make the JScrollPane auto resize
		messagesTablePanel.setLayout(new BoxLayout(messagesTablePanel, BoxLayout.Y_AXIS));
		messagesTablePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
		messagesTablePanel.add(messageTableScrollPane);
		messagesTablePanel.add(createMessagesActionPanel());
		
		JSplitPane horizontalSplitPane = new JSplitPane();
		horizontalSplitPane.setDividerLocation(350);
		horizontalSplitPane.setResizeWeight(.5D);
		horizontalSplitPane.setContinuousLayout(true);
		horizontalSplitPane.setBorder(null);

		JPanel messageViewerPanel = new JPanel();
		messageViewerPanel.setLayout(new BoxLayout(messageViewerPanel, BoxLayout.Y_AXIS));
		messageViewerPanel.add(messagesTablePanel, null);
		
		horizontalSplitPane.setTopComponent(topicTablePanel);
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
		
		this.messageEventListener = new MessageEventListener();
		
		domain.addListener(new DomainEventListener());
	}

	private JPanel createMessagesActionPanel() {
		// Create a panel for the message actions
		JPanel messagesActionPanel = new JPanel();
		messagesActionPanel.setLayout(new BoxLayout(messagesActionPanel, BoxLayout.X_AXIS));
		messagesActionPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		
		// Delete all messages button
		JButton clearBufferButton = CommonUITasks.createButton("Clear buffer", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearCurrentBuffer();
			}
		});
		CommonUITasks.makeSegmented(clearBufferButton, Segmented.FIRST);
		messagesActionPanel.add(clearBufferButton);
		
		// Delete button
		JButton deleteButton = CommonUITasks.createButton("Delete", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedMessages();
			}
		});
		CommonUITasks.makeSegmented(deleteButton, Segmented.MIDDLE);
		messagesActionPanel.add(deleteButton);

		// Save button
		JButton saveButton = CommonUITasks.createButton("Save", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveSelectedMessages();
			}
		});
		CommonUITasks.makeSegmented(saveButton, Segmented.LAST);
		messagesActionPanel.add(saveButton);
		return messagesActionPanel;
	}
	
	private void saveSelectedMessages() {
		List<Message> messages = CollectionFactory.newArrayList();
		
		int[] selectedRows = messageTable.getSelectedRows();
		for(int i: selectedRows) {
			messages.add(messageTable.getRowItem(i));
		}
		
		CommonUITasks.saveMessages(this, messages, worker, config);
	}

	/**
	 * Clear the messagebuffer for the currently displayed consumer
	 */
	private void clearCurrentBuffer() {
		JMSDestination destination = messageTable.getCurrentDestination();
		JMSSubscriber subscriber = subscriberTable.getItemForDestination(destination);
		
		if(subscriber != null) {
			messageTable.setData(destination, new ArrayList<Message>());
			subscriber.clear();
		}
	}
	
	/**
	 * Delete the messages currently selected in the message table
	 */
	private void deleteSelectedMessages() {
		final List<Message> messages = CollectionFactory.newArrayList();
		final ListSelectionModel lsm = messageTable.getSelectionModel();
		
		if(lsm.isSelectionEmpty()) {
			return;
		}
		
		int[] selectedIndexes = messageTable.getSelectedRows();
		
		for(int i: selectedIndexes) {
			Message message = messageTable.getRowItem(i);
			messages.add(message);
		}
		
		subscriberTable.getItemForDestination(messageTable.getCurrentDestination())
			.removeMessages(messages);
		
		for(int i=selectedIndexes.length-1; i>=0; i--) {
			messageTable.removeItem(messageTable.getRowItem(i));
		}
	}

	private JComboBox createBrokerCombo() {
		JComboBox cmb = new JComboBox();
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setAlignmentX(Component.CENTER_ALIGNMENT);
		cmb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getID() == ItemEvent.ITEM_STATE_CHANGED
				&& e.getStateChange() == ItemEvent.SELECTED) {
					JMSBroker selectedBroker = (JMSBroker)e.getItem();
					
					subscriberTable.clear();
					messageTable.clear();
					
					connectToBroker(selectedBroker);
					enumerateTopics(selectedBroker);
				}
			}
		});
		cmb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		return cmb;
	}
	
	private void connectToBroker(final JMSBroker broker) {
		// Connect to the broker
		worker.execute(injector.getInstance(TaskFactory.class).connectToBroker(broker));
	}

	/**
	 * Get the topics from configuration and start a topic enumeration task
	 * 
	 * @param selectedBroker
	 */
	private void enumerateTopics(final JMSBroker selectedBroker) {
		populateTopicTable(getConfiguredTopics(selectedBroker));

//		worker.execute(new Task() {
//			@Override
//			public void execute() throws Exception {
//				sonic.getDurableTopicList(selectedBroker, "");
//			}
//			
//			@Override
//			public String toString() {
//				return "Enumerating topics";
//			}
//		});
	}

	/**
	 * Get the configured topics for the broker.
	 * 
	 * @param broker
	 * @return
	 */
	private List<JMSTopic> getConfiguredTopics(JMSBroker broker) {
		List<String> topicNames = config.getTopicSubscriberNames(broker);
		final List<JMSTopic> topics = CollectionFactory.newArrayList();
		
		for(String name: topicNames) {
			topics.add(domain.createTopic(broker, name));
		}
		
		return topics;
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
	
	private TopicSubscriberTable createTopicTable(Injector injector) {
		final TopicSubscriberTable table = injector.getInstance(TopicSubscriberTable.class);
		
		// Give the table an empty list in case TOPICS_ENUMERATED never fires or there
		// are no topics to enumerate.
		table.setData(new ArrayList<JMSSubscriber>());
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				
				populateMessageTable(subscriberTable.getSelectedItem());
			}
		});

		return table;
	}
	
	private void populateMessageTable(JMSSubscriber newSubscriber) {
		JMSSubscriber currentSubscriber = subscriberTable.getItemForDestination(messageTable.getCurrentDestination()); 
		
		if(currentSubscriber != null && currentSubscriber != newSubscriber) {
			currentSubscriber.removeListener(messageEventListener);
			currentSubscriber.unlockMessages();
		}
		
		if(newSubscriber == null) {
			messageTable.setData(null, new ArrayList<Message>());
		} else {
			messageTable.setData(
				newSubscriber.getDestination(), 
				new ArrayList<Message>(newSubscriber.getMessages()));
	
			if(currentSubscriber != newSubscriber) {
				newSubscriber.addListener(messageEventListener);
			}
		}
	}
	
	private void addMessageToTable(final Message message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageTable.addItem(message);
			}
		});
	}
	
	private void removeMessageFromTable(final Message message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageTable.removeItem(message);
			}
		});
	}

	private void populateTopicTable(final List<JMSTopic> topics) {
		final List<JMSSubscriber> entries = CollectionFactory.newArrayList();
		for(JMSTopic t: topics) {
			entries.add(new JMSSubscriber(domain, worker, config, t, new MessageBuffer()));
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				subscriberTable.setData(entries);
			}
		});
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
	
	private void removeSelectedSubscribers() {
		int rows[] = subscriberTable.getSelectedRows();
		
		for(int i=rows.length-1; i>=0; i--) {
			final JMSSubscriber item = subscriberTable.getRowItem(rows[i]);			
			if(item != null) {
				item.setActive(false);
				subscriberTable.removeItem(item);
				config.removeTopicSubscriber((JMSTopic)item.getDestination());
			}
		}
	}
	
	private void displaySelectedMessage() {
		if(messageTable.getSelectedRow() == -1) {
			displayMessage(null);
		} else {
			displayMessage(messageTable.getSelectedItem());
		}
	}
	
	private void displayMessage(final Message message) {
		JMSSubscriber subscriber = subscriberTable.getSelectedItem();
		subscriber.unlockMessages();
		
		if(message != null) {
			subscriber.lockMessage(message);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				messageViewer.setMessage(message);
			}
		});
	}
	
	private JPanel createTopicActionPanel() {	
		// Textfield for topic name
		final JTextField topicNameField = new JTextField();
		topicNameField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE,
				topicNameField.getPreferredSize().height));
		
		// Remove button
		final JButton removeTopicButton = CommonUITasks.createButton("Remove", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedSubscribers();
			}
		});
		CommonUITasks.makeSegmented(removeTopicButton, Segmented.ONLY);
				
		// Add button 
		final JButton addTopicButton = CommonUITasks.createButton("Add subscriber", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String topicName = topicNameField.getText();
				
				if(topicName != null && topicName.trim().length() > 0) {
					JMSTopic topic = domain.createTopic((JMSBroker)brokerCombo.getSelectedItem(), topicName);
					
					JMSSubscriber subscriber = subscriberTable.getItemForDestination(topic); 
					if(subscriber == null) {
						subscriber = new JMSSubscriber(domain, worker, config, topic, new MessageBuffer());
						subscriber.setActive(true);
						subscriberTable.addItem(subscriber);
						config.addTopicSubscriber((JMSTopic)subscriber.getDestination());
					}
					
					topicNameField.setText("");
					subscriberTable.setSelectedItem(subscriber);
					subscriberTable.ensureRowVisible(subscriberTable.getItemRow(subscriber));
				}
			}
		});
		CommonUITasks.makeSegmented(addTopicButton, Segmented.ONLY);

		// Enter in topicNameField simulates Add button click
		topicNameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					addTopicButton.doClick();
				}
			}
		});
		
		// Create the panel
		final JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
			Math.max(removeTopicButton.getPreferredSize().height, topicNameField.getPreferredSize().height)));
		
		// Add everything to the panel
		actionPanel.add(removeTopicButton);
		actionPanel.add(Box.createHorizontalStrut(5));
		actionPanel.add(topicNameField);
		actionPanel.add(addTopicButton);

		return actionPanel;
	}
			
	public String getUITabName() {
		return "Topic subscriber";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {ConnectionState.CONNECTED};
	}

	private class DomainEventListener implements EventListener<DomainEvent> {
		@SuppressWarnings("unchecked")
		public void processEvent(DomainEvent event) {
			switch(event.getId()) {
			case BROKERS_ENUMERATED:
				populateBrokerCombo((List<JMSBroker>)event.getInfo());
				break;
			
			case BROKER_DISCONNECT:
				if(brokerCombo.getSelectedItem().equals(event.getInfo())) {
					populateTopicTable(new ArrayList<JMSTopic>());
					CommonUITasks.clear(messageTable);
				}
				break;
			}			
		}
	}
	
	private class MessageEventListener implements EventListener<MessageEvent> {
		public void processEvent(MessageEvent event) {
			switch(event.getId()) {
			case MESSAGE_RECEIVED:
				addMessageToTable((Message)event.getInfo());
				break;
				
			case MESSAGE_DISCARDED:
				removeMessageFromTable((Message)event.getInfo());
				break;
			}
		}
	}
}
