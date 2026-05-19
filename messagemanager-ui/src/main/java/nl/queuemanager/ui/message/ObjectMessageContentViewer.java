package nl.queuemanager.ui.message;

import nl.queuemanager.jms.impl.MessageFactory;

import jakarta.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.swing.*;
import java.io.Serializable;

class ObjectMessageContentViewer implements MessageContentViewer {

	private final TextMessageContentViewer textViewer;

	@Inject
	public ObjectMessageContentViewer(TextMessageContentViewer textViewer) {
		this.textViewer = textViewer;
	}

	@Override
	public boolean supports(Message message) {
		return message instanceof ObjectMessage;
	}

	@Override
	public String getDescription(Message message) {
		return "Object";
	}

	@Override
	public JComponent createUI(Message message) {
		try {
			TextMessage substitute = MessageFactory.createTextMessage();
			substitute.setText(describe((ObjectMessage) message));
			return textViewer.createUI(substitute);
		} catch (JMSException e) {
			JTextArea area = new JTextArea("ObjectMessage: " + e.getMessage());
			area.setEditable(false);
			return new JScrollPane(area);
		}
	}

	private String describe(ObjectMessage message) {
		try {
			Serializable obj = message.getObject();
			return obj == null
				? "ObjectMessage: (null body)"
				: "This is an ObjectMessage of class: " + obj.getClass().getName();
		} catch (JMSException e) {
			for (Throwable c = e; c != null; c = c.getCause()) {
				if (c instanceof ClassNotFoundException) {
					return "This is an ObjectMessage of class: " + c.getMessage()
						+ "\n(Class is not available in Message Manager, body cannot be deserialized.)";
				}
			}
			return "ObjectMessage: " + e.getMessage();
		}
	}
}
