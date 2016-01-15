package nl.queuemanager.debug;

import java.awt.AWTEvent;
import java.util.HashMap;
import java.util.Map;

class TracingEventQueueThread extends Thread {
	private long thresholdDelay;

	private Map<AWTEvent, Long> eventTimeMap;

	public TracingEventQueueThread(long thresholdDelay) {
		this.thresholdDelay = thresholdDelay;
		this.eventTimeMap = new HashMap<AWTEvent, Long>();
	}

	public synchronized void eventDispatched(AWTEvent event) {
		this.eventTimeMap.put(event, System.currentTimeMillis());
	}

	public synchronized void eventProcessed(AWTEvent event) {
		this.checkEventTime(event, System.currentTimeMillis(),
				this.eventTimeMap.get(event));
		this.eventTimeMap.put(event, null);
	}

	private void checkEventTime(AWTEvent event, long currTime, long startTime) {
		long currProcessingTime = currTime - startTime;
		if (currProcessingTime >= this.thresholdDelay) {
			System.out.println("Event [" + event.hashCode() + "] "
					+ event.getClass().getName()
					+ " is taking too much time on EDT (" + currProcessingTime
					+ ")");
		}
	}

	@Override
	public void run() {
		while (true) {
			long currTime = System.currentTimeMillis();
			synchronized (this) {
				for (Map.Entry<AWTEvent, Long> entry : this.eventTimeMap
						.entrySet()) {
					AWTEvent event = entry.getKey();
					if (entry.getValue() == null)
						continue;
					long startTime = entry.getValue();
					this.checkEventTime(event, currTime, startTime);
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}
	}
}