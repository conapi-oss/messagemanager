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
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.Clearable;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.DocumentAdapter;
import nl.queuemanager.ui.util.SingleExtensionFileFilter;

import javax.jms.Message;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
		chooser.addChoosableFileFilter(mmmsgFileFilter);
		chooser.addChoosableFileFilter(esbmsgFileFilter);
		chooser.setAcceptAllFileFilterUsed(true);

		if(numMessages == 1) {
			// Display a single file chooser
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.showSaveDialog(parent);			
			
			// Now put the File and the selected Message into a Pair
			File selectedFile = chooser.getSelectedFile();
			Message selectedMessage = messagesToSave.get(0);
			
			if(selectedMessage != null && selectedFile != null) {
				config.setUserPref(
						CoreConfiguration.PREF_SAVE_DIRECTORY, 
						chooser.getCurrentDirectory().getAbsolutePath());
				messages.add(Pair.create(selectedMessage, selectedFile));
				if(selectedFile.getName().toLowerCase().endsWith(ESBMessage.getFileExtension()) ||
					chooser.getFileFilter() == esbmsgFileFilter){
					messageFileExtension = ESBMessage.getFileExtension();
				}
				else if(selectedFile.getName().toLowerCase().endsWith(MessageManagerMessage.getFileExtension()) ||
						chooser.getFileFilter() == mmmsgFileFilter){
					messageFileExtension = MessageManagerMessage.getFileExtension();
				}
			}
		} else if(numMessages >= 2) {
			// Display a directory chooser
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.showSaveDialog(parent);			
			
			File selectedDir = chooser.getSelectedFile();
			if(selectedDir == null)
				return;
			
			config.setUserPref(
					CoreConfiguration.PREF_SAVE_DIRECTORY, 
					selectedDir.getAbsolutePath());

			for(Message m: messagesToSave) {
				messages.add(Pair.create(m, selectedDir));
			}

			if(chooser.getFileFilter() == esbmsgFileFilter){
				messageFileExtension = ESBMessage.getFileExtension();
			}
			else if(chooser.getFileFilter() == mmmsgFileFilter){
				messageFileExtension = MessageManagerMessage.getFileExtension();
			}
		}
		
		if(messages.size() > 0) {
			worker.execute(taskFactory.saveToFile(messages, messageFileExtension));
		}
	}
	
	public static JTextField createSearchField(final EventBus eventBus) {
		final AtomicBoolean publishSearch = new AtomicBoolean(true);
		final JTextField searchField = new JTextField();
		searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));
		searchField.putClientProperty("JTextField.variant", "search");
		searchField.setToolTipText("Type to search");

		searchField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void updated(DocumentEvent e) {
				if(!publishSearch.get()) return;
				
				try {
					int length = e.getDocument().getLength();
					String text = e.getDocument().getText(0, length);
					eventBus.post(new GlobalHighlightEvent(searchField, text));
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		});
		eventBus.register(new Object() {
			@Subscribe
			public void onGlobalHighlightEvent(GlobalHighlightEvent e) {
				if(e.getSource() != searchField) {
					try {
						publishSearch.set(false);
						searchField.setText(e.getHighlightString());
					} finally {
						publishSearch.set(true);
					}
				}
			}
		});
		return searchField;
	}

	// Static method to center any new JFrame or JDialog.
	public static void centerWindow(Window window){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		
		int x = (screenSize.width - window.getWidth())/2;
		int y = (screenSize.height - window.getHeight())/2;
		window.setLocation(x, y);
	}
}
