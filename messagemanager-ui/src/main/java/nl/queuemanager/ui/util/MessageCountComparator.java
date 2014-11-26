package nl.queuemanager.ui.util;

import java.io.Serializable;
import java.util.Comparator;

import nl.queuemanager.jms.JMSQueue;

@SuppressWarnings("serial")
public class MessageCountComparator implements Comparator<JMSQueue>, Serializable {

	private boolean descending = false;
	
	public MessageCountComparator(boolean descending) {
		this.descending = descending;
	}
	
	public int compare(JMSQueue o1, JMSQueue o2) {
		int res = descending ? 
			o2.getMessageCount() - o1.getMessageCount() : 
			o1.getMessageCount() - o2.getMessageCount();
			
		if(res == 0) { // If the message counts are equal, sort by name (always ascending)
			return o1.getName().compareTo(o2.getName());
		} else {
			return res;
		}
	}

}
