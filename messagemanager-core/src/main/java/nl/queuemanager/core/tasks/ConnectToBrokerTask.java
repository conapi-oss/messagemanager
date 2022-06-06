package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.configuration.CoreConfiguration;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.core.util.UserCanceledException;
import nl.queuemanager.jms.JMSBroker;

import javax.jms.JMSException;

public class ConnectToBrokerTask extends Task {
	private final JMSDomain domain;
	private final JMSBroker broker;
	private final CoreConfiguration configuration;

	@Inject
	ConnectToBrokerTask(
			JMSDomain domain, 
			CoreConfiguration configuration,
			EventBus eventBus,
			@Assisted JMSBroker broker) 
	{
		super(broker, eventBus);
		this.domain = domain;
		this.broker = broker;
		this.configuration = configuration;
	}

	@Override
	public void execute() throws Exception {
		Credentials credentials = configuration.getBrokerCredentials(broker);
		
		// Try to connect infinitely, until the user cancels or we succeed.
		while(true) {
			try {
				domain.connectToBroker(broker, credentials);
				break;
			} catch (JMSException e) {
				credentials = domain.getCredentials(broker, credentials, e);
				if(credentials == null)
					throw new UserCanceledException();
			}
		}

		// If we have specified alternate credentials, save them.
		if(credentials != null) {
			configuration.setBrokerCredentials(broker, credentials);
		}
	}

	@Override
	public String toString() {
		return "Connecting to broker " + broker;
	}
}
