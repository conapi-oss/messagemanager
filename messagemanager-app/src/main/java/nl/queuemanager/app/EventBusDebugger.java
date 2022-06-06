package nl.queuemanager.app;

import com.google.common.eventbus.Subscribe;

import java.util.logging.Logger;

public class EventBusDebugger {

	private Logger logger = Logger.getLogger(getClass().getName());

	@Subscribe
	public void event(Object event) {
		logger.info("Event: " + event.toString());
	}

}
