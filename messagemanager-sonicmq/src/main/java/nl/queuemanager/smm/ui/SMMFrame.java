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
import java.awt.Toolkit;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.smm.ConnectionModel;
import nl.queuemanager.smm.Domain;
import nl.queuemanager.smm.SMCConnectionModel;
import nl.queuemanager.smm.Version;
import nl.queuemanager.ui.MessageSendTabPanel;
import nl.queuemanager.ui.QueuesTabPanel;
import nl.queuemanager.ui.TopicSubscriberTabPanel;
import nl.queuemanager.ui.task.TaskQueuePanel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sonicsw.ma.gui.domain.DomainConnectionModel;
import com.sonicsw.ma.gui.util.JMAFrame;

@SuppressWarnings("serial")
@Singleton
public class SMMFrame extends JMAFrame {
	private static final String APP_NAME = "Message Manager";

	private final JTabbedPane tabsPane;
	
	private final ConnectionTabPanel connectionPanel;
	private final QueuesTabPanel queuesPanel;
	
	@Inject
	public SMMFrame(Domain sonic, TaskExecutor worker, 
			ConnectionTabPanel connectionPanel,
			QueuesTabPanel queuesPanel,
			TopicSubscriberTabPanel topicsPanel,
			MessageSendTabPanel messageSendPanel,
			HelpTabPanel helpPanel,
			TaskQueuePanel taskQueuePanel) {
		super("messagemanager");
		
		this.connectionPanel = connectionPanel;
		this.queuesPanel = queuesPanel;
		
		setTitle("");
		
		Container contentPane = getContentPane();
		
		// Create the tabbedpane and add all the panels to it
		tabsPane = new JTabbedPane();
		tabsPane.setToolTipText("");
		tabsPane.addTab("Connection", connectionPanel);
		tabsPane.addTab("Queue browser", queuesPanel);
		tabsPane.addTab("Topic subscriber", topicsPanel);
		tabsPane.addTab("Message sender", messageSendPanel);		
		tabsPane.add("Help", helpPanel);
		
		// Now add the TabbedPane to the layout
		contentPane.add(tabsPane, BorderLayout.CENTER);
		tabsPane.setEnabledAt(1, false);
		tabsPane.setEnabledAt(2, false);
		tabsPane.setEnabledAt(3, false);

		// Add the task queue panel
		contentPane.add(taskQueuePanel, BorderLayout.SOUTH);
		worker.addListener(taskQueuePanel);
		sonic.addListener(new DomainEventListener());
	}
	
	public void start() {
		final DomainConnectionModel model = connectionPanel.getConnectionModel();
		
		if(model != null) {
			connectionPanel.connectSonic(new SMCConnectionModel(model));
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
						tabsPane.setEnabledAt(1, true);
						tabsPane.setSelectedComponent(queuesPanel);
					}
				});
				break;
			case BROKER_CONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tabsPane.setEnabledAt(2, true);
						tabsPane.setEnabledAt(3, true);
						tabsPane.setEnabledAt(4, true);
					}
				});
				break;
			case BROKER_DISCONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						tabsPane.setEnabledAt(2, false);
						tabsPane.setEnabledAt(3, false);
						tabsPane.setEnabledAt(4, false);
					}
				});
				break;
			case JMX_DISCONNECT:
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setTitle("");
						tabsPane.setEnabledAt(1, false);
						tabsPane.setEnabledAt(2, false);
						tabsPane.setEnabledAt(3, false);
						tabsPane.setEnabledAt(4, false);
						tabsPane.setSelectedComponent(connectionPanel);
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
	
	// static initializer for setting look & feel
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if(Boolean.TRUE.equals(Toolkit.getDefaultToolkit().getDesktopProperty("awt.dynamicLayoutSupported")))
				Toolkit.getDefaultToolkit().setDynamicLayout(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
