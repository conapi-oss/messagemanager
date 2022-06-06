package nl.queuemanager.ui;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.MessageType;
import nl.queuemanager.ui.util.HighlighterSupport;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessageHighlighter extends HighlighterSupport<Message> {

	private final Logger log = Logger.getLogger(getClass().getName());
	
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
				if(shouldHighlightObj(value))
					return true;
			}
		
			switch(MessageType.fromClass(msg.getClass())) {
			case TEXT_MESSAGE:
			case XML_MESSAGE:
				return shouldHighlight(((TextMessage)msg).getText());
			case MAP_MESSAGE:
				return shouldHighlightMsg((MapMessage)msg);
			case MULTIPART_MESSAGE:
				final JMSMultipartMessage mpmsg = (JMSMultipartMessage)msg;
				for(int i = 0; i<mpmsg.getPartCount(); i++) {
					if(shouldHighlight(mpmsg.getPart(i))) {
						return true;
					}
				}
			}
		} catch (JMSException e) {
			log.log(Level.WARNING, "Exception while checking message for highlighting", e);
		}
		
		return false;
	}
	
	private boolean shouldHighlightMsg(MapMessage msg) throws JMSException {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = msg.getMapNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement();
			if(name.contains(searchTerm)) 
				return true;
			
			Object value = msg.getObject(name);
			if(shouldHighlightObj(value))
				return true;
		}
		
		return false;
	}

	private boolean shouldHighlightObj(Object obj) {
		return obj != null && shouldHighlight(obj.toString());
	}
	
	private boolean shouldHighlight(String text) {
		return text != null && text.contains(searchTerm);
	}
	
	private boolean shouldHighlight(JMSPart part) {
		// Check part headers
		Enumeration<String> names = part.getHeaderFieldNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement();
			if(name.contains(searchTerm)) 
				return true;
			
			String value = part.getHeaderField(name);
			if(shouldHighlight(value))
				return true;
		}
		
		// Check part contents
		switch(part.getContentType()) {
		case JMSPart.CONTENT_TEXT:
		case JMSPart.CONTENT_XML:
			return shouldHighlight((String)part.getContent());
			
		case JMSPart.CONTENT_MESSAGE:
			return shouldHighlight((Message)part.getContent());
		}
		
		return false;
	}

}