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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.EnumerateQueuesTask;
import nl.queuemanager.core.tasks.SendFileListTask;
import nl.queuemanager.core.tasks.SendMessageListTask;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;
import nl.queuemanager.jms.JMSDestination.TYPE;
import nl.queuemanager.jms.impl.MessageFactory;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.util.JIntegerField;
import nl.queuemanager.ui.util.JSearchableTextArea;
import nl.queuemanager.ui.util.SpringUtilities;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class MessageSendTabPanel extends JPanel {
	private final String[] deliveryModes = {"PERSISTENT", "NON-PERSISTENT"};
	
	private final JComboBox brokerCombo;
	private final JMSDestinationTable destinationTable;
	private final JMSDomain sonic;
	private final TaskExecutor worker;
	private final Configuration config;

	private JTextField filenameField;
	private JIntegerField numberOfMessagesField;
	private JTextArea typingArea;
	private boolean isFromImport;
	private JTextField jmsCorrelationIDField;
	private JMSDestinationField sendDestinationField;
	private JMSDestinationField jmsReplyToField;
	private JIntegerField jmsTTLField;
	private JComboBox deliveryModeCombo;
	private JTextField customPropertiesField;
	private JIntegerField delayPerMessageField;
	private JButton sendButton;
	private Map<String, Object> properties = CollectionFactory.newHashMap();
	
	@Inject
	public MessageSendTabPanel(JMSDomain sonic, TaskExecutor worker, Configuration config) {		
		this.sonic = sonic;
		this.worker = worker;
		this.config = config;
				
		/******************************
		 * Left side -- Queues and topic tables **
		 *****************************/
		brokerCombo = createBrokerCombo();
				
		// Create the destination table and wrap it in a scrollpane
		destinationTable = new JMSDestinationTable(sonic, worker);
		JScrollPane destinationTableScrollPane = new JScrollPane(destinationTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		destinationTableScrollPane.setPreferredSize(new Dimension(350, 100));
		destinationTableScrollPane.setViewportView(destinationTable);
		destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					sendDestinationField.setDestination(
						destinationTable.getSelectedItem());
			}
		});
		
		JPanel actionPanel = createActionPanel();
		
		// Create a panel for the broker combo, tables and action panel
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(brokerCombo);
		leftPanel.add(destinationTableScrollPane);
		leftPanel.add(actionPanel);
		
		/******************************
		 * Right side -- Settings    **
		 *****************************/
		
		JPanel sendMessageForm = createForm();
		
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		rightPanel.add(sendMessageForm);
		
		/******************************
		 * Main layout               **
		 *****************************/
		
		// Create the main splitpane (queues left, the rest right)
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(splitPane);
		
		sonic.addListener(new DomainEventListener());
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
					
					destinationTable.clear();
					
					connectToBroker(selectedBroker);
					enumerateTopics(selectedBroker);
					enumerateQueues(selectedBroker);
				}
			}
		});
		cmb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		return cmb;
	}
	
	private JPanel createActionPanel() {		
		JButton refreshButton = CommonUITasks.createButton("Refresh", new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				refreshQueues();
			}
		});
		CommonUITasks.makeSegmented(refreshButton, Segmented.ONLY);
		
		// Textfield for topic name
		final JTextField topicNameField = new JTextField();
		topicNameField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE,
				topicNameField.getPreferredSize().height));

		// Remove button
		final JButton removeTopicButton = CommonUITasks.createButton("Remove", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JMSDestination selectedItem = destinationTable.getSelectedItem();
				if(selectedItem == null)
					return;
				
				if(TYPE.TOPIC != selectedItem.getType()) {
					JOptionPane.showMessageDialog(null, "Only topics can be removed from the list");
					return;
				}
				
				destinationTable.removeItem(selectedItem);
				topicNameField.setText(selectedItem.getName());
				
				config.removeTopicPublisher((JMSTopic)selectedItem);
			}
		});
		CommonUITasks.makeSegmented(removeTopicButton, Segmented.ONLY);
				
		// Add button 
		final JButton addTopicButton = CommonUITasks.createButton("Add publisher", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final String topicName = topicNameField.getText();
				
				if(topicName != null && topicName.trim().length() > 0) {
					JMSTopic topic = sonic.createTopic((JMSBroker)brokerCombo.getSelectedItem(), topicName);
					
					if(destinationTable.getItemRow(topic) == -1) {
						destinationTable.addItem(topic);
						config.addTopicPublisher(topic);
					}
					
					topicNameField.setText("");
					destinationTable.setSelectedItem(topic);
					destinationTable.ensureRowVisible(destinationTable.getItemRow(topic));
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
		actionPanel.add(refreshButton);
		actionPanel.add(Box.createHorizontalStrut(5));
		actionPanel.add(removeTopicButton);
		actionPanel.add(Box.createHorizontalStrut(2));
		actionPanel.add(topicNameField);
		actionPanel.add(Box.createHorizontalStrut(2));
		actionPanel.add(addTopicButton);

		return actionPanel;
	}
	
	private JPanel createForm() {
		final JPanel panel = new JPanel();
		
		final JPanel formPanel = new JPanel();
		formPanel.setLayout(new SpringLayout());
		
		sendDestinationField = new JMSDestinationField();
		formPanel.add(createLabelFor(sendDestinationField, "Destination"));
		formPanel.add(sendDestinationField);
	
		numberOfMessagesField = new JIntegerField(10);
		numberOfMessagesField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, 
				numberOfMessagesField.getPreferredSize().height));
		numberOfMessagesField.setValue(1);
		numberOfMessagesField.setToolTipText("The number that the message to be sent");
		numberOfMessagesField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				updateSendButton();
			}

			public void insertUpdate(DocumentEvent e) {
				updateSendButton();
			}

			public void removeUpdate(DocumentEvent e) {
				updateSendButton();
			}
		});
		formPanel.add(createLabelFor(numberOfMessagesField, "Number of messages:"));
		formPanel.add(numberOfMessagesField);
		
		delayPerMessageField = new JIntegerField(6);
		delayPerMessageField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE,
				delayPerMessageField.getPreferredSize().height));
		delayPerMessageField.setValue(0);
		delayPerMessageField.setToolTipText("The number of milliseconds to wait between messages");
		formPanel.add(createLabelFor(delayPerMessageField, "Delay (ms):"));
		formPanel.add(delayPerMessageField);
		
		jmsCorrelationIDField = new JTextField();
		jmsCorrelationIDField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, 
				jmsCorrelationIDField.getPreferredSize().height));
		jmsCorrelationIDField.setToolTipText("CorrelationID, use %i for sequence number");
		jmsCorrelationIDField.setText("Message %i");
		formPanel.add(createLabelFor(jmsCorrelationIDField, "JMS Correlation ID:"));
		formPanel.add(jmsCorrelationIDField);
		
		jmsReplyToField = new JMSDestinationField();
		jmsReplyToField.setToolTipText("Type a name or drag a destination from the table on the left");
		formPanel.add(createLabelFor(jmsReplyToField, "JMS Reply to:"));
		formPanel.add(jmsReplyToField);

		jmsTTLField = new JIntegerField(10);
		jmsTTLField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, 
				jmsTTLField.getPreferredSize().height));
		jmsTTLField.setMinValue(0);
		jmsTTLField.setValue(0);
		jmsTTLField.setToolTipText("The number of seconds after which the message is no longer valid.");
		formPanel.add(createLabelFor(jmsTTLField, "Time to live (sec):"));
		formPanel.add(jmsTTLField);
		
		deliveryModeCombo = new JComboBox(deliveryModes);
		deliveryModeCombo.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, 
				deliveryModeCombo.getPreferredSize().height));
		deliveryModeCombo.setToolTipText("The delivery mode, persistent or non-persistent");
		deliveryModeCombo.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		formPanel.add(createLabelFor(deliveryModeCombo, "JMS Delivery Mode:"));
		formPanel.add(deliveryModeCombo);		
		
		JPanel propertiesButtonPanel = createPropertiesButtonPanel();
		formPanel.add(createLabelFor(propertiesButtonPanel, "Custom JMS properties:"));
		formPanel.add(propertiesButtonPanel);
		
		JPanel fileBrowsePanel = createFileBrowsePanel();
		JPanel typeMyOwnPanel = createTypeMyOwnPanel();
		
		JPanel radioPanel = createRadioButtonPanel(fileBrowsePanel, typeMyOwnPanel);
		formPanel.add(createLabelFor(radioPanel, "Message content:"));
		formPanel.add(radioPanel);
		
		SpringUtilities.makeCompactGrid(formPanel, 
				9, 2, 
				0, 0, 
				5, 5);		
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(formPanel);
		
		panel.add(fileBrowsePanel);
		panel.add(typeMyOwnPanel);
		
		panel.add(new Box.Filler(
				new Dimension(5, 5),
				new Dimension(5, 5),
				new Dimension(5, 5)));
		panel.add(Box.createHorizontalStrut(5));
		
		final JButton sendButton = createSendButton();
		CommonUITasks.makeSegmented(sendButton, Segmented.ONLY);
		panel.add(sendButton);		
		
		return panel;		
	}
	
	private JPanel createPropertiesButtonPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		customPropertiesField = new JTextField();
		customPropertiesField.setMaximumSize(new Dimension(Integer.MAX_VALUE, customPropertiesField.getPreferredSize().height));
		customPropertiesField.setEditable(false);
		customPropertiesField.setToolTipText("The custom properties for the message");
		panel.add(customPropertiesField);
		
		final JButton editButton = CommonUITasks.createButton("Edit...",
		new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				properties = PropertiesDialog.editProperties(properties);
				String propertiesText = createPropertiesText(properties);
				customPropertiesField.setText(propertiesText);
			}
		});
		CommonUITasks.makeSegmented(editButton, Segmented.ONLY);
		panel.add(editButton);
		
		return panel;
	}
	
	private String createPropertiesText(Map<String, ? extends Object> prop){
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, ? extends Object> entry: prop.entrySet()) {
			sb.append(entry.getKey());
			sb.append("=(");
			sb.append(entry.getValue().getClass().getSimpleName());
			sb.append(")");
			sb.append(entry.getValue());
			sb.append(",");
		}
		
		if (sb.length() > 0)
			return sb.substring(0, sb.length() -1);
		return sb.toString();
	}

	private JPanel createRadioButtonPanel(
			final JComponent importPanel, 
			final JComponent typePanel) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		final ButtonGroup buttonGroup = new ButtonGroup();
		
		final JRadioButton sendFileRadioButton = new JRadioButton("Send a file", true); 
		panel.add(sendFileRadioButton);
	
		sendFileRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				importPanel.setVisible(true);
				typePanel.setVisible(false);
				isFromImport = true;
			}
		});
				
		final JRadioButton composeRadioButton = new JRadioButton("Compose message", false); 
		panel.add(composeRadioButton);
	
		composeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				importPanel.setVisible(false);
				typePanel.setVisible(true);
				isFromImport = false;
			}
		});		
				
		buttonGroup.add(sendFileRadioButton);
		buttonGroup.add(composeRadioButton);
		
		// Set the compose button as selected
		buttonGroup.setSelected(composeRadioButton.getModel(), true);
		importPanel.setVisible(false);
		typePanel.setVisible(true);
		isFromImport = false;
		
		return panel;
	}

	private JPanel createTypeMyOwnPanel() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		typingArea = new JSearchableTextArea();
		
		// Set up drag & drop support for files
		new DropTarget(typingArea, new FileDropTargetListener(typingArea));
		
		JScrollPane scrollPane = new JScrollPane(typingArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		
		panel.add(scrollPane);
		
		return panel;
	}

	private JPanel createFileBrowsePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		filenameField = new JTextField();
		filenameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
		panel.add(filenameField);
		
		panel.add(new Box.Filler(
				new Dimension(5, 5),
				new Dimension(5, 5),
				new Dimension(5, 5)));
		
		final JButton browseButton = new JButton();
		browseButton.setText("Browse...");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {				
			      JFileChooser chooser = new JFileChooser();
			      chooser.setCurrentDirectory(new File(
			    		  config.getUserPref(
			    				  Configuration.PREF_BROWSE_DIRECTORY, 
			    				  ".")));
			      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			      int r = chooser.showOpenDialog(MessageSendTabPanel.this);
			      if (r == JFileChooser.APPROVE_OPTION) {
			        String fname = chooser.getSelectedFile().getPath();
			        filenameField.setText(fname);
			        
			        config.setUserPref(
			        		Configuration.PREF_BROWSE_DIRECTORY,
			        		chooser.getCurrentDirectory().getAbsolutePath());
			      }
			    
			}
		});
		CommonUITasks.makeSegmented(browseButton, Segmented.ONLY);
		panel.add(browseButton);
		
		return panel;
	}

	private JButton createSendButton() {
		sendButton = CommonUITasks.createButton("Send message", 
		new ActionListener() {
			String messageContent = null;
			
			public void actionPerformed(final ActionEvent e) {
				try {
					String filePath = filenameField.getText();
					
					int numberOfMessages = numberOfMessagesField.getValue();
					
					JMSDestination sendDestination = sendDestinationField.getDestination(
						sonic, (JMSBroker) brokerCombo.getSelectedItem());
					
					String jmsCorrIdValue = jmsCorrelationIDField.getText();
					
					JMSDestination jmsReplyToValue = jmsReplyToField.getDestination(
						sonic, (JMSBroker) brokerCombo.getSelectedItem());
					
					long jmsTimeToLiveValue = jmsTTLField.getValue();
										
					if(sendDestination == null) {
						JOptionPane.showMessageDialog(null,	"Please select or enter a destination" );
						return;
					}
					
					if(numberOfMessages == 0) {
						JOptionPane.showMessageDialog(null, "Please enter the number of messages to send" );
						return;
					}
					
					if(isFromImport)
					{					
						if(filePath == null || filePath.equals(""))
						{
							JOptionPane.showMessageDialog(null,
							"Please select a file" );
						}else
						{
							sendMessage(
								sendDestination, 
								numberOfMessages, 
								delayPerMessageField.getValue(), 
								getDeliveryMode(),
								new File(filePath), 
								jmsCorrIdValue, 
								jmsReplyToValue,
								jmsTimeToLiveValue != 0 ? jmsTimeToLiveValue : null,
								properties);
						}
					} else {
						messageContent = typingArea.getText();
						
						sendMessage(
							sendDestination, 
							numberOfMessages, 
							delayPerMessageField.getValue(), 
							getDeliveryMode(),
							messageContent == null ? "" : messageContent, 
							jmsCorrIdValue, 
							jmsReplyToValue, 
							jmsTimeToLiveValue != 0 ? jmsTimeToLiveValue : null,
							properties);
					}					
				} catch (JMSException ex) {
					JOptionPane.showMessageDialog(null, ex.toString());
				}
			}
		});
		
		CommonUITasks.makeSegmented(sendButton, Segmented.ONLY);
		return sendButton;
	}

	private Component createLabelFor(JComponent parent, String text) {
		JLabel label = new JLabel(text);
		label.setLabelFor(parent);
		return label;
	}
	
	protected void refreshQueues() {
		final JMSBroker broker = (JMSBroker)brokerCombo.getSelectedItem();
		
		worker.execute(new EnumerateQueuesTask(sonic, broker, null));
	};
	
	private int getDeliveryMode(){
		String item = (String)deliveryModeCombo.getSelectedItem();
		if(item.equalsIgnoreCase("NON-PERSISTENT"))
			return DeliveryMode.NON_PERSISTENT;
		return  DeliveryMode.PERSISTENT;
	}
	
	/**
	 * @param queue
	 * @param number
	 * @param delay
	 * @param deliveryMode
	 * @param file
	 * @param jmsCorrelationIdFieldValue
	 * @param jmsReplyToValue
	 * @param jmsTimeToLive
	 * @param props
	 * @throws JMSException
	 */
	private void sendMessage(
			final JMSDestination queue, 
			final int number, 
			final int delay,
			final int deliveryMode,
			final File file,
			final String jmsCorrelationIdFieldValue,
			final JMSDestination jmsReplyToValue,
			final Long jmsTimeToLive,
			final Map<String, ? extends Object> props) throws JMSException
	{
		Message message = prepMessage(
				jmsCorrelationIdFieldValue,
				jmsReplyToValue, 
				jmsTimeToLive);
		
		message.setJMSDeliveryMode(deliveryMode);
		
		//Set custom properties
		setMessageProperties(props, message);
		
		// Send the file(list) and schedule a refresh of the destination table
		worker.executeInOrder(
			new SendFileListTask(queue, file, message, number, delay, sonic),
			new EnumerateQueuesTask(sonic, queue.getBroker(), null));
	}

	/**
	 * @param queue
	 * @param number
	 * @param delay
	 * @param deliveryMode
	 * @param messageContent
	 * @param jmsCorrelationIdFieldValue
	 * @param jmsReplyToValue
	 * @param jmsTimeToLive
	 * @param props
	 * @throws JMSException
	 */
	private void sendMessage(
			final JMSDestination queue, 
			final int number,
			final int delay,
			final int deliveryMode,
			final String messageContent,
			final String jmsCorrelationIdFieldValue,
			final JMSDestination jmsReplyToValue,
			final Long jmsTimeToLive,
			final Map<String, ? extends Object> props) throws JMSException
	{
		TextMessage message = (TextMessage)prepMessage(
				MessageFactory.createTextMessage(),
				jmsCorrelationIdFieldValue,
				jmsReplyToValue, 
				jmsTimeToLive);

		message.setText(messageContent);
		message.setJMSDeliveryMode(deliveryMode);
		//Set custom properties
		setMessageProperties(props, message);
		
		// Create the tasks for sending and browsing messages
		worker.executeInOrder(
			new SendMessageListTask(queue, message, number, delay, sonic),
			new EnumerateQueuesTask(sonic, queue.getBroker(), null));
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
		worker.execute(new Task(broker) {
			@Override
			public void execute() throws Exception {
				sonic.connectToBroker(broker);
			}
			@Override
			public String toString() {
				return "Connecting to broker " + broker;
			}
		});
	}
	
	/**
	 * Get the topics from the configuration and polulate the topic list.
	 * 
	 * @param broker
	 */
	private void enumerateTopics(final JMSBroker broker) {
		destinationTable.updateData(getConfiguredTopics(broker));
	}
	
	/**
	 * Start a new queue enumeration task for the broker.
	 * 
	 * @param broker
	 */
	private void enumerateQueues(final JMSBroker broker) {		
		// Get the queue list from the broker
		worker.execute(new EnumerateQueuesTask(sonic, broker, null));
	}
	
	/**
	 * Get the configured topics for the broker.
	 * 
	 * @param broker
	 * @return
	 */
	private List<JMSTopic> getConfiguredTopics(JMSBroker broker) {
		List<String> topicNames = config.getTopicPublisherNames(broker);
		final List<JMSTopic> topics = CollectionFactory.newArrayList();
		
		for(String name: topicNames) {
			topics.add(sonic.createTopic(broker, name));
		}
		
		return topics;
	}

	private Message prepMessage(
			final String jmsCorrelationIdFieldValue,
			final JMSDestination jmsReplyToValue, 
			final Long jmsTimeToLive) throws JMSException {
		
		Message message = MessageFactory.createMessage();
		return prepMessage(message, jmsCorrelationIdFieldValue, jmsReplyToValue, jmsTimeToLive);
	}
	
	/**
	 * Prepare a message for sending.
	 * 
	 * @param message
	 * @param jmsCorrelationIdFieldValue
	 * @param jmsReplyToValue
	 * @param jmsTimeToLive
	 * @return
	 * @throws JMSException
	 */
	private Message prepMessage(
			final Message message,
			final String jmsCorrelationIdFieldValue,
			final JMSDestination jmsReplyToValue, 
			final Long jmsTimeToLive) throws JMSException {
		
		message.setJMSCorrelationID(jmsCorrelationIdFieldValue);
	
		if(jmsTimeToLive != null)
			message.setJMSExpiration(System.currentTimeMillis() + jmsTimeToLive*1000);
		
		if(jmsReplyToValue != null)
			message.setJMSReplyTo(jmsReplyToValue);
		
		return message;
	}

	private void setMessageProperties(final Map<String, ? extends Object> props, Message message) throws JMSException {
		for(Map.Entry<String, ? extends Object> entry: props.entrySet()) {
			message.setObjectProperty(entry.getKey(), entry.getValue());
		}
	}
	
	private void updateSendButton() {
		int numberOfMessages = numberOfMessagesField.getValue();
		
		switch(numberOfMessages) {
			case 0:
				sendButton.setEnabled(false);
				sendButton.setText("Send message");
				break;
				
			case 1:
				sendButton.setEnabled(true);
				sendButton.setText("Send message");
				break;
				
			default:
				sendButton.setEnabled(true);
				sendButton.setText(String.format("Send %d messages", numberOfMessages));
				break;
		}
	}
	
	private class DomainEventListener implements EventListener<DomainEvent> {

		@SuppressWarnings("unchecked")
		public void processEvent(final DomainEvent event) {
			switch(event.getId()) {
			
			case BROKERS_ENUMERATED:
				populateBrokerCombo((List<JMSBroker>)event.getInfo());
				break;
			
			case QUEUES_ENUMERATED:
				final List<JMSQueue> queueList = (List<JMSQueue>)event.getInfo();
				if(queueList.size() > 0 && queueList.get(0).getBroker().equals(brokerCombo.getSelectedItem())) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							destinationTable.updateData(queueList);
						}
					});
				}
				
				break;
				
			case BROKER_DISCONNECT:
				if(brokerCombo.getSelectedItem().equals(event.getInfo())) {
					CommonUITasks.clear(destinationTable);
				}
				break;
			}
		}		
	}
	
	/**
	 * Implements file drop operations on JTextComponent. Delegaring to the
	 * original TransferHandler where appropriate.
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 *
	 */
	private static class FileDropTargetListener extends DropTargetAdapter {
		private final JTextComponent component;
		
		public FileDropTargetListener(JTextComponent component) {
			this.component = component;
		}

		public void drop(DropTargetDropEvent dtde) {
			if((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0 
			&& canImport(dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				dtde.dropComplete(importData(component, dtde.getTransferable()));
				dragExit(null);
			} else {
				dtde.rejectDrop();
				dragExit(null);
			}
		}

		private boolean canImport(DataFlavor[] transferFlavors) {
			for(DataFlavor flavor: transferFlavors) {
				if(DataFlavor.javaFileListFlavor.equals(flavor)) {
					return true;
				}
				
				if(DataFlavor.stringFlavor.equals(flavor)) {
					return true;
				}
			}

			return false;
		}

		public boolean importData(JComponent comp, Transferable t) {
			if(!canImport(t.getTransferDataFlavors()))
				return false;
			
			try {
				if(comp instanceof JTextComponent) {
					DataFlavor[] availableFlavors = t.getTransferDataFlavors();
					for(DataFlavor flavor: availableFlavors) {
						if(DataFlavor.javaFileListFlavor.equals(flavor)) {
							return importFileList((JTextComponent)comp, t);
						}
						
						if(DataFlavor.stringFlavor.equals(flavor)) {
							return importString((JTextComponent)comp, t);
						}
					}
				}
			} catch (UnsupportedFlavorException e) {
				JOptionPane.showMessageDialog(null, "Cannot import dropped object. Unsupported DataFlavor: " + e.getMessage(), "Error while accepting drop", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error reading file", JOptionPane.ERROR_MESSAGE);
			}
			
			return false;
		}

		private boolean importString(JTextComponent comp, Transferable t) throws UnsupportedFlavorException, IOException {
			Reader reader = DataFlavor.stringFlavor.getReaderForText(t);
			StringBuffer buffer = new StringBuffer();
			
			System.out.println("Reader is " + reader.getClass().getName());
			
			int c = -1;
			while((c = reader.read()) != -1) {
				buffer.append((char)c);
			}
			
			comp.setText(buffer.toString());
			return true;
		}

		@SuppressWarnings("unchecked")
		private boolean importFileList(JTextComponent comp, Transferable t) throws UnsupportedFlavorException, IOException {
			// Read the files one by one and append the contents to a StringBuffer.
			StringBuffer buffer = new StringBuffer();
			List<File> fileList = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
			
			for(File file: fileList) {
				buffer.append(readFileContents(file));
				buffer.append('\n');
			}
			
			comp.setText(buffer.toString());
			
			return true;
		}

		private char[] readFileContents(File file) throws IOException {
			FileReader reader = new FileReader(file);
			char[] buffer = new char[(int)file.length()];
			reader.read(buffer);
			reader.close();
			return buffer;
		}
	}
	
	private static class JMSDestinationField extends Box {
		private final JTextField nameField;
		private final JComboBox typeField;
		
		public JMSDestinationField() {
			super(BoxLayout.X_AXIS);
			nameField = new JTextField();
			nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
					(int)nameField.getPreferredSize().getHeight()));
			typeField = new JComboBox(new TYPE[] {
					JMSDestination.TYPE.QUEUE, JMSDestination.TYPE.TOPIC});
			typeField.setMaximumSize(typeField.getPreferredSize());
//			typeField.setPrototypeDisplayValue(JMSDestination.TYPE.QUEUE);
			add(nameField);
			add(Box.createHorizontalStrut(5));
			add(typeField);
			
			setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)getPreferredSize().getHeight()));
			
			new DropTarget(nameField, DnDConstants.ACTION_COPY_OR_MOVE, new DTL());
		}
		
		@Override
		public void setToolTipText(String text) {
			super.setToolTipText(text);
			nameField.setToolTipText(text);
		}
		
		/**
		 * Return the entered destination, null if the name field is empty.
		 * 
		 * @param domain The JMSDomain to use to create the destination object
		 * 
		 * @return
		 */
		public JMSDestination getDestination(JMSDomain domain, JMSBroker broker) {
			String name = nameField.getText();
			
			if(name == null || name.trim().length() == 0)
				return null;
			
			if(JMSDestination.TYPE.QUEUE.equals(typeField.getSelectedItem()))
				return domain.createQueue(broker, name);
			
			if(JMSDestination.TYPE.TOPIC.equals(typeField.getSelectedItem()))
				return domain.createTopic(broker, name);
			
			return null;
		}
		
		public void setDestination(JMSDestination destination) {
			this.nameField.setText(destination.getName());
			this.typeField.setSelectedItem(destination.getType());
		}
		
		private class DTL extends DropTargetAdapter {
			public void drop(DropTargetDropEvent dtde) {
				if((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0 
				&& canImport(dtde.getCurrentDataFlavors())) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					dtde.dropComplete(importData(dtde.getTransferable()));
					dragExit(null);
				} else {
					dtde.rejectDrop();
					dragExit(null);
				}
			}

			private boolean importData(Transferable transferable) {
				try {
					if(transferable.isDataFlavorSupported(JMSDestinationInfoTransferable.jmsDestinationInfosFlavor))
						return importData((JMSDestinationInfo[])transferable.getTransferData(JMSDestinationInfoTransferable.jmsDestinationInfosFlavor));
					
					if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
						return importData((String)transferable.getTransferData(DataFlavor.stringFlavor));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				}
				
				return false;
			}

			private boolean importData(String transferData) {
				JMSDestinationField.this.nameField.setText(transferData);
				return true;
			}

			private boolean importData(JMSDestinationInfo[] transferData) {
				JMSDestinationField.this.nameField.setText(transferData[0].getName());
				JMSDestinationField.this.typeField.setSelectedItem(transferData[0].getType());
				return true;
			}

			private boolean canImport(DataFlavor[] currentDataFlavors) {
				for(DataFlavor flavor: currentDataFlavors) {
					if(JMSDestinationInfoTransferable.jmsDestinationInfosFlavor.equals(flavor))
						return true;
					
					if(DataFlavor.stringFlavor.equals(flavor))
						return true;
				}

				return false;
			}
		}
	}
}
