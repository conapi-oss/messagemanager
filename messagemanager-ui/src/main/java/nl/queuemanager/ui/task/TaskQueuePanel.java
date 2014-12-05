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

import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.ui.util.JStatusBar;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * JPanel subclass that displays the currently executing or waiting tasks using a list
 * of JStatusBar instances.
 * 
 * @author Gerco Dries (gdr@progaia-rs.nl)
 *
 */
@SuppressWarnings("serial")
public class TaskQueuePanel extends JPanel {

	private final EventBus eventBus;
	private Map<Task, StatusBarManipulator> statusBarManipulators = new IdentityHashMap<Task, StatusBarManipulator>();
	private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("developer"));
	
	@Inject
	public TaskQueuePanel(EventBus eventBus) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.eventBus = eventBus;
	}

	@Subscribe
	public void processEvent(final TaskEvent event) {
		// Always run this logic on the EDT to make sure updates to the panel are not clobbering 
		// each other and this method can remain lock free. Tasks can run on and send events from
		// any thread.
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					processEvent(event);
				}
			});
			return;
		}
		
		final Task t = (Task)event.getSource();

		// Do not display background tasks (except in dev mode)
		if(!DEBUG && t.isBackground())
			return;
		
		switch(event.getId()) {
		case TASK_WAITING:
		case TASK_STARTED:
			if(!statusBarManipulators.containsKey(t)) {
				final JStatusBar bar = new JStatusBar();
				final StatusBarManipulator manipulator = new StatusBarManipulator(bar, t);
				eventBus.register(manipulator);
				manipulator.processEvent(event);
				statusBarManipulators.put(t, manipulator);
				
				add(bar);
				revalidate();
			}
			break;
			
		case TASK_DISCARDED:
		case TASK_FINISHED:
			final StatusBarManipulator manipulator = statusBarManipulators.remove(t);
			if(manipulator != null) {
				eventBus.unregister(manipulator);
				remove(manipulator.getStatusBar());
				revalidate();
			}
			
			break;
		}
	}
}

class StatusBarManipulator {
	private final JStatusBar statusBar;
	private final Task myTask;
	
	public StatusBarManipulator(JStatusBar statusBar, Task task) {
		this.statusBar = statusBar;
		this.myTask = task;
	}

	@Subscribe
	public void processEvent(final TaskEvent event) {	
		final Task task = (Task)event.getSource();
		if(task != myTask) {
			return;
		}
		
		switch(event.getId()) {
		case TASK_WAITING:
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getStatusBar().setText(event.getSource().toString() + " (waiting)");
					getStatusBar().setBusy(false);
					
					if(task instanceof CancelableTask) {
						getStatusBar().setCancelEnabled(true);
					}
				}
			});
			break;
		
		case TASK_STARTED:
			if(getStatusBar().getCancelPressed()) {
				((CancelableTask)task).cancel();
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getStatusBar().setText(task.getStatus());
					getStatusBar().setBusy(true);
					
					if(task.getProgressMaximum() != 1) {
						getStatusBar().enableProgressBar(0, task.getProgressMaximum());
					}
					
					if(task instanceof CancelableTask) {
						getStatusBar().setCancelEnabled(true);
					}
				}
			});
			break;
			
		case TASK_PROGRESS:
			if(getStatusBar().getCancelPressed()) {
				((CancelableTask)task).cancel();
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getStatusBar().setText(task.getStatus());
					getStatusBar().setBusy(true);
					
					if(task.getProgressMaximum() != 1) {
						getStatusBar().setProgressAmount((Integer)event.getInfo());
					}
				}
			});
			
			break;
		}
	}

	public JStatusBar getStatusBar() {
		return statusBar;
	}

}
