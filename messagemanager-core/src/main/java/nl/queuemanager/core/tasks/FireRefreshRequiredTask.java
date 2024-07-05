package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.task.BackgroundTask;
import nl.queuemanager.jms.JMSDestination;

import java.util.List;

public class FireRefreshRequiredTask extends BackgroundTask {
		private final JMSDestinationHolder target;
		private final JMSDestination destination;

	@Inject
	FireRefreshRequiredTask(Object resource, EventBus eventBus,
				@Assisted JMSDestinationHolder target,
				@Assisted JMSDestination destination) {
			super(resource, eventBus);
			this.target = target;
			this.destination = destination;
		}

		@Override
		public void execute() throws Exception {
			target.refreshRequired(destination);
		}

		@Override
		public String toString() {
			return "Refreshing " + destination;
		}

		public interface JMSDestinationHolder {
			public JMSDestination getJMSDestination();
			public List<JMSDestination> getJMSDestinationList();
			public void refreshRequired(JMSDestination destination);
		}

	}