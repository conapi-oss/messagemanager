package nl.queuemanager.activemq.ui;

import java.util.List;

public interface JavaProcessFinder {
	public abstract List<JavaProcessDescriptor> find();
}