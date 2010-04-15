package nl.queuemanager.ui.message;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

class BytesMessageViewer extends HexEditorContentViewer<Message> implements MessageContentViewer {

	@Override
	public byte[] getContent(final Message message) {
		BytesMessage bm = (BytesMessage)message;
		
		try {
			byte[] data = new byte[(int)bm.getBodyLength()];
			bm.reset();
			bm.readBytes(data);
			return data;
		} catch (JMSException e) {
			return null;
		}
	}

	public boolean supports(Message message) {
		return BytesMessage.class.isAssignableFrom(message.getClass());
	}

	public String getDescription(Message object) {
		return "Bytes";
	}

}
