package nl.queuemanager.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.events.EventListener;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.jms.JMSBroker;

@Singleton
public class QueueCountsRefresher implements EventListener<DomainEvent> {
	
	private TaskExecutor worker;
	private TaskFactory taskFactory;
	private Map<JMSBroker, Integer> brokersToRefresh = new HashMap<JMSBroker, Integer>();
	private Timer timer;
	
	@Inject
	QueueCountsRefresher(JMSDomain domain, TaskExecutor worker, Configuration configuration, TaskFactory taskFactory) {
		this.worker = worker;
		this.taskFactory = taskFactory;
		this.timer = new Timer();
		
		timer.schedule(new RefreshTask(), 
			Long.valueOf(configuration.getUserPref(Configuration.PREF_AUTOREFRESH_INTERVAL, "5000")), 
			Long.valueOf(configuration.getUserPref(Configuration.PREF_AUTOREFRESH_INTERVAL, "5000")));
			
		domain.addListener(this);
	}
	
	private synchronized void refreshBrokers() {
		for(JMSBroker broker: brokersToRefresh.keySet()) {
			worker.execute(taskFactory.enumerateQueues(broker, null));
		}
	}
	
	public synchronized void registerInterest(JMSBroker broker) {
		int count = 0;
		if(brokersToRefresh.containsKey(broker)) {
			count = brokersToRefresh.get(broker);
		}
		
		brokersToRefresh.put(broker, count+1);
	}
	
	public synchronized void unregisterInterest(JMSBroker broker) {
		if(!brokersToRefresh.containsKey(broker)) {
			// Print an exception to debug this but don't throw it. It's not a problem, really.
			new IllegalStateException("Unable to unregister interest. Count is 0 for broker " + broker)
				.printStackTrace();
			return;
		}
		
		int count = brokersToRefresh.get(broker);
		if(count == 1) {
			brokersToRefresh.remove(broker);
		} else {
			brokersToRefresh.put(broker, count - 1);
		}
	}

	public void processEvent(DomainEvent event) {
		switch(event.getId()) {
			case JMX_CONNECT:
			case JMX_DISCONNECT:
				brokersToRefresh.clear();
		}
	}

	private class RefreshTask extends TimerTask {

		@Override
		public void run() {
			refreshBrokers();
		}
		
	}
}
