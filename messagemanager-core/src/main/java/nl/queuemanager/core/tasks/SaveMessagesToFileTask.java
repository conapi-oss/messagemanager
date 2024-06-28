/**

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.ESBMessage;
import nl.queuemanager.core.MessageManagerMessage;
import nl.queuemanager.core.Pair;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.jms.JMSMultipartMessage;
import nl.queuemanager.jms.JMSPart;
import nl.queuemanager.jms.MessageType;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SaveMessagesToFileTask extends Task implements CancelableTask {
	private final List<Pair<javax.jms.Message, File>> messages;
	private final String msgExtension;
	private volatile boolean canceled;
	
	@Inject
	public SaveMessagesToFileTask(
			@Assisted List<Pair<javax.jms.Message, File>> messages, 
			@Assisted String msgExtension,
			EventBus eventBus) {
		super(null, eventBus);
		
		this.messages = messages;
		this.msgExtension = msgExtension;
	}
	
	@Override
	public void execute() throws Exception {
		
		int i = 0;
		for(Pair<javax.jms.Message, File> pair: messages) {
			if(canceled) return;
			saveSingleMessage(pair.first(), pair.second());
			eventBus.post(new TaskEvent(TaskEvent.EVENT.TASK_PROGRESS, i++, this));
		}
	}

	private void saveSingleMessage(javax.jms.Message message, File file) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, JMSException {
		if(msgExtension!=null) {
			saveAsMessage(message, file,msgExtension);
		} else {
			saveRegularMessage(message, file);
		}
	}

	private void saveRegularMessage(javax.jms.Message message, File file)
			throws JMSException, IOException {
		switch(MessageType.fromClass(message.getClass())) {
		case TEXT_MESSAGE:
		case XML_MESSAGE:
		case BYTES_MESSAGE:
			saveSingleFile(message, file);
			break;
			
		case MULTIPART_MESSAGE:
			saveMultipleFiles((JMSMultipartMessage)message, file);
			break;
			
		default:
			throw new RuntimeException("Save message of type " + message.getClass().getName() + " is not implemented");
		}
	}

	static void saveMultipleFiles(JMSMultipartMessage message, File directory) throws JMSException, IOException {
		if(!directory.isDirectory()) {
			if(!directory.mkdirs()) {
				throw new RuntimeException("Unable to create directory: " + directory.getName());
			}
		}

		File baseFilename = createFilenameForMessage(message, directory);

		for (int i = 0; i < message.getPartCount(); i++) {
			File partFile = createFilenameWithExtension(
					message.getPart(i), new File(baseFilename.getAbsolutePath() + "_PART" + i));
			FileOutputStream fos = new FileOutputStream(partFile);
			fos.write(message.getPart(i).getContentBytes());
			fos.close();
		}
	}

	private void saveSingleFile(javax.jms.Message message, File file) throws JMSException, IOException {
		File realFile = file;
		
		// Create a filename when a directory has been selected
		if(realFile.isDirectory())
			realFile = createFilenameForMessage(message, realFile);

		// If the file does not have an extension, determine one based on the message (type)
		if(realFile.getName().indexOf('.') == -1) {
			realFile = createFilenameWithExtension(message, realFile);
		}
		
		// Write the message content to the file
		FileOutputStream fos = new FileOutputStream(realFile);
		switch(MessageType.fromClass(message.getClass())) {
		case TEXT_MESSAGE:
		case XML_MESSAGE:
			fos.write(((TextMessage)message).getText().getBytes());
			break;
			
		case BYTES_MESSAGE:
			BytesMessage bm = (BytesMessage)message;
			bm.reset();
			byte[] data = new byte[(int)bm.getBodyLength()];
			bm.readBytes(data);
			fos.write(data);
			break;
		}
		fos.close();
	}

	static File createFilenameForMessage(javax.jms.Message message, File directory) throws JMSException {
		String name = String.format("%d-%s", message.getJMSTimestamp(), message.getJMSMessageID().replace(':', '_'));
		return new File(directory, name);
	}
	
	static File createFilenameWithExtension(javax.jms.Message message, File file) {
		final MessageType type = MessageType.fromClass(message.getClass());
		return new File(file.getAbsolutePath() + type.getExtension());
	}
	
	static File createFilenameWithExtension(JMSPart part, File file) {
		if(part.getContentType().endsWith("xml"))
			return new File(file.getAbsolutePath() + ".xml");

		if(part.getContentType().endsWith("json"))
			return new File(file.getAbsolutePath() + ".json");

		if(part.getContentType().startsWith("text"))
			return new File(file.getAbsolutePath() + ".txt");
		
		return new File(file.getAbsolutePath() + ".bin");
	}

	private void saveAsMessage(javax.jms.Message message, File file, String msgExtension) throws ParserConfigurationException, IOException, TransformerFactoryConfigurationError, TransformerException, JMSException {
		File realFile = file;
		
		// Create a filename when a directory has been selected
		if(realFile.isDirectory())
			realFile = createFilenameForMessage(message, realFile);
		
		// If there is no extension, append .esbmsg
		if(realFile.getName().indexOf('.') == -1) {
			realFile = new File(realFile.getAbsolutePath() + msgExtension);
		}
		
		// Save the message to file
		if(msgExtension.equals(ESBMessage.getFileExtension())) {
			new ESBMessage().saveToFile(message, realFile);
		}
		else if(msgExtension.equals(MessageManagerMessage.getFileExtension())) {
			new MessageManagerMessage().saveToFile(message, realFile);
		}
		else {
			throw new RuntimeException("Unknown message extension: " + msgExtension);
		}

	}
	
	@Override
	public int getProgressMaximum() {
		return messages.size();
	}

	@Override
	public String toString() {
		return "Saving " + messages.size() + " message(s)";
	}

	public void cancel() {
		this.canceled = true;
	}
}
