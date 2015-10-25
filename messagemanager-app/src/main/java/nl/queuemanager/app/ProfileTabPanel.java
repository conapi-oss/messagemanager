package nl.queuemanager.app;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.app.tasks.TaskFactory;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;

@SuppressWarnings("serial")
public class ProfileTabPanel extends JPanel implements UITab {
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private JTextField txtProfileName;
	
	private Profile selectedProfile;
	private JList<Profile> profilesList;
	private JButton btnRemoveClasspath;
	private JButton btnAddClasspath;
	private JTextArea txtDescription;
	private JList<URL> classpathList;
	
	@Inject
	public ProfileTabPanel(final ProfileManager profileManager, final TaskExecutor worker, final TaskFactory taskFactory) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{81, 88, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblAvailableProfiles = new JLabel("Available profiles");
		GridBagConstraints gbc_lblAvailableProfiles = new GridBagConstraints();
		gbc_lblAvailableProfiles.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableProfiles.anchor = GridBagConstraints.WEST;
		gbc_lblAvailableProfiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableProfiles.gridx = 0;
		gbc_lblAvailableProfiles.gridy = 0;
		add(lblAvailableProfiles, gbc_lblAvailableProfiles);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 7;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		DefaultListModel<Profile> profilesModel = new DefaultListModel<Profile>();
		for(Profile profile: profileManager.getAllProfiles()) {
			profilesModel.addElement(profile);
		}
		profilesList = new JList<Profile>(profilesModel);
		profilesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) { return; }
				selectedProfile = profilesList.getSelectedValue();
				displaySelectedProfile();
			}
		});
		scrollPane.setViewportView(profilesList);
		lblAvailableProfiles.setLabelFor(profilesList);
		
		JLabel lblProfileName = new JLabel("Profile name");
		GridBagConstraints gbc_lblProfileName = new GridBagConstraints();
		gbc_lblProfileName.anchor = GridBagConstraints.LINE_START;
		gbc_lblProfileName.insets = new Insets(0, 0, 5, 5);
		gbc_lblProfileName.gridx = 2;
		gbc_lblProfileName.gridy = 1;
		add(lblProfileName, gbc_lblProfileName);
		
		txtProfileName = new JTextField();
		lblProfileName.setLabelFor(txtProfileName);
		GridBagConstraints gbc_txtProfileName = new GridBagConstraints();
		gbc_txtProfileName.gridwidth = 2;
		gbc_txtProfileName.anchor = GridBagConstraints.LINE_START;
		gbc_txtProfileName.insets = new Insets(0, 0, 5, 0);
		gbc_txtProfileName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtProfileName.gridx = 2;
		gbc_txtProfileName.gridy = 2;
		add(txtProfileName, gbc_txtProfileName);
		txtProfileName.setColumns(50);
		txtProfileName.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void updated(DocumentEvent e) {
				selectedProfile.setName(txtProfileName.getText());
			}
		});
		txtProfileName.setEnabled(false);
		
		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.LINE_START;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 2;
		gbc_lblDescription.gridy = 3;
		add(lblDescription, gbc_lblDescription);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 1.0;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 4;
		add(scrollPane_1, gbc_scrollPane_1);
		
		txtDescription = new JTextArea();
		txtDescription.setEnabled(false);
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		lblDescription.setLabelFor(txtDescription);
		scrollPane_1.setViewportView(txtDescription);
		txtDescription.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void updated(DocumentEvent e) {
				selectedProfile.setDescription(txtDescription.getText());
			}
		});
		
		JLabel lblClasspath = new JLabel("Classpath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.anchor = GridBagConstraints.LINE_START;
		gbc_lblClasspath.insets = new Insets(0, 0, 5, 5);
		gbc_lblClasspath.gridx = 2;
		gbc_lblClasspath.gridy = 5;
		add(lblClasspath, gbc_lblClasspath);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.weightx = 1.0;
		gbc_scrollPane_3.weighty = 1.0;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.gridwidth = 2;
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 2;
		gbc_scrollPane_3.gridy = 6;
		add(scrollPane_3, gbc_scrollPane_3);
		
		classpathList = new JList<URL>(new DefaultListModel<URL>());
		scrollPane_3.setViewportView(classpathList);
		classpathList.setEnabled(false);
		
		btnAddClasspath = new JButton("Add jar");
		btnAddClasspath.setEnabled(false);
		btnAddClasspath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Use java.awt.FileDialog so we can have native dialogs
				FileDialog fd = new java.awt.FileDialog((java.awt.Frame) null);
				fd.setMultipleMode(true);
				fd.setVisible(true);
				
				File[] files = fd.getFiles();
				for(File file: files) {
					try {
						URL url = file.toURI().toURL();
						selectedProfile.getClasspath().add(url);
						((DefaultListModel<URL>) classpathList.getModel()).addElement(url);
					} catch (MalformedURLException ex) {
						logger.log(Level.WARNING, "Exception while choosing file", ex);
					}
				}
			}
		});
		
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.LINE_START;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 7;
		add(btnAddClasspath, gbc_btnAdd);
		
		btnRemoveClasspath = new JButton("Remove jar");
		btnRemoveClasspath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URL url = classpathList.getSelectedValue();
				((DefaultListModel<URL>)classpathList.getModel()).removeElement(url);
				selectedProfile.getClasspath().remove(url);
			}
		});
		btnRemoveClasspath.setEnabled(false);
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.anchor = GridBagConstraints.LINE_START;
		gbc_btnRemove.gridx = 3;
		gbc_btnRemove.gridy = 7;
		add(btnRemoveClasspath, gbc_btnRemove);
		
		JButton duplicateProfileButton = new JButton("Duplicate");
		duplicateProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile source = profilesList.getSelectedValue();
				
				Profile profile = new Profile();
				profile.setName(source + " (copy)");
				profile.setDescription(source.getDescription());
				profile.getPlugins().addAll(source.getPlugins());
				profile.getClasspath().addAll(source.getClasspath());
				
				((DefaultListModel<Profile>)profilesList.getModel()).insertElementAt(profile, profilesList.getSelectedIndex()+1);
				profileManager.putProfileIfNotExist(profile);
				profileManager.tryToSaveProfile(profile);
				
				profilesList.setSelectedValue(profile, true);
			}
		});
		GridBagConstraints gbc_duplicateProfileButton = new GridBagConstraints();
		gbc_duplicateProfileButton.anchor = GridBagConstraints.LINE_START;
		gbc_duplicateProfileButton.insets = new Insets(0, 0, 0, 5);
		gbc_duplicateProfileButton.gridx = 0;
		gbc_duplicateProfileButton.gridy = 8;
		add(duplicateProfileButton, gbc_duplicateProfileButton);
		
		JButton removeProfileButton = new JButton("Remove");
		removeProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO inform ProfileManager. Perhaps replace the line below with a reactive substitute (automatically update
				// the UI list based on the list in the ProfileManager).
				((DefaultListModel<Profile>)profilesList.getModel()).removeElement(profilesList.getSelectedValue());
			}
		});
		GridBagConstraints gbc_removeProfileButton = new GridBagConstraints();
		gbc_removeProfileButton.anchor = GridBagConstraints.LINE_START;
		gbc_removeProfileButton.insets = new Insets(0, 0, 0, 5);
		gbc_removeProfileButton.gridx = 1;
		gbc_removeProfileButton.gridy = 8;
		add(removeProfileButton, gbc_removeProfileButton);
		
		JButton activateProfileButton = new JButton("Activate profile");
		activateProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectedProfile == null) { return; }

				worker.execute(taskFactory.activateProfile(selectedProfile));
			}
		});
		GridBagConstraints gbc_activateProfileButton = new GridBagConstraints();
		gbc_activateProfileButton.anchor = GridBagConstraints.LINE_END;
		gbc_activateProfileButton.gridx = 3;
		gbc_activateProfileButton.gridy = 8;
		
		add(activateProfileButton, gbc_activateProfileButton);
	}
	
	private void displaySelectedProfile() {
		txtProfileName.setText(selectedProfile.getName());
		txtDescription.setText(selectedProfile.getDescription());

		txtProfileName.setEnabled(true);
		txtDescription.setEnabled(true);

		classpathList.setEnabled(true);
		((DefaultListModel<URL>)classpathList.getModel()).clear();
		for(URL url: selectedProfile.getClasspath()) {
			((DefaultListModel<URL>)classpathList.getModel()).addElement(url);
		}
		
		btnAddClasspath.setEnabled(true);
		btnRemoveClasspath.setEnabled(true);
	}
		
	public String getUITabName() {
		return "Profile";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	@Override
	public String toString() {
		return getUITabName();
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
			//ConnectionState.CONNECTED,
			ConnectionState.DISCONNECTED
		};
	}

}
