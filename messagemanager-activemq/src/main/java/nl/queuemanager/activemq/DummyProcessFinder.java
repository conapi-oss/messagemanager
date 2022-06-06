package nl.queuemanager.activemq;

import nl.queuemanager.activemq.ui.JavaProcessDescriptor;
import nl.queuemanager.activemq.ui.JavaProcessFinder;

import java.util.Collections;
import java.util.List;

public class DummyProcessFinder implements JavaProcessFinder {

	@Override
	public List<JavaProcessDescriptor> find() {
		return Collections.emptyList();
	}

	@Override
	public boolean isSupported() {
		return false;
	}

}
