package nl.queuemanager.ui.util;

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSDestination.TYPE;
import nl.queuemanager.ui.JMSDestinationInfo;
import nl.queuemanager.ui.JMSDestinationInfoTransferable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;

public class JMSDestinationField extends Box {
		private final JTextField nameField;
		private final JComboBox<?> typeField;
		
		public JMSDestinationField() {
			super(BoxLayout.X_AXIS);
			nameField = new JTextField();
			nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
					(int)nameField.getPreferredSize().getHeight()));
			typeField = new JComboBox<Object>(new TYPE[] {
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
			if(destination != null) {
				this.nameField.setText(destination.getName());
				this.typeField.setSelectedItem(destination.getType());
			} else {
				this.nameField.setText("");
				this.typeField.setSelectedItem(JMSDestination.TYPE.QUEUE);
			}
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