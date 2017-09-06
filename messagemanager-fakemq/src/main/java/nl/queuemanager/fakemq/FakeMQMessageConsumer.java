package nl.queuemanager.fakemq;

import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

public class FakeMQMessageConsumer implements MessageConsumer {

	private final Timer timer;

	private MessageListener messageListener;
	
	public FakeMQMessageConsumer(MessageListener listener) {
		this.messageListener = listener;
		
		timer = new Timer();
		timer.schedule(postMessage, 1000, 1000);
	}
	
	private final TimerTask postMessage = new TimerTask() {
		@Override
		public void run() {
			try {
				if(getMessageListener() != null) {
					getMessageListener().onMessage(FakeMQMessageCreator.createRandomMessage());
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	};
	
	public MessageListener getMessageListener() throws JMSException {
		return messageListener;
	}

	public void setMessageListener(MessageListener listener) throws JMSException {
		this.messageListener = listener;
	}
	
	public String getMessageSelector() throws JMSException {
		return null;
	}

	public Message receive() throws JMSException {
		return null;
	}

	public Message receive(long timeout) throws JMSException {
		return null;
	}

	public Message receiveNoWait() throws JMSException {
		return null;
	}

	public void close() throws JMSException {
		timer.cancel();
	}

}
