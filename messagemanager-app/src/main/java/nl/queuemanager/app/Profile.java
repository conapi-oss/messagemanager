package nl.queuemanager.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Profile {
	private String id;
	private String name;
	private String description;
	private List<String> plugins = new ArrayList<String>();
	private List<URL> classpath = new ArrayList<URL>();
	
	public Profile() {
		setId(UUID.randomUUID().toString());
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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
	
	public List<String> getPlugins() {
		return plugins;
	}
	
	public void setPlugins(List<String> plugins) {
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
	
	@Override
	public boolean equals(Object o) {
		return o != null
			&& o instanceof Profile
			&& ((Profile)o).getId().equals(getId());
	}
	
	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}
