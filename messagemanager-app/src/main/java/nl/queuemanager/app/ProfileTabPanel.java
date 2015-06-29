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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.queuemanager.ConnectivityProviderPlugin;
import nl.queuemanager.core.CoreModule;
import nl.queuemanager.ui.UIModule;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.util.TableColumnAdjuster;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ProfileTabPanel extends JPanel implements UITab {

	private final Injector parentInjector;
	private final EventBus eventBus;
	private final PluginManager pluginManager;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	private JTextField txtProfileName;
	private JTable pluginTable;
	private TableColumnAdjuster pluginTableAdjuster;
	
	private Profile selectedProfile;
	private JList<Profile> profilesList;
	private JButton btnRemoveClasspath;
	private JButton btnAddClasspath;
	private JTextArea txtDescription;
	private JList<URL> classpathList;
	private JButton addPluginButton;
	private JButton removePluginButton;
	
	@Inject
	public ProfileTabPanel(final Injector injector, final EventBus eventBus, final PluginManager pluginManager) {
		this.parentInjector = injector;
		this.eventBus = eventBus;
		this.pluginManager = pluginManager;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{81, 88, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblAvailableProfiles = new JLabel("Available profiles");
		GridBagConstraints gbc_lblAvailableProfiles = new GridBagConstraints();
		gbc_lblAvailableProfiles.anchor = GridBagConstraints.LINE_START;
		gbc_lblAvailableProfiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableProfiles.gridx = 0;
		gbc_lblAvailableProfiles.gridy = 0;
		add(lblAvailableProfiles, gbc_lblAvailableProfiles);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridwidth = 2;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.gridx = 2;
		gbc_scrollPane_2.gridy = 6;
		add(scrollPane_2, gbc_scrollPane_2);
		
		pluginTable = new JTable();
		scrollPane_2.setViewportView(pluginTable);
		pluginTable.setModel(new PluginListTableModel());
		pluginTableAdjuster = new TableColumnAdjuster(pluginTable);
		pluginTableAdjuster.adjustColumns();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 10;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		profilesList = new JList<Profile>(new DefaultListModel<Profile>());
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
		txtProfileName.setColumns(10);
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
		lblDescription.setLabelFor(txtDescription);
		scrollPane_1.setViewportView(txtDescription);
		
		JLabel lblActivePlugins = new JLabel("Active plugins");
		GridBagConstraints gbc_lblActivePlugins = new GridBagConstraints();
		gbc_lblActivePlugins.anchor = GridBagConstraints.LINE_START;
		gbc_lblActivePlugins.insets = new Insets(0, 0, 5, 5);
		gbc_lblActivePlugins.gridx = 2;
		gbc_lblActivePlugins.gridy = 5;
		add(lblActivePlugins, gbc_lblActivePlugins);
		
		addPluginButton = new JButton("Add plugin");
		addPluginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PluginDescriptor plugin = new AddPluginDialog(pluginManager).selectPlugin();
				if(plugin != null) {
					selectedProfile.getPlugins().add(plugin);
					PluginListTableModel model = ((PluginListTableModel)pluginTable.getModel());
					model.setData(selectedProfile.getPlugins());
					pluginTableAdjuster.adjustColumns();
				}
			}
		});
		addPluginButton.setEnabled(false);
		GridBagConstraints gbc_addPluginButton = new GridBagConstraints();
		gbc_addPluginButton.anchor = GridBagConstraints.LINE_START;
		gbc_addPluginButton.insets = new Insets(0, 0, 5, 5);
		gbc_addPluginButton.gridx = 2;
		gbc_addPluginButton.gridy = 7;
		add(addPluginButton, gbc_addPluginButton);
		
		removePluginButton = new JButton("Remove plugin");
		removePluginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = pluginTable.getSelectedRow();
				if(row != -1) {
					PluginListTableModel model = ((PluginListTableModel)pluginTable.getModel());
					PluginDescriptor plugin = model.getRowItem(row);
					selectedProfile.getPlugins().remove(plugin);
					model.removeRow(plugin);
				}
			}
		});
		removePluginButton.setEnabled(false);
		GridBagConstraints gbc_removePluginButton = new GridBagConstraints();
		gbc_removePluginButton.anchor = GridBagConstraints.LINE_START;
		gbc_removePluginButton.insets = new Insets(0, 0, 5, 0);
		gbc_removePluginButton.gridx = 3;
		gbc_removePluginButton.gridy = 7;
		add(removePluginButton, gbc_removePluginButton);
		
		JLabel lblClasspath = new JLabel("Classpath");
		GridBagConstraints gbc_lblClasspath = new GridBagConstraints();
		gbc_lblClasspath.anchor = GridBagConstraints.LINE_START;
		gbc_lblClasspath.insets = new Insets(0, 0, 5, 5);
		gbc_lblClasspath.gridx = 2;
		gbc_lblClasspath.gridy = 8;
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
		gbc_scrollPane_3.gridy = 9;
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
		gbc_btnAdd.gridy = 10;
		add(btnAddClasspath, gbc_btnAdd);
		
		btnRemoveClasspath = new JButton("Remove jar");
		btnRemoveClasspath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URL url = classpathList.getSelectedValue();
				((DefaultListModel<Profile>)profilesList.getModel()).removeElement(url);
				selectedProfile.getClasspath().remove(url);
			}
		});
		btnRemoveClasspath.setEnabled(false);
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.anchor = GridBagConstraints.LINE_START;
		gbc_btnRemove.gridx = 3;
		gbc_btnRemove.gridy = 10;
		add(btnRemoveClasspath, gbc_btnRemove);
		
		JButton newProfileButton = new JButton("New profile");
		newProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Profile profile = new Profile();
				profile.setName("New profile");
				((DefaultListModel<Profile>)profilesList.getModel()).addElement(profile);
				
				profilesList.setSelectedValue(profile, true);
			}
		});
		GridBagConstraints gbc_newProfileButton = new GridBagConstraints();
		gbc_newProfileButton.anchor = GridBagConstraints.LINE_START;
		gbc_newProfileButton.insets = new Insets(0, 0, 0, 5);
		gbc_newProfileButton.gridx = 0;
		gbc_newProfileButton.gridy = 11;
		add(newProfileButton, gbc_newProfileButton);
		
		JButton removeProfileButton = new JButton("Remove profile");
		GridBagConstraints gbc_removeProfileButton = new GridBagConstraints();
		gbc_removeProfileButton.anchor = GridBagConstraints.LINE_START;
		gbc_removeProfileButton.insets = new Insets(0, 0, 0, 5);
		gbc_removeProfileButton.gridx = 1;
		gbc_removeProfileButton.gridy = 11;
		add(removeProfileButton, gbc_removeProfileButton);
		
		JButton activateProfileButton = new JButton("Activate profile");
		activateProfileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(selectedProfile == null) { return; }
				activateProfile(selectedProfile);
			}
		});
		GridBagConstraints gbc_activateProfileButton = new GridBagConstraints();
		gbc_activateProfileButton.anchor = GridBagConstraints.LINE_END;
		gbc_activateProfileButton.insets = new Insets(0, 0, 0, 5);
		gbc_activateProfileButton.gridx = 3;
		gbc_activateProfileButton.gridy = 11;
		
		add(activateProfileButton, gbc_activateProfileButton);
	}
	
	private void displaySelectedProfile() {
		txtProfileName.setText(selectedProfile.getName());
		((PluginListTableModel)pluginTable.getModel()).setData(selectedProfile.getPlugins());

		txtProfileName.setEnabled(true);
		txtDescription.setEnabled(true);

		pluginTable.setEnabled(true);
		addPluginButton.setEnabled(true);
		removePluginButton.setEnabled(true);
		
		classpathList.setEnabled(true);
		btnAddClasspath.setEnabled(true);
		btnRemoveClasspath.setEnabled(true);
	}
	
//	private void sonicmq() {
//		try {
//			File dir = new File("/Users/gerco/Projects/MessageManager/workspace/messagemanager-2.x/lib/8.6");
//			File[] files = dir.listFiles();
//			List<URL> urls = new ArrayList<URL>(files.length);
//			urls.add(new URL("file:///Users/gerco/Projects/MessageManager/workspace/messagemanager/messagemanager-sonicmq/target/messagemanager-sonicmq-3.0-SNAPSHOT.jar"));
//			for(File file: files) {
//				urls.add(file.toURL());
//			}
//			initializeWithModule("nl.queuemanager.smm.SMMModule", urls.toArray(new URL[urls.size()]));
//		} catch (MalformedURLException ex) {
//			throw new RuntimeException(ex);
//		}
//	}
//
//	private void initializeWithModule(String moduleName, URL[] urls) {
//	}
	
	private void activateProfile(Profile profile) {
		// Load the configured plugin modules
		List<Module> modules = new ArrayList<Module>();
		modules.add(new CoreModule());
		modules.add(new UIModule());
		modules.addAll(pluginManager.loadPluginModules(profile.getPlugins(), profile.getClasspath()));
		final Injector injector = parentInjector.createChildInjector(modules);
		ConnectivityProviderPlugin provider = injector.getInstance(ConnectivityProviderPlugin.class);
		provider.initialize();
	}
	
	public String getUITabName() {
		return "Profile";
	}

	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return new ConnectionState[] {
			//ConnectionState.CONNECTED,
			ConnectionState.DISCONNECTED
		};
	}

}
