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
package nl.queuemanager.smm.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.smm.ConnectionModel;
import nl.queuemanager.smm.Domain;
import nl.queuemanager.smm.SMCConnectionModel;
import nl.queuemanager.smm.SMMConfiguration;
import nl.queuemanager.ui.CommonUITasks;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.util.DesktopHelper;

import com.google.inject.Inject;
import com.sonicsw.ma.gui.PreferenceManager;
import com.sonicsw.ma.gui.domain.DomainConnectionModel;
import com.sonicsw.ma.gui.domain.JDomainConnectionDialog;

@SuppressWarnings("serial")
public class ConnectionTabPanel extends JPanel implements UITab {
	private final Domain sonic;
	private final TaskExecutor worker;
	private final SMMConfiguration config;
	private final DesktopHelper desktop;
	private       ConnectionModelTable connectionTable;
	private final PreferenceManager prefs = PreferenceManager.getInstance();
	private final ConnectionDialogProvider connectionDialogProvider;
	
	@Inject
	public ConnectionTabPanel(Domain sonic, TaskExecutor worker, SMMConfiguration config, DesktopHelper desktop, ConnectionDialogProvider connectionDialogProvider) {
		this.sonic = sonic;
		this.worker = worker;
		this.config = config;
		this.desktop = desktop;
		this.connectionDialogProvider = connectionDialogProvider;

		JPanel brandingPanel = createBrandingPanel();
		JPanel connectionsPanel = createConnectionsPanel();
		
		// Now add the panels to this panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(brandingPanel);
		setupMailingList();
		add(connectionsPanel);
	}

	private void setupMailingList() {		
		if("unknown".equals(config.getUserPref(SMMConfiguration.PREF_MAILINGLIST_STATUS, "unknown"))) {
			final JTextField emailAddressField;
			final JButton subscribeButton;
			final JButton denyButton;
			final Box box = Box.createHorizontalBox();
			
			box.setBorder(new TitledBorder("Mailing list"));
			box.add(new JLabel("To subscribe to our mailing list, enter your e-mail address:"));
			box.add(Box.createHorizontalStrut(5));
			box.add(emailAddressField = new JTextField(15));
			emailAddressField.setMaximumSize(new Dimension(
					Integer.MAX_VALUE,
					emailAddressField.getPreferredSize().height));
			box.add(Box.createHorizontalStrut(10));
			box.add(subscribeButton = new JButton("Subscribe"));
			box.add(Box.createHorizontalStrut(5));
			box.add(denyButton = new JButton("No, thank you"));
			add(box);
			
			subscribeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(emailAddressField.getText() != null && emailAddressField.getText().length() > 0) {
						try {
							URL url = new URL("http://queuemanager.nl/subscribe.php?email=" + 
									emailAddressField.getText());
							URLConnection c = url.openConnection();
							c.connect();
							c.getContent();
						} catch (MalformedURLException e1) {
							// This URL cannot be malformed since it is static.
							throw new RuntimeException(e1);
						} catch (IOException e1) {
							// If an IOException occurs. Ignore it.
							return;
						}
						config.setUserPref(SMMConfiguration.PREF_MAILINGLIST_STATUS, 
								emailAddressField.getText());
						remove(box);
						revalidate();
					}
				}
			});
			denyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					config.setUserPref(SMMConfiguration.PREF_MAILINGLIST_STATUS, "deny");
					remove(box);
					revalidate();
				}
			});
		}
	}

	private JPanel createConnectionsPanel() {
		connectionTable = createConnectionTable();
		JScrollPane connectionScrollPane = new JScrollPane(connectionTable);
		
		// Create the action panel
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		actionPanel.add(createConnectButton());
		actionPanel.add(createDisconnectButton());
		actionPanel.add(Box.createHorizontalStrut(15));
		actionPanel.add(createNewConnectionButton());
		actionPanel.add(createDeleteButton());
		
		// Tie them all together in the connectionsPanel
		JPanel connectionsPanel = new JPanel();
		connectionsPanel.setLayout(new BoxLayout(connectionsPanel, BoxLayout.Y_AXIS));
		connectionsPanel.setBorder(BorderFactory.createTitledBorder("Recently used connections"));
		connectionsPanel.add(connectionScrollPane);
		connectionsPanel.add(actionPanel);
		return connectionsPanel;
	}

	private JPanel createBrandingPanel() {
		// Create the branding area
		JPanel brandingPanel = new JPanel();
		brandingPanel.setLayout(new BoxLayout(brandingPanel, BoxLayout.X_AXIS));
		brandingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		brandingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		
		brandingPanel.setBackground(Color.white);

		// create space between the be used to align the two logo pictures!
		Dimension minSize = new Dimension(5, 80);
		Dimension prefSize = new Dimension(5, 80);
		Dimension maxSize = new Dimension(Integer.MAX_VALUE, 80);

		// space on left of the first picture
		brandingPanel.add(new Box.Filler(minSize, prefSize, maxSize));		
		
		// Get Progaia logo
		JLabel jLabelProgaia = new JLabel();
		URL url = getClass().getResource("progaia.jpg");
		jLabelProgaia.setIcon(new ImageIcon(url));
		try {
			desktop.addLink(jLabelProgaia, new URI("http://www.progaia.nl"));
		} catch (URISyntaxException e) {
		}
		brandingPanel.add(jLabelProgaia);
		
		// space between the pictures
		brandingPanel.add(new Box.Filler(minSize, prefSize, maxSize));		
		
		// Get Progress Sonic logo
		JLabel jLabelSonic = new JLabel();
		URL url2 = getClass().getResource("progresssonic.jpg");
		jLabelSonic.setIcon(new ImageIcon(url2));
		try {
			desktop.addLink(jLabelSonic, new URI("http://www.progress.com"));
		} catch (URISyntaxException e) {
		}
		brandingPanel.add(jLabelSonic);
				
		// space on right of the second picture
		brandingPanel.add(new Box.Filler(minSize, prefSize, maxSize));
		return brandingPanel;
	}

	private ConnectionModelTable createConnectionTable() {
		ConnectionModelTable table = new ConnectionModelTable();		
	
		table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					connectToSelectedModel();
				}
			}
	     });
		
		table.setData(getConnectionModels());
		
		return table;
	}

	@SuppressWarnings("unchecked")
	private List<ConnectionModel> getConnectionModels() {
		List<String> connectionModels = DomainConnectionModel.getFirstN(prefs);
		List<ConnectionModel> models = CollectionFactory.newArrayList();
		
		for(Object o: connectionModels) {
			String name = (String)o;
			models.add(new SMCConnectionModel(new DomainConnectionModel(prefs, name)));
		}
		
		Collections.sort(models);
		
		return models;
	}
		
	private JButton createNewConnectionButton() {
		JButton button = CommonUITasks.createButton("New connection",
		new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Start up the connection dialog with an empty connection
				final DomainConnectionModel model = getConnectionModel();
				
				if(model != null) {
					connectSonic(new SMCConnectionModel(model));
				}
			}
		});
		CommonUITasks.makeSegmented(button, Segmented.FIRST);
		return button;
	}

	private JButton createConnectButton() {
		JButton button = CommonUITasks.createButton("Connect",
		new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				connectToSelectedModel();
			}
		});
		CommonUITasks.makeSegmented(button, Segmented.FIRST);
		return button;
	}
	
	public JButton createDisconnectButton() {
		JButton button = CommonUITasks.createButton("Disconnect", 
		new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
			}
		});
		CommonUITasks.makeSegmented(button, Segmented.LAST);
		return button;
	}
	
	private JButton createDeleteButton() {
		JButton button = CommonUITasks.createButton("Delete connection", 
		new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				removeConnection();
			}
		});
		CommonUITasks.makeSegmented(button, Segmented.LAST);
		return button;
	}
	
	@SuppressWarnings("unchecked")
	private void removeConnection(){
		ConnectionModel model = connectionTable.getSelectedItem();
		if (model != null) {
			String connectionModel = model.getConnectionName();
			ArrayList<String> connectionModels = DomainConnectionModel.getFirstN(prefs);
			for (int i = 0; i < connectionModels.size(); i++) {
				if (connectionModels.get(i).equals(connectionModel)) {
					// remove it from the list
					connectionModels.remove(i);
					// remove the node
					prefs.removeNode("connections/"+connectionModel);
				}
			}
			// save the list
			saveFirstN(prefs, connectionModels);
			// check if it was set to default
			checkDefaultConnectionName(prefs, connectionModel, connectionModels);
			// reload the table
			connectionTable.setData(getConnectionModels());
		}
	}
	
	/**
	 * Checks whether the deleted connectionModel was default, if so set another connectionModel as default.
	 * @param preferencemanager The PreferenceManager instance.
	 * @param deletedModel The deleted model.
	 * @param arraylist ArrayList of the models still available.
	 */
	private static void checkDefaultConnectionName(PreferenceManager preferencemanager, String deletedModel, ArrayList<String> arraylist){
		String defaultConnectionName = preferencemanager.getString("connections", "defaultConnectionName", null);
		if (defaultConnectionName != null && deletedModel.equals(defaultConnectionName)){
			if (arraylist.size() > 0)
				preferencemanager.setString("connections", "defaultConnectionName", arraylist.get(0), false);
			else
				preferencemanager.setString("connections", "defaultConnectionName", "Connection1", false);
		}
	}
	
	// Private method of DomainConnectionModel to store the connections
	private static void saveFirstN(PreferenceManager preferencemanager,	ArrayList<String> arraylist) {
		StringBuffer stringbuffer = new StringBuffer();
		for (int i = 0; i < arraylist.size(); i++) {
			if (stringbuffer.length() > 0)
				stringbuffer.append(',');
			stringbuffer.append(arraylist.get(i));
		}
		preferencemanager.setString("connections", "firstN", stringbuffer.toString(), false);
	}
	
	private void disconnect() {
		worker.execute(new Task(sonic) {
			@Override
			public void execute() throws Exception {
				sonic.disconnect();
			}
			@Override
			public String toString() {
				return "Disconnecting";
			}
		});
	}
	
	private void connectToSelectedModel() {
		// Get the selected row in the connections table and
		// connect the Sonic domain to it.
		ConnectionModel model = connectionTable.getSelectedItem();
		if(model != null) {
			// Allow the user to enter the password by popping up the connection dialog
			final DomainConnectionModel realModel =
				getConnectionModel(((SMCConnectionModel)model).getDelegate());
			
			if(realModel != null) {
				connectSonic(new SMCConnectionModel(realModel));
			}
		} else {
			// When no item is selected in the connectionTable open the default connection
			final DomainConnectionModel realModel = getConnectionModel();
			
			if(realModel != null) {
				connectSonic(new SMCConnectionModel(realModel));
			}
		}
	}

	public void connectSonic(final ConnectionModel model) {
		worker.execute(new Task(sonic) {
			@Override
			public void execute() throws Exception {
				sonic.connect(model);
			}
			@Override
			public String toString() {
				return "Connecting to management broker";
			}
		});
	}

	public DomainConnectionModel getConnectionModel() {
		return getConnectionModel(new DomainConnectionModel(prefs));
	}
	
	public DomainConnectionModel getConnectionModel(DomainConnectionModel model) {
		JDomainConnectionDialog connectionDialog = connectionDialogProvider.get();
		try {
			connectionDialog.editInstance(null, model, true);
			connectionDialog.setVisible(true);
		} catch (Exception e) {
		} 
		
        if(connectionDialog.getCloseCommand() != -1)
        {
            model = (DomainConnectionModel)connectionDialog.getModel();
            model.saveToPrefs(prefs);
            
            // Display the new model data in case the user changed something
            connectionTable.setData(getConnectionModels());
        } else  {
        	model = null;
        }
        
        connectionDialog.dispose();
        
        return model;
	}
	
	public void showDefaultConnectionDialog() {
		final DomainConnectionModel model = getConnectionModel();
		if(model != null) {
			connectSonic(new SMCConnectionModel(model));
		}		
	}

	public String getUITabName() {
		return "Connection";
	}
	
	public JComponent getUITabComponent() {
		return this;
	}

	public ConnectionState[] getUITabEnabledStates() {
		return ConnectionState.values();
	}

}
