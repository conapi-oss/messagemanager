package nl.queuemanager.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.Profile;
import nl.queuemanager.app.tasks.ActivateProfileTask;
import nl.queuemanager.app.tasks.TaskFactory;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.CommonUITasks;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.util.SingleExtensionFileFilter;

import com.google.common.base.Strings;

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

	private JButton activateProfileButton;
	
	private PlatformHelper platform;
	
	@Inject
	public ProfileTabPanel(final ProfileManager profileManager, final TaskExecutor worker, final TaskFactory taskFactory, final PlatformHelper platform, final CoreConfiguration config) {
		this.platform = platform;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.5, 0.5, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblHeader = new JLabel("Select a profile");
		lblHeader.setFont(lblHeader.getFont().deriveFont(32));
		GridBagConstraints gbc_lblHeader = new GridBagConstraints();
		gbc_lblHeader.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblHeader.anchor = GridBagConstraints.CENTER;
		gbc_lblHeader.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeader.gridx = 0;
		gbc_lblHeader.gridy = 0;
		add(lblHeader, gbc_lblHeader);
		
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

		final String lastActiveProfileId = config.getUserPref(ActivateProfileTask.LAST_ACTIVE_PROFILE, "");
		Profile lastActiveProfile = null;
		
		DefaultListModel<Profile> profilesModel = new DefaultListModel<Profile>();
		List<Profile> profiles = new ArrayList<Profile>(profileManager.getAllProfiles());
		Collections.sort(profiles);
		for(Profile profile: profiles) {
			profilesModel.addElement(profile);
			if(profile.getId().equals(lastActiveProfileId)) {
				lastActiveProfile = profile;
			}
		}
		profilesList = new JList<Profile>(profilesModel);
		profilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profilesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) { return; }
				selectedProfile = profilesList.getSelectedValue();
				displaySelectedProfile();
			}
		});
		profilesList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(value instanceof Profile) {
					final Profile profile = (Profile)value;
					setIcon(profile.getIcon());
					setToolTipText(profile.getName());
					setText(profile.getName());
				}
				
				return comp;
			}
		});
		scrollPane.setViewportView(profilesList);
		lblHeader.setLabelFor(profilesList);
				
		JLabel lblProfileName = new JLabel("Profile name");
		GridBagConstraints gbc_lblProfileName = new GridBagConstraints();
		gbc_lblProfileName.anchor = GridBagConstraints.LINE_START;
		gbc_lblProfileName.insets = new Insets(0, 0, 5, 0);
		gbc_lblProfileName.gridx = 2;
		gbc_lblProfileName.gridy = 1;
		add(lblProfileName, gbc_lblProfileName);
		
		txtProfileName = new JTextField();
		lblProfileName.setLabelFor(txtProfileName);
		GridBagConstraints gbc_txtProfileName = new GridBagConstraints();
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
				if(selectedProfile != null) {
					selectedProfile.setName(txtProfileName.getText());
				}
			}
		});
		
		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.LINE_START;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 0);
		gbc_lblDescription.gridx = 2;
		gbc_lblDescription.gridy = 3;
		add(lblDescription, gbc_lblDescription);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.weighty = 1.0;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 4;
		add(scrollPane_1, gbc_scrollPane_1);
		
		txtDescription = new JTextArea();
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		lblDescription.setLabelFor(txtDescription);
		scrollPane_1.setViewportView(txtDescription);
		txtDescription.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void updated(DocumentEvent e) {
				if(selectedProfile != null) {
					selectedProfile.setDescription(txtDescription.getText());
				}
			}
		});
		
		JLabel lblClasspath = new JLabel("Classpath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.anchor = GridBagConstraints.LINE_START;
		gbc_lblClasspath.insets = new Insets(0, 0, 5, 0);
		gbc_lblClasspath.gridx = 2;
		gbc_lblClasspath.gridy = 5;
		add(lblClasspath, gbc_lblClasspath);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.weightx = 1.0;
		gbc_scrollPane_3.weighty = 1.0;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 2;
		gbc_scrollPane_3.gridy = 6;
		add(scrollPane_3, gbc_scrollPane_3);
		
		classpathList = new JList<URL>(new DefaultListModel<URL>());
		classpathList.setCellRenderer(new DefaultListCellRenderer() {
			private final JFileChooser fileChooser = new JFileChooser();
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, final Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(value instanceof URL) {
					try {
						final File file = new File(((URL)value).toURI());
						if(file != null) {
							setIcon(fileChooser.getIcon(file));
							setText(file.getName() + (!file.exists()?" (missing)": ""));
							setToolTipText(file.getAbsolutePath());
							if(!file.exists()) {
								setForeground(Color.red);
							}
						}
					} catch (URISyntaxException e) {
						// Ok then, no icon for you!
						logger.log(Level.WARNING, String.format("Unable to resolve icon for %s", value), e);
					}
				}
				
				return comp;
			}
		});
		scrollPane_3.setViewportView(classpathList);
		
		Box classpathButtonsBox = Box.createHorizontalBox();
		GridBagConstraints gbc_horizontalBox = new GridBagConstraints();
		gbc_horizontalBox.anchor = GridBagConstraints.WEST;
		gbc_horizontalBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_horizontalBox.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalBox.gridx = 2;
		gbc_horizontalBox.gridy = 7;
		add(classpathButtonsBox, gbc_horizontalBox);
		
		btnAddClasspath = new JButton("Add jar");
		classpathButtonsBox.add(btnAddClasspath);
		CommonUITasks.makeSegmented(btnAddClasspath, Segmented.FIRST);
		btnAddClasspath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addJar(selectedProfile);
				displaySelectedProfile();
			}
		});
		
		btnRemoveClasspath = new JButton("Remove jar");
		classpathButtonsBox.add(btnRemoveClasspath);
		CommonUITasks.makeSegmented(btnRemoveClasspath, Segmented.LAST);
		btnRemoveClasspath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<URL> urls = classpathList.getSelectedValuesList();
				
				for(URL url: urls) {
					((DefaultListModel<URL>)classpathList.getModel()).removeElement(url);
					selectedProfile.getClasspath().remove(url);
				}
				
				displaySelectedProfile();
			}
		});
		
		Component horizontalGlue = Box.createHorizontalGlue();
		classpathButtonsBox.add(horizontalGlue);
		
		Box profileButtonsBox = Box.createHorizontalBox();
		GridBagConstraints gbc_profileButtonsBox = new GridBagConstraints();
		gbc_profileButtonsBox.anchor = GridBagConstraints.LINE_START;
		gbc_profileButtonsBox.insets = new Insets(0, 0, 0, 5);
		gbc_profileButtonsBox.gridx = 0;
		gbc_profileButtonsBox.gridy = 8;
		add(profileButtonsBox, gbc_profileButtonsBox);
		
		JButton duplicateProfileButton = new JButton("Duplicate");
		CommonUITasks.makeSegmented(duplicateProfileButton, Segmented.FIRST);
		duplicateProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile source = profilesList.getSelectedValue();
				Profile profile = Profile.copyOf(source);
				
				((DefaultListModel<Profile>)profilesList.getModel()).insertElementAt(profile, profilesList.getSelectedIndex()+1);
				profileManager.putProfileIfNotExist(profile);
				profileManager.tryToSaveProfile(profile);
				
				profilesList.setSelectedValue(profile, true);
			}
		});
		profileButtonsBox.add(duplicateProfileButton);
		
		JButton removeProfileButton = new JButton("Remove");
		CommonUITasks.makeSegmented(removeProfileButton, Segmented.LAST);
		removeProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile toRemove = profilesList.getSelectedValue();
				if(toRemove != null) {
					((DefaultListModel<Profile>)profilesList.getModel()).removeElement(toRemove);
					profileManager.removeProfile(toRemove);
				}
			}
		});
		profileButtonsBox.add(removeProfileButton);
		profileButtonsBox.add(Box.createHorizontalGlue());
		
		activateProfileButton = new JButton("Activate profile");
		activateProfileButton.setHorizontalAlignment(SwingConstants.RIGHT);
		activateProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectedProfile == null) { return; }

				worker.execute(taskFactory.activateProfile(selectedProfile));
			}
		});
		GridBagConstraints gbc_activateProfileButton = new GridBagConstraints();
		gbc_activateProfileButton.anchor = GridBagConstraints.LINE_END;
		gbc_activateProfileButton.gridx = 2;
		gbc_activateProfileButton.gridy = 8;
		
		add(activateProfileButton, gbc_activateProfileButton);
		
		// If we have a saved profile, select that
		if(lastActiveProfile != null) {
			selectedProfile = lastActiveProfile;
			profilesList.setSelectedValue(lastActiveProfile, true);
		}
		
		// Update the UI state
		displaySelectedProfile();
	}
	
	public JButton getDefaultButton() {
		return activateProfileButton;
	}
	
	private void displaySelectedProfile() {
		if(selectedProfile != null) {
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
			if(selectedProfile.getClasspath().size() > 0) {
				activateProfileButton.setEnabled(true);
				activateProfileButton.setToolTipText("");
			} else {
				activateProfileButton.setEnabled(false);
				activateProfileButton.setToolTipText("Please set the classpath before activating this profile");
			}
		} else {
			txtProfileName.setText("");
			txtDescription.setText("");
	
			txtProfileName.setEnabled(false);
			txtDescription.setEnabled(false);

			classpathList.setEnabled(false);
			
			btnAddClasspath.setEnabled(false);
			btnRemoveClasspath.setEnabled(false);
			activateProfileButton.setEnabled(false);
		}
	}
	
	private void addJar(Profile profile) {
		final File[] files = platform.chooseFiles(this, "Add to classpath", true, new SingleExtensionFileFilter("jar", "Java Archive File"));
		final Set<File> expandedFiles = new HashSet<File>();
				
		// Expand the files list with the Class-Path: entries from the selected jar manifests and 
		// any jars the profile suggests we add (if found).
		int oldSize = 0;
		do {
			oldSize = expandedFiles.size();
			
			for(File file: files) {
				// Add the user selected file to the set
				expandedFiles.add(file);
				
				// Find and add profile-suggested jars that can be found relative to the current jar
				for(String suggestion: profile.getJars()) {
					File suggestionFile = new File(file.getParentFile(), suggestion);
					if(suggestionFile.exists()) {
						expandedFiles.add(suggestionFile);
					}
				}
				
				// Find and add any jars referenced by Class-Path of the current jars
				try(JarFile jarFile = new JarFile(file)) {
					Attributes attr = jarFile.getManifest().getMainAttributes();
					if(attr != null) {
						String classpath = attr.getValue("Class-Path");
						if(!Strings.isNullOrEmpty(classpath)) {
							String[] names = classpath.split("\\s");
							for(String name: names) {
								File extraJar = new File(file.getParentFile(), name);
								if(extraJar.exists()) {
									expandedFiles.add(extraJar);
								}
							}
						}
					}
				} catch (IOException e) {
					logger.log(Level.WARNING, "IOException while reading jar", e);
				}
			}
			// Keep going until the Set of Files doesn't get bigger anymore
		} while(oldSize != expandedFiles.size());
		
		for(File file: expandedFiles) {
			try {
				URL url = file.toURI().toURL();
				profile.getClasspath().add(url);
				((DefaultListModel<URL>) classpathList.getModel()).addElement(url);
			} catch (MalformedURLException ex) {
				logger.log(Level.WARNING, "Exception while choosing file", ex);
			}
		}
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
