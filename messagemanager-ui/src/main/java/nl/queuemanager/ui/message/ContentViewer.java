package nl.queuemanager.ui.message;

import javax.swing.JComponent;

interface ContentViewer<T> {

	public boolean supports(T object);
	
	public JComponent createUI(T object);

}
