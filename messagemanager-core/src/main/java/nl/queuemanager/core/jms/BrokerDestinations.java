package nl.queuemanager.core.jms;

import lombok.Data;
import nl.queuemanager.jms.JMSBroker;
import nl.queuemanager.jms.JMSDestination;
import nl.queuemanager.jms.JMSQueue;

import java.util.List;

@Data
public class BrokerDestinations {
    private JMSBroker broker;
    private List destinations;

    public BrokerDestinations(JMSBroker broker, List destinations) {
        this.broker = broker;
        this.destinations = destinations;
    }
}
