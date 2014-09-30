package nl.queuemanager.core;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

public class DeadEventListener {

	@Subscribe
	public void processDeadEvent(DeadEvent event) {
		System.err.println("Received dead event: " + event.getEvent());
	}
	
}
