package nl.queuemanager.ui.util;

import java.io.Serializable;
import java.util.Comparator;

import nl.queuemanager.core.jms.JMSQueue;

@SuppressWarnings("serial")
public class MessageCountComparator implements Comparator<JMSQueue>, Serializable {

	private boolean descending = false;
	
	public MessageCountComparator(boolean descending) {
		this.descending = descending;
	}
	
	public int compare(JMSQueue o1, JMSQueue o2) {
		return descending ? 
			o2.getMessageCount() - o1.getMessageCount() : 
			o1.getMessageCount() - o2.getMessageCount();
	}

}
