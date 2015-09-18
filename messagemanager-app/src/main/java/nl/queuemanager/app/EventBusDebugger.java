package nl.queuemanager.app;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

public class EventBusDebugger {

	private Logger logger = Logger.getLogger(getClass().getName());

	@Subscribe
	public void event(Object event) {
		logger.info("Event: " + event.toString());
	}

}
