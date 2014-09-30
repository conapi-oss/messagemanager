package nl.queuemanager.core.tasks;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;

public class EnumerateBrokersTask extends Task {

	private final JMSDomain domain;

	@Inject
	public EnumerateBrokersTask(JMSDomain domain, EventBus eventBus) {
		super(domain, eventBus);
		this.domain = domain;
	}
	
	@Override
	public void execute() throws Exception {
		domain.enumerateBrokers();
	}
	
	@Override
	public String toString() {
		return "Enumerating brokers";
	}
	
}
