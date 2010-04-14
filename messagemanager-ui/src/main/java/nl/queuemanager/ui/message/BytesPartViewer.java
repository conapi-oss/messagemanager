package nl.queuemanager.ui.message;

import nl.queuemanager.core.jms.JMSPart;

class BytesPartViewer extends HexEditorContentViewer<JMSPart> implements MessagePartContentViewer {
	
	@Override
	public byte[] getContent(JMSPart part) {
		Object content = part.getContent();
		
		if(content != null) {
			if(byte[].class.isAssignableFrom(content.getClass())) {
				return (byte[])content;
			} else {
				return content.toString().getBytes();
			}
		} else {
			return new byte[] {};
		}
	}

	public boolean supports(JMSPart object) {
		return true;
	}		
}
