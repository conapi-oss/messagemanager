package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.jms.JMSQueue;

import javax.inject.Inject;
import java.util.List;

public class ClearQueuesTask extends Task {
	private final JMSDomain domain;
	private final List<JMSQueue> queueList;

	@Inject
	ClearQueuesTask(JMSDomain domain, EventBus eventBus, @Assisted List<JMSQueue> queueList) {
		super(domain, eventBus);
		this.domain = domain;
		this.queueList = queueList;
	}

	@Override
	public void execute() throws Exception {
		domain.deleteMessages(queueList);
	}

	@Override
	public String toString() {
		if(queueList.size()==1)
			return "Deleting all messages from " + queueList.get(0);
		else
			return "Deleting all messages from " + queueList.size() + " queue(s)";
	}
}
