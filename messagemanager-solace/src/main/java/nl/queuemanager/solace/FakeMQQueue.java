package nl.queuemanager.solace;


import javax.jms.JMSException;

import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

public class FakeMQQueue implements JMSQueue {

	public JMSBroker getBroker() {
		// TODO Auto-generated method stub
		return null;
	}

	public TYPE getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(JMSDestination o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getQueueName() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMessageCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getMessageSize() {
		// TODO Auto-generated method stub
		return -1;
	}

}
