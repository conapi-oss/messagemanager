package nl.queuemanager.core;

import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskExecutor;

import com.google.inject.Inject;

/**
 * Listens for applicationwide domain events and reacts accordingly. Currently, this is only the
 * JMX_CONNECT event, that triggers enumeration of brokers in the domain.
 * 
 * @author gerco
 *
 */
public class GlobalDomainEventListener implements EventListener<DomainEvent> {

	private final JMSDomain domain;
	private final TaskExecutor worker;

	@Inject
	public GlobalDomainEventListener(JMSDomain domain, TaskExecutor worker) {
		this.domain = domain;
		this.worker = worker;
		
		domain.addListener(this);
	}
	
	public void processEvent(DomainEvent event) {
		switch(event.getId()) {
		case JMX_CONNECT:
			enumerateBrokers();
			break;
		}
	}

	private void enumerateBrokers() {
		worker.execute(new Task(domain) {
			@Override
			public void execute() throws Exception {
				domain.enumerateBrokers();
			}
			@Override
			public String toString() {
				return "Enumerating brokers";
			}
		});
	}
	
}
