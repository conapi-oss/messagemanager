package nl.queuemanager.app;

public class PluginDescriptor {

	private String name;
	private String description;
	private String moduleClass;
	
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
	
}
