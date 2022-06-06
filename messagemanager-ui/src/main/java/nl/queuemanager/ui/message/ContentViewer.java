package nl.queuemanager.ui.message;

import javax.swing.*;

interface ContentViewer<T> {

	/**
	 * Examines the object and determines whether this Content Viewer supports 
	 * displaying its content.
	 * 
	 * @param message
	 * @return
	 */
	public boolean supports(T object);
	
	/**
	 * Create and return the user interface component to display this object.
	 * 
	 * @return
	 */
	public JComponent createUI(T object);

	/**
	 * Get a description for the type of this content (Xml, Text, etc).
	 * 
	 * @param object
	 * @return
	 */
	public String getDescription(T object);
}
