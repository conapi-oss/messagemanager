package nl.queuemanager;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Profile implements Comparable<Profile> {
	private String id;
	private String name;
	private byte[] iconData;
	private Icon icon;
	private String description;
	
	/**
	 * This is a list of jar file names that should/could/may be present on the classpath
	 * when loading this profile. The UI will search for these jars whenever an entry is
	 * added to the classpath to help the user in selecting the correct jars.
	 */
	private List<String> jars = new ArrayList<String>();
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
	
	public byte[] getIconData() {
		return iconData;
	}

	public void setIconData(byte[] iconData) {
		this.iconData = iconData;
	}

	public Icon getIcon() {
		if(icon != null) {
			return icon;
		}

		if(getIconData() != null) {
			return new ImageIcon(getIconData());
		}
		
		return null;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<String> getJars() {
		return jars;
	}

	public void setJars(List<String> jars) {
		this.jars = jars;
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
	
	/**
	 * Create a copy of the source profile with a new Id.
	 * @param source
	 */
	public static Profile copyOf(Profile source) {
		final Profile profile = new Profile();
		profile.setName(source + " (copy)");
		profile.setIconData(source.getIconData());
		profile.setDescription(source.getDescription());
		profile.getJars().addAll(source.getJars());
		profile.getPlugins().addAll(source.getPlugins());
		profile.getClasspath().addAll(source.getClasspath());
		return profile;
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

	@Override
	public int compareTo(Profile o) {
		return getName().compareTo(o.getName());
	}

}
