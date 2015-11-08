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
package nl.queuemanager.app;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import nl.queuemanager.AddUITabEvent;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.platform.AboutEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.PreferencesEvent;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.task.TaskQueuePanel;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class MMFrame extends JFrame {
	private static final String APP_NAME = "Message Manager";

	private final JTabbedPane tabsPane;
	private final SortedMap<Integer, UITab> tabs;
	
	private UITab.ConnectionState currentState;
	
	@Inject
	public MMFrame(TaskQueuePanel taskQueuePanel, PlatformHelper platformHelper, ProfileTabPanel profileTab) {
		setTitle(APP_NAME);

		platformHelper.setFullScreenEnabled(this, true);
		
		this.tabs = new TreeMap<Integer, UITab>();
		
		Container contentPane = getContentPane();
		
		// Create the tabbedpane and add all the panels to it
		tabsPane = new JTabbedPane();
		tabsPane.setToolTipText("");
		
		addTab(new AddUITabEvent(0, profileTab));
		getRootPane().setDefaultButton(profileTab.getDefaultButton());
		
		// Now add the TabbedPane to the layout
		contentPane.add(tabsPane, BorderLayout.CENTER);
		setTabStates(UITab.ConnectionState.DISCONNECTED);

		// Add the task queue panel
		contentPane.add(taskQueuePanel, BorderLayout.SOUTH);
		
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@Subscribe
	public void addTab(final AddUITabEvent e) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					addTab(e);
				}
			});
			return;
		}
		
		tabs.put(e.getKey(), e.getTab());
		syncTabs();
	}
	
	@Subscribe
	public void removeTab(RemoveUITabEvent e) {
		removeTab(e.getKey());
	}
	
	@Subscribe
	public void profileActivated(ProfileActivatedEvent e) {
		removeTab(0);
		getRootPane().setDefaultButton(null);
	}
	
	public void removeTab(final int index) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					removeTab(index);
				}
			});
			return;
		}
		
		int sel = tabsPane.getSelectedIndex();
		boolean wasSelected = false;
		
		for(int pos = 0; pos < tabs.size(); pos++) {
			if(pos == index) {
				tabsPane.removeTabAt(pos);
				if(pos == sel) {
					wasSelected = true;
				}
				
				break;
			}
		}

		tabs.remove(index);
		
		// If we removed the selected tab, select the first one
		if(wasSelected) {
			tabsPane.setSelectedIndex(0);
		}
		
		syncTabs();
	}
		
	private void syncTabs() {
		if(!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Must run on EDT!");
		}
		
		int pos = 0;
		for(UITab tab: tabs.values()) {
			if(tabsPane.getTabCount() <= pos) {
				tabsPane.addTab(tab.getUITabName(), tab.getUITabComponent());
			} else 
			if(tabsPane.getComponentAt(pos) != tab.getUITabComponent()) {
				tabsPane.insertTab(tab.getUITabName(), null, tab.getUITabComponent(), null, pos);
			}
			pos++;
	
			setTabState(tab, currentState);
		}
	}
	
	/**
	 *  Select the "Help" tab, if any
	 */
	@Subscribe
	public void onAboutEvent(AboutEvent e) {
		for(UITab tab: tabs.values()) {
			if(tab.getUITabName().equals("Help")) {
				tabsPane.setSelectedIndex(tabsPane.indexOfComponent(tab.getUITabComponent()));
			}
			
		}
	}
	
	@Subscribe
	public void onPreferencesEvent(PreferencesEvent e) {
		for(UITab tab: tabs.values()) {
			if(tab.getUITabName().equals("Settings")) {
				tabsPane.setSelectedIndex(tabsPane.indexOfComponent(tab.getUITabComponent()));
			}
			
		}
	}
	
	private void setTabStates(UITab.ConnectionState state) {
		currentState = state;
		
		for(UITab tab: tabs.values()) {
			setTabState(tab, state);
		}
	}
	
	private void setTabState(UITab tab, UITab.ConnectionState state) {
		int index = tabsPane.indexOfComponent(tab.getUITabComponent());
		for(UITab.ConnectionState s: tab.getUITabEnabledStates()) {
			if(s == state) {
				tabsPane.setEnabledAt(index, true);
				return;
			}
		}
		tabsPane.setEnabledAt(index, false);
	}
	
	@Subscribe
	public void processEvent(DomainEvent event) {
		switch(event.getId()) {
		case JMX_CONNECT:
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
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
