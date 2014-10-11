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
package nl.queuemanager.activemq;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.ui.CommonUITasks;
import nl.queuemanager.ui.CommonUITasks.Segmented;
import nl.queuemanager.ui.UITab;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ConnectionTabPanel extends JPanel implements UITab {
	private final ActiveMQDomain domain;
	private final TaskExecutor worker;
	private final EventBus eventBus;
	
	private final JTextField pidField;
	
	@Inject
	public ConnectionTabPanel(ActiveMQDomain domain, TaskExecutor worker, EventBus eventBus) {
		this.domain = domain;
		this.worker = worker;
		this.eventBus = eventBus;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		add(pidField = createPIDTextBox());
		add(createNewConnectionButton());
	}

	private JTextField createPIDTextBox() {
		return new JTextField(10);
	}
	
	private JButton createNewConnectionButton() {
		JButton button = CommonUITasks.createButton("New connection",
		new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				connect();
			}
		});
		CommonUITasks.makeSegmented(button, Segmented.ONLY);
		return button;
	}

	public void connect() {
		worker.execute(new Task(domain, eventBus) {
			@Override
			public void execute() throws Exception {
				String url = sun.management.ConnectorAddressLink.importFrom(Integer.valueOf(pidField.getText()));
				domain.connect(url);
			}
			@Override
			public String toString() {
				return "Connecting to ActiveMQ";
			}
		});
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
