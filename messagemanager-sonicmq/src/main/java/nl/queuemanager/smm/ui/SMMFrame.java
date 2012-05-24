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

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.smm.ConnectionModel;
import nl.queuemanager.smm.Domain;
import nl.queuemanager.smm.Version;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.task.TaskQueuePanel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sonicsw.ma.gui.util.JMAFrame;

@SuppressWarnings("serial")
@Singleton
public class SMMFrame extends JMAFrame {
	private static final String APP_NAME = "Message Manager";

	private final JTabbedPane tabsPane;
	private final SortedMap<Integer, UITab> tabs;
	
	@Inject
	public SMMFrame(Domain sonic, TaskExecutor worker, Map<Integer, UITab> tabs, TaskQueuePanel taskQueuePanel) {
		super("messagemanager");
		
		setTitle("");
		
		this.tabs = new TreeMap<Integer, UITab>(tabs);
		
		Container contentPane = getContentPane();
		
		// Create the tabbedpane and add all the panels to it
		tabsPane = new JTabbedPane();
		tabsPane.setToolTipText("");
		for(UITab tab: this.tabs.values()) {
			tabsPane.addTab(tab.getUITabName(), tab.getUITabComponent());
		}
		
		// Now add the TabbedPane to the layout
		contentPane.add(tabsPane, BorderLayout.CENTER);
		setTabStates(UITab.ConnectionState.DISCONNECTED);

		// Add the task queue panel
		contentPane.add(taskQueuePanel, BorderLayout.SOUTH);
		worker.addListener(taskQueuePanel);
		sonic.addListener(new DomainEventListener());
	}
	
	private void setTabStates(UITab.ConnectionState state) {
		tabs:
		for(UITab tab: tabs.values()) {
			int index = tabsPane.indexOfComponent(tab.getUITabComponent());
			for(UITab.ConnectionState s: tab.getUITabEnabledStates()) {
				if(s == state) {
					tabsPane.setEnabledAt(index, true);
					continue tabs;
				}
			}
			tabsPane.setEnabledAt(index, false);
		}
	}
		
	private class DomainEventListener implements EventListener<DomainEvent> {
		public void processEvent(DomainEvent event) {
			switch(event.getId()) {
			case JMX_CONNECT:
				final ConnectionModel model = (ConnectionModel)event.getInfo();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setTitle("(" + model.getConnectionName() + " - " + model.getDomainName() + "@" + model.getUrl() + ")");
						setTabStates(UITab.ConnectionState.CONNECTED);
						int nextIndex = tabsPane.getSelectedIndex() + 1;
						if(nextIndex < tabsPane.getTabCount()) {
							tabsPane.setSelectedIndex(nextIndex);
						}
					}
				});
				break;
			case BROKER_CONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setTabStates(UITab.ConnectionState.CONNECTED);
					}
				});
				break;
			case BROKER_DISCONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setTabStates(UITab.ConnectionState.CONNECTED);
					}
				});
				break;
			case JMX_DISCONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setTitle("");
						setTabStates(UITab.ConnectionState.CONNECTED);
					}
				});
				break;
			}
		}
		
	}
	
	@Override 
	public void setTitle(String title) {
		super.setTitle(APP_NAME + " " + Version.getVersion() + " " + title);
	}
	
	@Override
	protected void maCleanup() {
	}

	@Override
	protected void maInitialize() {
	}
	
}
