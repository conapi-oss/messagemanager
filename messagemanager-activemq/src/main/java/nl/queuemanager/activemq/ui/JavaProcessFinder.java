package nl.queuemanager.activemq.ui;

import java.util.List;

public interface JavaProcessFinder {
	public boolean isSupported();
	
	public abstract List<JavaProcessDescriptor> find();
}