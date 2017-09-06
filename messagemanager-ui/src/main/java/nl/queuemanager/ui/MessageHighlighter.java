package nl.queuemanager.ui;

import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;

import nl.queuemanager.jms.MessageType;
import nl.queuemanager.ui.util.HighlighterSupport;

public final class MessageHighlighter extends HighlighterSupport<Message> {

	private final SimpleDateFormat dateFormatter = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");
	
	private String searchTerm = "";

	@Subscribe
	public void onGlobalHighlightEvent(GlobalHighlightEvent e) {
		searchTerm = e.getHighlightString();
		resetHighlights();
	}
	
	public boolean shouldHighlight(Message msg) {
		try {
			if(Strings.isNullOrEmpty(searchTerm))
				return false;
			
			if(msg.getJMSMessageID().contains(searchTerm))
				return true;
			
			if(msg.getJMSCorrelationID() != null && msg.getJMSCorrelationID().contains(searchTerm))
				return true;
			
			if(msg.getJMSDestination() != null && msg.getJMSDestination().toString().contains(searchTerm))
				return true;
			
			if(msg.getJMSTimestamp() > 0 && dateFormatter.format(msg.getJMSTimestamp()).contains(searchTerm))
				return true;
			
			@SuppressWarnings("unchecked")
			Enumeration<String> names = msg.getPropertyNames();
			while(names.hasMoreElements()) {
				String name = names.nextElement();
				if(name.contains(searchTerm)) 
					return true;
				
				Object value = msg.getObjectProperty(name);
				if(value != null && value.toString().contains(searchTerm))
					return true;
			}
		
			switch(MessageType.fromClass(msg.getClass())) {
			case TEXT_MESSAGE:
			case XML_MESSAGE:
				String text = ((TextMessage)msg).getText();
				return text != null && text.contains(searchTerm);
			default:
				return false;
			}
		} catch (JMSException e) {
			return false;
		}
	}

}