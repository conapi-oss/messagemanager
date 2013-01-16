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
package nl.queuemanager.ui.task;

import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.Subscribe;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.core.util.CollectionFactory;
import nl.queuemanager.ui.util.JStatusBar;

/**
 * JPanel subclass that displays the currently executing or waiting tasks using a list
 * of JStatusBar instances.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class TaskQueuePanel extends JPanel {

	private Map<Task, JStatusBar> statusbars = CollectionFactory.newHashMap();
	
	public TaskQueuePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	@Subscribe
	public void processEvent(final TaskEvent event) {
		final Task t = (Task)event.getSource();

		// Do not display background tasks
		if(t.isBackground())
			return;
		
		switch(event.getId()) {
		case TASK_WAITING:
		case TASK_STARTED:
			if(!statusbars.containsKey(t)) {
				final JStatusBar bar = new JStatusBar();
				final StatusBarManipulator manipulator = new StatusBarManipulator(bar);
				t.addListener(manipulator);
				manipulator.processEvent(event);
				statusbars.put(t, bar);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						TaskQueuePanel.this.add(bar);
						TaskQueuePanel.this.revalidate();
					}
				});
			}
			break;
			
		case TASK_DISCARDED:
		case TASK_FINISHED:
			final JStatusBar bar = statusbars.remove(t);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TaskQueuePanel.this.remove(bar);
					TaskQueuePanel.this.revalidate();
				}
			});
			break;
		}
	}
}

class StatusBarManipulator implements EventListener<TaskEvent> {
	private final JStatusBar statusBar;
	
	public StatusBarManipulator(JStatusBar statusBar) {
		this.statusBar = statusBar;
	}
	
	public void processEvent(final TaskEvent event) {	
		final Task task = (Task)event.getSource();
		
		switch(event.getId()) {
		case TASK_WAITING:
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText(event.getSource().toString() + " (waiting)");
					statusBar.setBusy(false);
					
					if(task instanceof CancelableTask) {
						statusBar.setCancelEnabled(true);
					}
				}
			});
			break;
		
		case TASK_STARTED:
			if(statusBar.getCancelPressed()) {
				((CancelableTask)task).cancel();
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText(task.getStatus());
					statusBar.setBusy(true);
					
					if(task.getProgressMaximum() != 1) {
						statusBar.enableProgressBar(0, task.getProgressMaximum());
					}
					
					if(task instanceof CancelableTask) {
						statusBar.setCancelEnabled(true);
					}
				}
			});
			break;
			
		case TASK_PROGRESS:
			if(statusBar.getCancelPressed()) {
				((CancelableTask)task).cancel();
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusBar.setText(task.getStatus());
					statusBar.setBusy(true);
					
					if(task.getProgressMaximum() != 1) {
						statusBar.setProgressAmount((Integer)event.getInfo());
					}
				}
			});
			
			break;
		}
	}

}
