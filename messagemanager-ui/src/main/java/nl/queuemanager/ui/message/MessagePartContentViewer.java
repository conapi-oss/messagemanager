package nl.queuemanager.ui.message;

import javax.swing.JComponent;

import nl.queuemanager.core.jms.JMSPart;

public interface MessagePartContentViewer extends ContentViewer<JMSPart> {
	
	/**
	 * Examines the message and determines whether this Content Viewer supports 
	 * displaying the message part content.
	 * 
	 * @param message
	 * @return
	 */
	public boolean supports(JMSPart part);

	/**
	 * Create and return the user interface component for this message.
	 * 
	 * @return
	 */
	public JComponent createUI(JMSPart part);
	
}
