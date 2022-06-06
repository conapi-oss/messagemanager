package nl.queuemanager.fakemq;

import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

import javax.jms.JMSException;
import java.util.Random;

public class FakeMQQueue implements JMSQueue {

        private final FakeMQBroker broker;
        private final String name;
        private final int messageCount;
        private final int size;

        public FakeMQQueue(FakeMQBroker broker, String name, int messageCount) {
            this.broker = broker;
            this.name = name;
            this.messageCount = messageCount;
            this.size = new Random().nextInt(1024*1024*1024);
        }
    
	public JMSBroker getBroker() {
            return broker;
	}

	public TYPE getType() {
            return TYPE.QUEUE;
	}

	public String getName() {
            return name;
	}

	public int compareTo(JMSDestination o) {
            return getName().compareTo(o.getName());
	}

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof FakeMQQueue) 
            && getName().equals(((FakeMQQueue)obj).getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

	public String getQueueName() throws JMSException {
            return getName();
	}

	public int getMessageCount() {
            return messageCount;
	}

	public long getMessageSize() {
            return size;
	}
        
        @Override
        public String toString() {
            return getName();
        }

}
