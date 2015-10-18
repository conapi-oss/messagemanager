package nl.queuemanager.activemq;

import java.util.Collections;
import java.util.List;

import nl.queuemanager.activemq.ui.JavaProcessDescriptor;
import nl.queuemanager.activemq.ui.JavaProcessFinder;

public class DummyProcessFinder implements JavaProcessFinder {

	@Override
	public List<JavaProcessDescriptor> find() {
		return Collections.emptyList();
	}

}
