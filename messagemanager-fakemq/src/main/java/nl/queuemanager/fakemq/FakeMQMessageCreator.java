package nl.queuemanager.fakemq;

import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.impl.MessageFactory;

import javax.jms.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class FakeMQMessageCreator {

	private static final Random random = new Random();
	
	public static Message createRandomMessage() throws JMSException {
		switch(random.nextInt(4)) {
		case 0: return createTextMessage();
		case 1: return createBytesMessage();
		case 2: return createMapMessage();
		case 3: return createMultipartMessage();
		default:
			throw new JMSException("Failed to create random message");
		}
	}
	
	public static List<Message> createRandomMessages(int amount) throws JMSException {
		List<Message> msgs = new ArrayList<>(amount);
		while(amount-- > 0) {
			msgs.add(createRandomMessage());
		}
		return msgs;
	}
	
	public static TextMessage createTextMessage() throws JMSException {
		TextMessage msg = MessageFactory.createTextMessage();
		msg.setText("<xml>" + UUID.randomUUID() + "</xml>");
		setProps(msg);
		return msg;
	}
	
	public static JMSMultipartMessage createMultipartMessage() throws JMSException {
		JMSMultipartMessage msg = MessageFactory.createMultipartMessage();
		setProps(msg);
		msg.addPart(msg.createPart("<xml>" + UUID.randomUUID() + "</xml>", JMSPart.CONTENT_XML));
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(16);
			DataOutputStream dos = new DataOutputStream(bos);
			UUID uuid = UUID.randomUUID();
			dos.writeLong(uuid.getMostSignificantBits());
			dos.writeLong(uuid.getLeastSignificantBits());
			msg.addPart(msg.createPart(bos.toByteArray(), JMSPart.CONTENT_BYTES));
		} catch(IOException e) {
			throw new JMSException(e.getMessage());
		}
		return msg;
	}
	
	public static BytesMessage createBytesMessage() throws JMSException {
		BytesMessage msg = MessageFactory.createBytesMessage();
		UUID uuid = UUID.randomUUID();
		msg.writeLong(uuid.getMostSignificantBits());
		msg.writeLong(uuid.getLeastSignificantBits());
		setProps(msg);
		msg.reset();
		return msg;
	}
	
	public static MapMessage createMapMessage() throws JMSException {
		MapMessage msg = MessageFactory.createMapMessage();
		msg.setBoolean("boolean", true);
		msg.setByte("byte", (byte)42);
		msg.setBytes("bytes", new byte[] {42, 21});
		msg.setChar("char", 'P');
		msg.setDouble("double", 42.42d);
		msg.setFloat("float", 123.456f);
		msg.setInt("int", 237865);
		msg.setLong("long", 87654321234L);
		msg.setShort("short", (short)16384);
		msg.setString("string", "Some string");
		setProps(msg);
		return msg;
	}
	
    private static void setProps(Message msg) {
        try {
            msg.setStringProperty("prop1", UUID.randomUUID().toString());
            msg.setStringProperty("prop2", UUID.randomUUID().toString());
            msg.setStringProperty("prop3", UUID.randomUUID().toString());
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
