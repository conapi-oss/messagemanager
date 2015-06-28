package nl.queuemanager.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Profile {

	private String name;
	private List<PluginDescriptor> plugins = new ArrayList<PluginDescriptor>();
	private List<URL> classpath = new ArrayList<URL>();
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<PluginDescriptor> getPlugins() {
		return plugins;
	}
	
	public void setPlugins(List<PluginDescriptor> plugins) {
		this.plugins = plugins;
	}
	
	public List<URL> getClasspath() {
		return classpath;
	}
	
	public void setClasspath(List<URL> classpath) {
		this.classpath = classpath;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
