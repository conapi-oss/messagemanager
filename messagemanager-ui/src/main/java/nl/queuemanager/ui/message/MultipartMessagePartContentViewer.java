package nl.queuemanager.ui.message;

import javax.swing.JComponent;

import nl.queuemanager.core.jms.JMSMultipartMessage;
import nl.queuemanager.core.jms.JMSPart;

import com.google.inject.Provider;

class MultipartMessagePartContentViewer implements MessagePartContentViewer {

	private final Provider<MessageViewerPanel> panelProvider;
	
	public MultipartMessagePartContentViewer(Provider<MessageViewerPanel> panelProvider) {
		this.panelProvider = panelProvider;
	}

	public JComponent createUI(JMSPart part) {
		MessageViewerPanel panel = panelProvider.get();
		panel.setMessage((JMSMultipartMessage)part.getContent());
		return panel;
	}

	public boolean supports(JMSPart part) {
		return JMSMultipartMessage.class.isAssignableFrom(part.getContent().getClass());
	}		
}
