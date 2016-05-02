package nl.queuemanager.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.events.ApplicationInitializedEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.core.util.ReleasePropertiesEvent;
import nl.queuemanager.ui.util.MarqueePanel;

import com.google.common.eventbus.Subscribe;

public class MOTDPanel extends MarqueePanel {

	private TaskExecutor worker;
	private TaskFactory taskFactory;
	private CoreConfiguration config;

	@Inject
	public MOTDPanel(TaskExecutor worker, TaskFactory taskFactory, CoreConfiguration config) {
		super(10, 5);
		this.worker = worker;
		this.taskFactory = taskFactory;
		this.config = config;
		
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setBackground(Color.YELLOW);
		
		// This panel starts out invisible. If there is an MOTD, it will be made visible.
		setVisible(false);
		
		setScrollWhenFocused(false);
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Subscribe
	public void applicationInitialized(ApplicationInitializedEvent e) {
		// Kick off the MOTD task. It will fire an event when MOTD is known
		// FIXME get the right hostname in here
		worker.execute(taskFactory.checkMotdTask(config.getUniqueId(), "smm.queuemanager.nl"));
		
		// Kick off the ReleaseNote task. It will fire an event if we have a release note
		// FIXME Create a real build id
		worker.execute(taskFactory.checkReleaseNote("smm.queuemanager.nl", "1234"));
	}

	public void addMessage(String message) {
		JLabel label = new JLabel(message);
		add(label);
		add(Box.createHorizontalStrut(25));
		setVisible(true);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			startScrolling();
		} else {
			stopScrolling();
		}
		
		super.setVisible(visible);
	}
	
	@Subscribe
	public void processEvent(final ReleasePropertiesEvent event) {
		if(SwingUtilities.isEventDispatchThread()) {
			switch(event.getId()) {
			case RELEASE_NOTES_FOUND:
			case MOTD_FOUND:
				addMessage(event.getInfo().toString());
				break;
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					processEvent(event);
				}
			});
		}
	}

}
