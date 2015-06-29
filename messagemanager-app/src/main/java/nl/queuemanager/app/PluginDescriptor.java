package nl.queuemanager.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginDescriptor {

	private String name;
	private String description;
	private String moduleClass;
	private List<URL> classpath = new ArrayList<URL>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getModuleClassName() {
		return moduleClass;
	}
	public void setModuleClass(String moduleClass) {
		this.moduleClass = moduleClass;
	}
	public List<URL> getClasspath() {
		return classpath;
	}
	public void setClasspath(List<? extends URL> classpath) {
		ArrayList<URL> cp = new ArrayList<URL>();
		cp.addAll(classpath);
		this.classpath = Collections.unmodifiableList(cp);
	}
	
}
