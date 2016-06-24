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
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import nl.queuemanager.AddUITabEvent;
import nl.queuemanager.ProfileActivatedEvent;
import nl.queuemanager.Version;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.events.ApplicationInitializedEvent;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.platform.AboutEvent;
import nl.queuemanager.core.platform.PlatformHelper;
import nl.queuemanager.core.platform.PreferencesEvent;
import nl.queuemanager.core.platform.QuitEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.PreconnectTaskFactory;
import nl.queuemanager.ui.MOTDPanel;
import nl.queuemanager.ui.UITab;
import nl.queuemanager.ui.task.TaskQueuePanel;

@SuppressWarnings("serial")
@Singleton
public class MMFrame extends JFrame {
	private static final String APP_NAME = "Message Manager";
	private static final String WINDOW_POSITION_AND_SIZE = "windowPositionAndSize";

	private final CoreConfiguration config;
	
	private final JTabbedPane tabsPane;
	private final SortedMap<Integer, UITab> tabs;
	
	private UITab.ConnectionState currentState;
	private TaskExecutor worker;
	private PreconnectTaskFactory taskFactory;
	
	@Inject
	public MMFrame(CoreConfiguration config, TaskQueuePanel taskQueuePanel, PlatformHelper platformHelper, MOTDPanel motdPanel, ProfileTabPanel profileTab,
			TaskExecutor worker, PreconnectTaskFactory taskFactory) {
		this.config = config;
		this.worker = worker;
		this.taskFactory = taskFactory;
		
		setTitle(String.format("%s %s", APP_NAME, Version.VERSION));

		platformHelper.setFullScreenEnabled(this, true);
		
		this.tabs = new TreeMap<Integer, UITab>();
		
		Container contentPane = getContentPane();
		
		// Create the MOTDPanel and put it in the right place
		contentPane.add(motdPanel, BorderLayout.NORTH);
		
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

		//centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		
		setSize(new Dimension(800, 600));
		restoreWindowPositionAndSize();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
	public void removeTab(final RemoveUITabEvent e) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					removeTab(e);
				}
			});
			return;
		}
		removeTab(e.getKey());
	}
	
	@Subscribe
	public void applicationInitialized(ApplicationInitializedEvent e) {
		// Kick off the MOTD task. It will fire an event when MOTD is known
		worker.execute(taskFactory.checkMotdTask(config.getUniqueId(), "smm.queuemanager.nl"));
		
		// Kick off the ReleaseNote task. It will fire an event if we have a release note
		worker.execute(taskFactory.checkReleaseNote("smm.queuemanager.nl", Version.BUILD_ID));
	}
	
	@Subscribe
	public void profileActivated(final ProfileActivatedEvent e) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					profileActivated(e);
				}
			});
			return;
		}
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
	
	@Subscribe
	public void onQuitEvent(QuitEvent e) {
		saveWindowPositionAndSize();
		e.quit();
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
	
	private void saveWindowPositionAndSize() {
		final String value = String.format("%d,%d,%d,%d", getX(), getY(), getWidth(), getHeight());
		config.setUserPref(WINDOW_POSITION_AND_SIZE, value);
	}
	
	private void restoreWindowPositionAndSize() {
		final String value = config.getUserPref(WINDOW_POSITION_AND_SIZE, null);
		if(value == null) {
			return; // No config value found
		}

		final String[] values = value.split(",");
		if(values.length != 4) {
			return; // Value is not in the right format, should be x,y,w,h.
		}
		
		try {
			int x = Integer.valueOf(values[0]);
			int y = Integer.valueOf(values[1]);
			int w = Integer.valueOf(values[2]);
			int h = Integer.valueOf(values[3]);
			
			// Clamp size to screen size
			final Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			w = Math.min(w, maxBounds.width);
			h = Math.min(h, maxBounds.height);
			
			// We guaranteed that the window will fit on the display, now make sure that
			// the window will fit at it's current position. If it doesn't, move it to a
			// position in which it will fit (at worst covering the whole display)
			x = clamp(x, maxBounds.x, maxBounds.x+maxBounds.width-w);
			y = clamp(y, maxBounds.y, maxBounds.y+maxBounds.height-h);

			Point location = new Point(x, y);
			setLocation(location);

			Dimension size = new Dimension(w, h);
			setSize(size);
		} catch (NumberFormatException e) {
			return; // One or more numbers are not integers
		}
	}
	
	private int clamp(int v, int min, int max) {
		if(v<min) return min;
		if(v>max) return max;
		return v;
	}
	
}
