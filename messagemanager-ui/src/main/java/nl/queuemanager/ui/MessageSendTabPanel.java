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

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import nl.queuemanager.core.MessageBuffer;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSDestination.TYPE;
import nl.queuemanager.jms.JMSQueue;
import nl.queuemanager.jms.JMSTopic;
import nl.queuemanager.jms.impl.MessageFactory;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.util.*;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MessageSendTabPanel extends JPanel implements UITab {
	private final String[] deliveryModes = {"PERSISTENT", "NON-PERSISTENT"};
	
	private final JComboBox<JMSBroker> brokerCombo;
	private final JMSDestinationTable destinationTable;
	private final JMSDomain sonic;
	private final TaskExecutor worker;
	private final TaskFactory taskFactory;
	private final CoreConfiguration config;
	private final QueueCountsRefresher qcRefresher;

	private JTextField filenameField;
	private JIntegerField numberOfMessagesField;
	private JSearchableTextArea typingArea;
	private boolean isFromImport;
	private JTextField jmsCorrelationIDField;
	private JIntegerField jmsPriorityField;
	private JMSDestinationField sendDestinationField;
	private JMSDestinationField jmsReplyToField;
	private JIntegerField jmsTTLField;
	private JComboBox<String> deliveryModeCombo;
	private JTextField customPropertiesField;
	private JIntegerField delayPerMessageField;
	private JButton sendButton;
	private Map<String, Object> properties = CollectionFactory.newHashMap();
	private List<String> predefinedPropertyNames = new ArrayList<>();
	
	@Inject
	public MessageSendTabPanel(JMSDomain sonic, TaskExecutor worker, CoreConfiguration config, 
			TaskFactory taskFactory, JMSDestinationTable destinationTable, 
			QueueCountsRefresher refresher) 
	{
		this.sonic = sonic;
		this.worker = worker;
		this.config = config;
		this.taskFactory = taskFactory;
		this.qcRefresher = refresher;
		predefinedPropertyNames = sonic.getPredefinedPropertyNames();

				
		/******************************
		 * Left side -- Queues and topic tables **
		 *****************************/
		brokerCombo = createBrokerCombo();
				
		// Create the destination table and wrap it in a scrollpane
		this.destinationTable = destinationTable;
		JScrollPane destinationTableScrollPane = new JScrollPane(destinationTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		destinationTableScrollPane.setPreferredSize(new Dimension(350, 100));
		destinationTableScrollPane.setViewportView(destinationTable);
		destinationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					sendDestinationField.setDestination(
						MessageSendTabPanel.this.destinationTable.getSelectedItem());
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
	}
	
	private JComboBox<JMSBroker> createBrokerCombo() {
		JComboBox<JMSBroker> cmb = new JComboBox<JMSBroker>();
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setAlignmentX(Component.CENTER_ALIGNMENT);
		cmb.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getID() != ItemEvent.ITEM_STATE_CHANGED)
					return;
				
				switch(e.getStateChange()) {
				case ItemEvent.DESELECTED: {
					JMSBroker previouslySelectedBroker = (JMSBroker)e.getItem();
					if(previouslySelectedBroker != null) {
						if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_QUEUE)) {
							qcRefresher.unregisterInterest(previouslySelectedBroker);
						}
					}
				} break;
				
				case ItemEvent.SELECTED: {
					JMSBroker selectedBroker = (JMSBroker)e.getItem();
					
					destinationTable.clear();

					if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_QUEUE)) {
						qcRefresher.registerInterest(selectedBroker);
					}

					connectToBroker(selectedBroker);
					if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_TOPIC)) {
						enumerateTopics(selectedBroker);
					}

					if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_QUEUE)) {
						enumerateQueues(selectedBroker);
					}
				} break;
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

		final JButton removeTopicButton;
		final JTextField topicNameField;
		final JButton addTopicButton;

		if(sonic.isFeatureSupported(JMSFeature.TOPIC_SUBSCRIBER_CREATION)) {
			CommonUITasks.makeSegmented(refreshButton, Segmented.ONLY);
			// Textfield for topic name
			topicNameField = new JTextField();
			topicNameField.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					topicNameField.getPreferredSize().height));

			// Remove button
			removeTopicButton = CommonUITasks.createButton("Remove", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final JMSDestination selectedItem = destinationTable.getSelectedItem();
					if (selectedItem == null)
						return;

					if (TYPE.TOPIC != selectedItem.getType()) {
						JOptionPane.showMessageDialog(null, "Only topics can be removed from the list");
						return;
					}

					destinationTable.removeItem(selectedItem);
					topicNameField.setText(selectedItem.getName());

					config.removeTopicPublisher((JMSTopic) selectedItem);
				}
			});
			CommonUITasks.makeSegmented(removeTopicButton, Segmented.ONLY);

			// Add button
			addTopicButton = CommonUITasks.createButton("Add Publisher", new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					final String topicName = topicNameField.getText();

					if (topicName != null && topicName.trim().length() > 0) {
						JMSTopic topic = sonic.createTopic((JMSBroker) brokerCombo.getSelectedItem(), topicName);

						if (destinationTable.getItemRow(topic) == -1) {
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
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						addTopicButton.doClick();
					}
				}
			});
		}
		else {
			removeTopicButton = null;
			topicNameField = null;
			addTopicButton = null;
		}

		// Create the panel
		final JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		if(removeTopicButton != null && topicNameField != null) {
			actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
					Math.max(removeTopicButton.getPreferredSize().height, topicNameField.getPreferredSize().height)));
		}
		
		// Add everything to the panel
		actionPanel.add(refreshButton);
		if(sonic.isFeatureSupported(JMSFeature.TOPIC_SUBSCRIBER_CREATION)) {
			actionPanel.add(Box.createHorizontalStrut(5));
			actionPanel.add(removeTopicButton);
			actionPanel.add(Box.createHorizontalStrut(2));
			actionPanel.add(topicNameField);
			actionPanel.add(Box.createHorizontalStrut(2));
			actionPanel.add(addTopicButton);
		}

		return actionPanel;
	}
	
	private JPanel createForm() {
		final JPanel panel = new JPanel();

		int numRows = 0;

		final JPanel formPanel = new JPanel();
		formPanel.setLayout(new SpringLayout());

		sendDestinationField = new JMSDestinationField(getSupportedDestinationTypes());
		formPanel.add(createLabelFor(sendDestinationField, "Destination"));
		formPanel.add(sendDestinationField);
		numRows++;

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
		formPanel.add(createLabelFor(numberOfMessagesField, "Number of Messages:"));
		formPanel.add(numberOfMessagesField);
		numRows++;

		delayPerMessageField = new JIntegerField(6);
		delayPerMessageField.setMaximumSize(new Dimension(
				Integer.MAX_VALUE,
				delayPerMessageField.getPreferredSize().height));
		delayPerMessageField.setValue(0);
		delayPerMessageField.setToolTipText("The number of milliseconds to wait between messages");
		formPanel.add(createLabelFor(delayPerMessageField, "Delay (ms):"));
		formPanel.add(delayPerMessageField);
		numRows++;

		if (sonic.isFeatureSupported(JMSFeature.JMS_HEADERS)) {
			jmsCorrelationIDField = new JTextField();
			jmsCorrelationIDField.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					jmsCorrelationIDField.getPreferredSize().height));
			jmsCorrelationIDField.setToolTipText("CorrelationID, use %i for sequence number");
			jmsCorrelationIDField.setText("Message %i");
			formPanel.add(createLabelFor(jmsCorrelationIDField, "JMS Correlation ID:"));
			formPanel.add(jmsCorrelationIDField);
			numRows++;


			if (sonic.isFeatureSupported(JMSFeature.MESSAGE_SET_PRIORITY)) {
				jmsPriorityField = new JIntegerField(1);
				jmsPriorityField.setMaximumSize(new Dimension(
						Integer.MAX_VALUE,
						jmsPriorityField.getPreferredSize().height));
				jmsPriorityField.setMaxValue(9);
				jmsPriorityField.setToolTipText("The JMS Priority to use when sending the message (0-9");
				formPanel.add(createLabelFor(jmsPriorityField, "JMS Priority:"));
				formPanel.add(jmsPriorityField);
				numRows++;
			}

			jmsReplyToField = new JMSDestinationField();
			jmsReplyToField.setToolTipText("Type a name or drag a destination from the table on the left");
			formPanel.add(createLabelFor(jmsReplyToField, "JMS Reply to:"));
			formPanel.add(jmsReplyToField);
			numRows++;

			jmsTTLField = new JIntegerField(10);
			jmsTTLField.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					jmsTTLField.getPreferredSize().height));
			jmsTTLField.setMinValue(0);
			jmsTTLField.setValue(0);
			jmsTTLField.setToolTipText("The number of seconds after which the message is no longer valid.");
			formPanel.add(createLabelFor(jmsTTLField, "Time to Live (sec):"));
			formPanel.add(jmsTTLField);
			numRows++;

			deliveryModeCombo = new JComboBox<String>(deliveryModes);
			deliveryModeCombo.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					deliveryModeCombo.getPreferredSize().height));
			deliveryModeCombo.setToolTipText("The delivery mode, persistent or non-persistent");
			deliveryModeCombo.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
			formPanel.add(createLabelFor(deliveryModeCombo, "JMS Delivery Mode:"));
			formPanel.add(deliveryModeCombo);
			numRows++;

		}

		JPanel propertiesButtonPanel = createPropertiesButtonPanel();
		formPanel.add(createLabelFor(propertiesButtonPanel, "Custom Message Properties:"));
		formPanel.add(propertiesButtonPanel);
		numRows++;
		
		JPanel fileBrowsePanel = createFileBrowsePanel();
		JPanel typeMyOwnPanel = createTypeMyOwnPanel();
		
		JPanel radioPanel = createRadioButtonPanel(fileBrowsePanel, typeMyOwnPanel);
		formPanel.add(createLabelFor(radioPanel, "Message Content:"));
		formPanel.add(radioPanel);
		numRows++;
		
		SpringUtilities.makeCompactGrid(formPanel, 
				numRows, 2, 
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

	// allows subclasses to override this method and i.e. only show Topics
	protected TYPE[] getSupportedDestinationTypes() {
		List<TYPE> types = new ArrayList<>();

		if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_TOPIC)) {
			types.add(TYPE.TOPIC);
		}

		if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_QUEUE)) {
			types.add(TYPE.QUEUE);
		}

		return types.toArray(new TYPE[0]);
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
				properties = PropertiesDialog.editProperties(properties, predefinedPropertyNames);
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
		//TODO: make this more flexible
		typingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		typingArea.setCodeFoldingEnabled(true);
		
		// Set up drag & drop support for files
		new DropTarget(typingArea, new FileDropTargetListener(typingArea));
		
		RTextScrollPane scrollPane = new RTextScrollPane(typingArea);
		scrollPane.setLineNumbersEnabled(true);
		scrollPane.setHorizontalScrollBarPolicy(RTextScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(RTextScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
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
			    				  CoreConfiguration.PREF_BROWSE_DIRECTORY, 
			    				  ".")));
			      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			      int r = chooser.showOpenDialog(MessageSendTabPanel.this);
			      if (r == JFileChooser.APPROVE_OPTION) {
			        String fname = chooser.getSelectedFile().getPath();
			        filenameField.setText(fname);
			        
			        config.setUserPref(
			        		CoreConfiguration.PREF_BROWSE_DIRECTORY,
			        		chooser.getCurrentDirectory().getAbsolutePath());
			      }
			    
			}
		});
		CommonUITasks.makeSegmented(browseButton, Segmented.ONLY);
		panel.add(browseButton);
		
		return panel;
	}

	private JButton createSendButton() {
		sendButton = CommonUITasks.createButton("Send Message",
		new ActionListener() {
			String messageContent = null;
			
			public void actionPerformed(final ActionEvent e) {
				try {
					String filePath = filenameField.getText();
					
					int numberOfMessages = numberOfMessagesField.getValue();
					
					JMSDestination sendDestination = sendDestinationField.getDestination(
						sonic, (JMSBroker) brokerCombo.getSelectedItem());
					
					String jmsCorrIdValue = jmsCorrelationIDField==null? null: jmsCorrelationIDField.getText();

					Integer jmsPriorityValue = jmsPriorityField == null ? null : 
							Strings.isNullOrEmpty(jmsPriorityField.getText()) 
							? null : jmsPriorityField.getValue();
					
					JMSDestination jmsReplyToValue = jmsReplyToField==null? null: jmsReplyToField.getDestination(
						sonic, (JMSBroker) brokerCombo.getSelectedItem());
					
					long jmsTimeToLiveValue = jmsTTLField==null? 0: jmsTTLField.getValue();
										
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
								jmsPriorityValue,
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
							jmsPriorityValue,
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
		if(sonic.isFeatureSupported(JMSFeature.DESTINATION_TYPE_QUEUE)) {
			final JMSBroker broker = (JMSBroker) brokerCombo.getSelectedItem();

			worker.execute(taskFactory.enumerateQueues(broker, null));
		}
	};
	
	private int getDeliveryMode(){
		// if deliveryModeCombo is null, then the this is not visible, default to PERSISTENT
		if(deliveryModeCombo!=null){
			String item = (String)deliveryModeCombo.getSelectedItem();
			if(item.equalsIgnoreCase("NON-PERSISTENT"))
				return DeliveryMode.NON_PERSISTENT;
		}
		return  DeliveryMode.PERSISTENT;
	}
	
	/**
	 * @param queue
	 * @param number
	 * @param delay
	 * @param deliveryMode
	 * @param file
	 * @param jmsCorrelationIdFieldValue
	 * @param jmsPriorityValue
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
			final Integer jmsPriorityValue,
			final JMSDestination jmsReplyToValue,
			final Long jmsTimeToLive,
			final Map<String, ? extends Object> props) throws JMSException
	{
		Message message = prepMessage(
				jmsCorrelationIdFieldValue,
				jmsPriorityValue,
				jmsReplyToValue, 
				jmsTimeToLive);
		
		message.setJMSDeliveryMode(deliveryMode);
		
		Map<String, Object> propsCopy = CollectionFactory.newHashMap();
		propsCopy.putAll(props);
		
		// Special handling for some properties
		setSpecialJMSProperties(propsCopy, message);
		
		//Set custom properties
		setMessageProperties(propsCopy, message);
		
		// Send the file(list) and schedule a refresh of the destination table
		worker.executeInOrder(
			taskFactory.sendFile(queue, file, message, number, delay),
			taskFactory.enumerateQueues(queue.getBroker(), null));
	}

	/**
	 * @param queue
	 * @param number
	 * @param delay
	 * @param deliveryMode
	 * @param messageContent
	 * @param jmsCorrelationIdFieldValue
	 * @param jmsPriorityValue
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
			final Integer jmsPriorityValue,
			final JMSDestination jmsReplyToValue,
			final Long jmsTimeToLive,
			final Map<String, ? extends Object> props) throws JMSException
	{
		TextMessage message = (TextMessage)prepMessage(
				MessageFactory.createTextMessage(),
				jmsCorrelationIdFieldValue,
				jmsPriorityValue,
				jmsReplyToValue, 
				jmsTimeToLive);

		message.setText(messageContent);

		message.setJMSDeliveryMode(deliveryMode);
		
		Map<String, Object> propsCopy = CollectionFactory.newHashMap();
		propsCopy.putAll(props);
		
		// Special handling for some properties
		setSpecialJMSProperties(propsCopy, message);
		
		// Set custom properties
		setMessageProperties(propsCopy, message);
		
		// Create the tasks for sending and browsing messages
		worker.executeInOrder(
			taskFactory.sendMessage(queue, message, number, delay),
			taskFactory.enumerateQueues(queue.getBroker(), null));
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
	
	/**
	 * Get the topics from the configuration and polulate the topic list.
	 * 
	 * @param broker
	 */
	private void enumerateTopics(final JMSBroker broker) {
		destinationTable.updateData(getConfiguredTopics(broker));

		// optionally, enumerate topics
		worker.execute(taskFactory.enumerateTopics(broker, null));
	}
	
	/**
	 * Start a new queue enumeration task for the broker.
	 * 
	 * @param broker
	 */
	private void enumerateQueues(final JMSBroker broker) {		
		// Get the queue list from the broker
		worker.execute(taskFactory.enumerateQueues(broker, null));
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
			final Integer jmsPriorityValue,
			final JMSDestination jmsReplyToValue, 
			final Long jmsTimeToLive) throws JMSException {
		
		Message message = MessageFactory.createMessage();
		return prepMessage(message, jmsCorrelationIdFieldValue, jmsPriorityValue, jmsReplyToValue, jmsTimeToLive);
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
			final Integer jmsPriorityValue,
			final JMSDestination jmsReplyToValue, 
			final Long jmsTimeToLive) throws JMSException {
		
		if(jmsCorrelationIdFieldValue!=null)
			message.setJMSCorrelationID(jmsCorrelationIdFieldValue);
	
		if(jmsPriorityValue != null)
			message.setJMSPriority(jmsPriorityValue);
		
		if(jmsTimeToLive != null)
			message.setJMSExpiration(System.currentTimeMillis() + jmsTimeToLive*1000);
		
		if(jmsReplyToValue != null)
			message.setJMSReplyTo(jmsReplyToValue);
		
		return message;
	}

	private void setSpecialJMSProperties(final Map<String, Object> props, Message message) throws JMSException {
		for(Iterator<Map.Entry<String, Object>> it = props.entrySet().iterator(); it.hasNext();) {
			Entry<String, Object> entry = it.next();
			if("JMSType".equals(entry.getKey()) && entry.getValue() != null) {
				message.setJMSType(entry.getValue().toString());
				it.remove();
			}
		}
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
	
	@SuppressWarnings("unchecked")
	@Subscribe
	public void handleDomainEvent(final DomainEvent event) {
		switch(event.getId()) {
		
		case BROKERS_ENUMERATED:
			populateBrokerCombo((List<JMSBroker>)event.getInfo());
			break;

		case TOPICS_ENUMERATED:
		case QUEUES_ENUMERATED:
			List<JMSDestination> destinationList = (List<JMSDestination>)event.getInfo();
			if(destinationList.size() > 0 && destinationList.get(0).getBroker().equals(brokerCombo.getSelectedItem())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						destinationTable.updateData(destinationList);
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
	
	public String getUITabName() {
		return "Message Sender";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {ConnectionState.CONNECTED};
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
}
