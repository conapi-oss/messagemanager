package nl.queuemanager.ui.util;

import com.google.common.eventbus.Subscribe;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.jms.DomainEvent;
import nl.queuemanager.core.task.TaskExecutor;
import nl.queuemanager.core.tasks.TaskFactory;
import nl.queuemanager.jms.JMSBroker;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class QueueCountsRefresher {
	
	private TaskExecutor worker;
	private TaskFactory taskFactory;
	private Map<JMSBroker, Integer> brokersToRefresh = new HashMap<JMSBroker, Integer>();
	private Timer timer;
	
	@Inject
	QueueCountsRefresher(TaskExecutor worker, CoreConfiguration configuration, TaskFactory taskFactory) {
		this.worker = worker;
		this.taskFactory = taskFactory;
		this.timer = new Timer(true);
		
		timer.schedule(new RefreshTask(), 
			Long.valueOf(configuration.getUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, CoreConfiguration.DEFAULT_AUTOREFRESH_INTERVAL)), 
			Long.valueOf(configuration.getUserPref(CoreConfiguration.PREF_AUTOREFRESH_INTERVAL, CoreConfiguration.DEFAULT_AUTOREFRESH_INTERVAL)));
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

	@Subscribe
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
