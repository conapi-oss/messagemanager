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
import nl.queuemanager.core.ESBMessage;
import nl.queuemanager.core.MessageManagerMessage;
import nl.queuemanager.core.Pair;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.JMSFeature;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.HighlightsModel;
import nl.queuemanager.ui.util.SingleExtensionFileFilter;

import javax.jms.Message;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class CommonUITasks {


	/**
	 * Button segment positions for Mac OS X. These constants are the positions a
	 * button can have within a segmented button "bar".
	 * 
	 * @author Gerco Dries (gdr@progaia-rs.nl)
	 */
	public enum Segmented {ONLY, FIRST, MIDDLE, LAST}		
	
	/**
	 * Invoke the "clear()" method on the subject on the EDT
	 * 
	 * @param subject
	 */
	public static void clear(final Clearable subject) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				subject.clear();
			}
		});
	}

	/**
	 * Update a combo box to contain exactly the given (sorted) items while
	 * keeping the current selection, without firing any selection events.
	 * Domains may publish a refreshed broker list while the user is working;
	 * rebuilding the combo would fire selection events with disruptive side
	 * effects (tables cleared, connects and enumerations triggered, prepared
	 * sends redirected).
	 * <p>
	 * Must be called on the EDT.
	 *
	 * @return true when the combo was updated in place. False when the caller
	 *         should fall back to a full rebuild: nothing is selected, or the
	 *         selected item is not in the new list.
	 */
	public static <T> boolean updateComboItems(final JComboBox<T> combo, final List<T> sortedItems) {
		Object selected = combo.getSelectedItem();
		if(selected == null || !sortedItems.contains(selected))
			return false;

		// Remove items that are no longer present (never the selected one)
		for(int i = combo.getItemCount() - 1; i >= 0; i--) {
			if(!sortedItems.contains(combo.getItemAt(i))) {
				combo.removeItemAt(i);
			}
		}

		// Insert new items at their position in the sorted list. The remaining
		// combo items are a subsequence of sortedItems, so the insertion index
		// is right after the last combo item that precedes the new item.
		for(int newIndex = 0; newIndex < sortedItems.size(); newIndex++) {
			T item = sortedItems.get(newIndex);

			boolean present = false;
			int insertAt = 0;
			for(int i = 0; i < combo.getItemCount(); i++) {
				T existing = combo.getItemAt(i);
				if(item.equals(existing)) {
					present = true;
					break;
				}
				if(sortedItems.indexOf(existing) < newIndex) {
					insertAt = i + 1;
				}
			}

			if(!present) {
				combo.insertItemAt(item, insertAt);
			}
		}

		return true;
	}

	/**
	 * Create a JButton with some standard settings and an actionListener.
	 * 
	 * @param caption
	 * @param actionListener
	 * @return
	 */
	public static JButton createButton(String caption, ActionListener actionListener) {
		final JButton button = new JButton();
		button.setText(caption);
		button.setMinimumSize(new Dimension(80, 30));
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.addActionListener(actionListener);
		return button;
	}	
	
	/**
	 * Makes a JButton a segmented button on Mac OS X.
	 * 
	 * @param button
	 * @param position first, middle, last or only
	 */
	public static void makeSegmented(JButton button, Segmented position) {
		button.putClientProperty("JButton.buttonType", "segmented");
		button.putClientProperty("JButton.segmentPosition", position.name().toLowerCase());
	}
	
	/**
	 * Ask the user for a save location and save the message to file (or directory).
	 * 
	 * @param parent The parent GUI component for the dialog boxes
	 * @param messagesToSave The messages to save
	 */
	public static void saveMessages(Component parent, List<Message> messagesToSave, TaskExecutor worker, TaskFactory taskFactory, CoreConfiguration config) {
		final FileFilter contentFileFilter = new FileNameExtensionFilter("Message Content Only (*.xml, *.txt, *.json, *.bin)", "xml", "txt", "json", "bin");
		final FileFilter mmmsgFileFilter = new SingleExtensionFileFilter(MessageManagerMessage.getFileExtension(), "Message Manager - Message File");
		final FileFilter esbmsgFileFilter = new SingleExtensionFileFilter(ESBMessage.getFileExtension(), "ESB Message File");
		String messageFileExtension = null;


		int numMessages = messagesToSave.size();
		if(numMessages == 0)
			return;

		List<Pair<Message, File>> messages = CollectionFactory.newArrayList();

		// Initialize the JFileChooser
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(
				config.getUserPref(CoreConfiguration.PREF_SAVE_DIRECTORY, ".")));
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(contentFileFilter);
		chooser.addChoosableFileFilter(mmmsgFileFilter);
		chooser.addChoosableFileFilter(esbmsgFileFilter);
		chooser.setAcceptAllFileFilterUsed(true);
		// setAcceptAllFileFilterUsed() silently selects the first added filter, so choose the default explicitly
		chooser.setFileFilter(contentFileFilter);

		if(numMessages == 1) {
			// Display a single file chooser
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
				return;

			// Now put the File and the selected Message into a Pair
			File selectedFile = chooser.getSelectedFile();
			Message selectedMessage = messagesToSave.get(0);

			if(selectedMessage != null && selectedFile != null) {
				config.setUserPref(
						CoreConfiguration.PREF_SAVE_DIRECTORY,
						chooser.getCurrentDirectory().getAbsolutePath());
				messages.add(Pair.create(selectedMessage, selectedFile));
				messageFileExtension = messageFileExtensionFor(
						selectedFile.getName(), chooser.getFileFilter(), mmmsgFileFilter, esbmsgFileFilter);
			}
		} else if(numMessages >= 2) {
			// Display a directory chooser
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
				return;

			File selectedDir = chooser.getSelectedFile();
			if(selectedDir == null)
				return;

			config.setUserPref(
					CoreConfiguration.PREF_SAVE_DIRECTORY,
					selectedDir.getAbsolutePath());

			for(Message m: messagesToSave) {
				messages.add(Pair.create(m, selectedDir));
			}

			// Directory names say nothing about the format, only the filter counts
			messageFileExtension = messageFileExtensionFor(
					null, chooser.getFileFilter(), mmmsgFileFilter, esbmsgFileFilter);
		}

		if(messages.size() > 0) {
			worker.execute(taskFactory.saveToFile(messages, messageFileExtension));
		}
	}

	/**
	 * Decide the file format for saving. An extension typed by the user wins over the
	 * selected filter, so "order.xml" is always saved as the plain message content.
	 *
	 * @param fileName The file name entered by the user, or null when saving to a directory
	 * @return The message file extension to save as, or null to save the plain message content
	 */
	static String messageFileExtensionFor(String fileName, FileFilter selectedFilter, FileFilter mmmsgFileFilter, FileFilter esbmsgFileFilter) {
		if(fileName != null) {
			String name = fileName.toLowerCase();
			if(name.endsWith(ESBMessage.getFileExtension()))
				return ESBMessage.getFileExtension();
			if(name.endsWith(MessageManagerMessage.getFileExtension()))
				return MessageManagerMessage.getFileExtension();
			if(name.indexOf('.') != -1)
				return null;
		}

		if(selectedFilter == esbmsgFileFilter)
			return ESBMessage.getFileExtension();
		if(selectedFilter == mmmsgFileFilter)
			return MessageManagerMessage.getFileExtension();
		return null;
	}


	// Static method to center any new JFrame or JDialog.
	public static void centerWindow(Window window){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		
		int x = (screenSize.width - window.getWidth())/2;
		int y = (screenSize.height - window.getHeight())/2;
		window.setLocation(x, y);
	}

	public static MessagesTable createMessageTable(MessageHighlighter messageHighlighter, EventBus eventBus, JMSDomain domain, MessageTableActions actions) {
		// Create the message table
		MessagesTable table = new MessagesTable();
		MessagesTable.MessageTableModel tableModel = (MessagesTable.MessageTableModel) table.getModel();
		table.setHighlightsModel(new HighlightsModel<>(tableModel, messageHighlighter));

		eventBus.register(new Object() {
			@Subscribe
			public void onSearchModeChangedEvent(SearchModeChangedEvent e) {
				if(e.getSource().equals(table)) {
					// this is coming from the search panel below this message table
					table.setFiltering(e.getMode() != SearchModeChangedEvent.SearchMode.NO_FILTER, e.getMode() == SearchModeChangedEvent.SearchMode.INVERSE_FILTER);
					table.resetHighlights();
				}
			}
		});

		// do not show the correlation id column if the feature is not supported
		if(!domain.isFeatureSupported(JMSFeature.JMS_HEADERS)) {
			table.removeColumn(table.getColumnModel().getColumn(2));
		}

		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				actions.displaySelectedMessage();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_DELETE) {
					actions.deleteSelectedMessages();
				} else {
					super.keyTyped(e);
				}
			}
		});

		table.setDragEnabled(true);

		return table;
	}
}
