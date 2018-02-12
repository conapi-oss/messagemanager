package nl.queuemanager.solace;

import javax.jms.JMSException;

import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

class SolaceQueue implements JMSQueue {

	private final MessageVPN vpn;
	private final String name;
	private final long numMessagesSpooled;
	private final long spoolUsageBytes;
	private final boolean ingressShutdown;
	private final boolean egressShutdown;
	
	public SolaceQueue(
			MessageVPN vpn, String name, long numMessagesSpooles, long spoolUsageBytes,
			boolean ingressShutdown, boolean egressShutdown) {
		this.vpn = vpn;
		this.name = name;
		this.numMessagesSpooled = numMessagesSpooles;
		this.spoolUsageBytes = spoolUsageBytes;
		this.ingressShutdown = ingressShutdown;
		this.egressShutdown = egressShutdown;
	}

	public JMSBroker getBroker() {
		return vpn;
	}

	public TYPE getType() {
		return TYPE.QUEUE;
	}

	public String getName() {
		return name;
	}
	
	public boolean isIngressShutdown() {
		return ingressShutdown;
	}

	public boolean isEgressShutdown() {
		return egressShutdown;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof JMSDestination) 
			&& ((JMSDestination)o).getType().equals(getType())
			&& ((JMSDestination)o).getName().equals(getName());
	}
	
	public int compareTo(JMSDestination o) {
		return getName().compareTo(o.getName());
	}
	
	public String getQueueName() throws JMSException {
		return getName();
	}

	public int getMessageCount() {
		return (int)numMessagesSpooled;
	}

	@Override
	public long getMessageSize() {
		return spoolUsageBytes;
	}
	
	@Override
	public String toString() {
		String res = getName();
		
		if(isIngressShutdown() && isEgressShutdown()) {
			return res += " (shutdown)";
		}
		
		if(isIngressShutdown()) {
			return res += " (ingress shutdown)";
		}
		
		if(isEgressShutdown()) {
			return res += " (egress shutdown)";
		}
		
		return res;
	}

}
