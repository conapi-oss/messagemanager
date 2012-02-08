package nl.queuemanager.core.tasks;

import javax.jms.JMSSecurityException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import nl.queuemanager.core.Configuration;
import nl.queuemanager.core.jms.BrokerCredentialsProvider;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.util.Credentials;
import nl.queuemanager.core.util.UserCanceledException;
import nl.queuemanager.jms.JMSBroker;

public class ConnectToBrokerTask extends Task {
	private final JMSDomain domain;
	private final JMSBroker broker;
	private final Configuration configuration;
	private final BrokerCredentialsProvider credentialsProvider;

	@Inject
	public ConnectToBrokerTask(
			JMSDomain domain, 
			Configuration configuration,
			BrokerCredentialsProvider credentialsProvider,
			@Assisted JMSBroker broker) 
	{
		super(broker);
		this.domain = domain;
		this.broker = broker;
		this.configuration = configuration;
		this.credentialsProvider = credentialsProvider;
	}

	@Override
	public void execute() throws Exception {
		Credentials credentials = configuration.getBrokerCredentials(broker);

		// Try to connect infinitely, until the user cancels or we succeed.
		while(true) {
			try {
				domain.connectToBroker(broker, credentials);
				break;
			} catch (JMSSecurityException e) {
				credentials = credentialsProvider.getCredentials(broker, credentials, e);
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
