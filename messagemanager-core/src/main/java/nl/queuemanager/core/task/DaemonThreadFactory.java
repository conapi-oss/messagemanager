package nl.queuemanager.core.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {
	
	private static final AtomicInteger counter = new AtomicInteger(0);

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, "MultiQueueTaskExecutorThread " + counter.incrementAndGet());
		t.setDaemon(true);
		return t;
	}

}
