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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.ui.message.MessagePropertiesTable;
import nl.queuemanager.ui.util.SpringUtilities;

@SuppressWarnings("serial")
public class PropertiesPanel extends JPanel {
	private static final String[] PROPTYPES = { 
		String.class.getSimpleName(),
		Boolean.class.getSimpleName(), 
		Byte.class.getSimpleName(), 
		Short.class.getSimpleName(), 
		Integer.class.getSimpleName(), 
		Long.class.getSimpleName(), 
		Float.class.getSimpleName(), 
		Double.class.getSimpleName()};
	
	// TODO Replace these constants with some kind of provider-dependent list
	private static final String[] PATTERNS = {
			"JMSType",
            "JMS_SonicMQ_preserveUndelivered",
            "JMS_SonicMQ_notifyUndelivered",
            "JMS_SonicMQ_destinationUndelivered"};

	private final MessagePropertiesTable messagePropertiesTable;
	private final JComboBox propNameCombo;
	private final JTextField propValueField;
	private final JComboBox propTypeCombo;
	private final Map<String, Object> properties;

	public PropertiesPanel(final Map<String, Object> properties) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		/**
		 * The upper panel (edit properties)
		 * */

		JPanel propertiesEditPanel = new JPanel();
		propertiesEditPanel.setLayout(new SpringLayout());
		propertiesEditPanel.setBorder(BorderFactory.createTitledBorder("Property"));
		
		JLabel propNameLabel = new JLabel(" Name: ");
		propertiesEditPanel.add(propNameLabel);
		propNameCombo = createPropNameCombo();
		propNameCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, propNameCombo.getPreferredSize().height));
		propertiesEditPanel.add(propNameCombo);
		final JButton setButton = new JButton();
		setButton.setText("Set");
		setButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(propNameCombo.getSelectedItem() == null)
					return;
				
				if(propValueField.getText() == null)
					return;
				
				final String propertyName = propNameCombo.getSelectedItem().toString().trim();
				final String propertyValue = propValueField.getText().trim();
				final String propertyType = propTypeCombo.getSelectedItem().toString();
				
				if (propertyName.equals("") || propertyValue.equals(""))
					return;
				
				try {
					properties.put(propertyName, createPropertyObject(propertyType, propertyValue));
					refreshTable();
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(getParent(), 
							String.format(
									"The value '%s' cannot be used for a property of type %s", 
									propertyValue, propertyType));
				}
			}
		});
		propertiesEditPanel.add(setButton);
		
		JLabel propValueLabel = new JLabel(" Value: ");
		propValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		propertiesEditPanel.add(propValueLabel);
		propValueField = new JTextField();
		propValueField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 	propValueField.getPreferredSize().height));
		propValueField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
					setButton.doClick();
				}
				
				super.keyPressed(e);
			}
		});
		propertiesEditPanel.add(propValueField);
		propertiesEditPanel.add(new JLabel());	// empty label to fill in the gap
		
		JLabel propTypeLabel = new JLabel(" Type: ");
		propertiesEditPanel.add(propTypeLabel);
		propTypeCombo = createPropTypeCombo();
		propTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, propTypeCombo.getPreferredSize().height));
		propertiesEditPanel.add(propTypeCombo);
		final JButton deleteButton = new JButton();
		deleteButton.setText("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				properties.remove(propNameCombo.getSelectedItem());
				refreshTable();
			}
		});
		propertiesEditPanel.add(deleteButton);
		
		SpringUtilities.makeCompactGrid(propertiesEditPanel, 
				3, 3, 
				0, 0, 
				5, 5);
		panel.add(propertiesEditPanel);
		
		/**
		 * Lower panel (the Table)
		 * */
		
		messagePropertiesTable = new MessagePropertiesTable();
		refreshTable();
		
		ListSelectionModel selectionModel = messagePropertiesTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				else {
					propNameCombo.setSelectedItem(messagePropertiesTable.getModel().getValueAt(messagePropertiesTable.getSelectedRow(), 0));
					propValueField.setText((String)messagePropertiesTable.getModel().getValueAt(messagePropertiesTable.getSelectedRow(), 1));
					propTypeCombo.setSelectedItem(messagePropertiesTable.getModel().getValueAt(messagePropertiesTable.getSelectedRow(), 2));
				}
			}
		});
		
		JScrollPane propertiesTableScrollPane = new JScrollPane(messagePropertiesTable,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		propertiesTableScrollPane.setPreferredSize(new Dimension(450, 150));
		propertiesTableScrollPane.setViewportView(messagePropertiesTable);
		
		JPanel propertiesTablePanel = new JPanel();
		propertiesTablePanel.setLayout(new SpringLayout());
		propertiesTablePanel.setBorder(BorderFactory.createTitledBorder("Properties"));
		propertiesTablePanel.add(propertiesTableScrollPane);
		
		final JButton clearButton = new JButton();
		clearButton.setText("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				properties.clear();
				refreshTable();
			}
		});
		propertiesTablePanel.add(clearButton);
		
		SpringUtilities.makeCompactGrid(propertiesTablePanel, 
				1, 2, 
				0, 0, 
				5, 5);
		
		panel.add(propertiesTablePanel);
		
		panel.add(new Box.Filler(
				new Dimension(5, 5),
				new Dimension(5, 5),
				new Dimension(5, 5)));
		add(panel);
		
		this.properties = properties;
	}
	
	public void refreshTable(){
		messagePropertiesTable.setProperties(properties);
	}
	
	private JComboBox createPropNameCombo() {
		JComboBox cmb = new JComboBox(PATTERNS);
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setSelectedIndex(-1);
		cmb.setEditable(true);
		cmb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		return cmb;
	}

	private JComboBox createPropTypeCombo() {
		JComboBox cmb = new JComboBox(PROPTYPES);
		cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		cmb.setSelectedIndex(0);
		cmb.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
		return cmb;
	}
			
	private Object createPropertyObject(String type, String value) throws NumberFormatException {
		if (type.equalsIgnoreCase(Boolean.class.getSimpleName())){
			if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
				return Boolean.parseBoolean(value);
			else throw new NumberFormatException("Only 'true' or 'false' is allowed");
		}
		if (type.equalsIgnoreCase(Byte.class.getSimpleName()))
			return Byte.parseByte(value);
		if (type.equalsIgnoreCase(Short.class.getSimpleName()))
			return Short.parseShort(value);
		if (type.equalsIgnoreCase(Integer.class.getSimpleName()))
			return Integer.parseInt(value);
		if (type.equalsIgnoreCase(Long.class.getSimpleName()))
			return Long.parseLong(value);
		if (type.equalsIgnoreCase(Float.class.getSimpleName()))
			return Float.parseFloat(value);
		if (type.equalsIgnoreCase(Double.class.getSimpleName()))
			return Double.parseDouble(value);
		return value;
	}
	
}
