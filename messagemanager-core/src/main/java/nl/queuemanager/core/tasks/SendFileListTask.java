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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import nl.queuemanager.core.ESBMessage;
import nl.queuemanager.core.jms.JMSDestination;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.jms.impl.MessageFactory;
import nl.queuemanager.core.task.CancelableTask;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.util.CollectionFactory;

public class SendFileListTask extends Task implements CancelableTask {
	
	private final JMSDestination queue;
	private final List<File> fileList;
	private final Message template;
	private final int repeats;
	private final int delay;
	private final JMSDomain sonic;
	private volatile boolean canceled;

	public SendFileListTask(JMSDestination queue, File file, Message template, JMSDomain sonic) {
		this(queue, Collections.singletonList(file), template, 1, 0, sonic);
	}
	
	public SendFileListTask(JMSDestination queue, File file, Message template, int repeats, int delay, JMSDomain sonic) {
		this(queue, Collections.singletonList(file), template, repeats, delay, sonic);
	}
	
	public SendFileListTask(JMSDestination queue, List<File> files, JMSDomain sonic) {
		this(queue, files, null, 1, 0, sonic);
	}
	
	public SendFileListTask(JMSDestination queue, List<File> files, int repeats, JMSDomain sonic) {
		this(queue, files, null, repeats, 0, sonic);
	}
	
	public SendFileListTask(JMSDestination queue, List<File> files, Message template, int repeats, int delay, JMSDomain sonic) {
		super(queue.getBroker());
		
		this.queue = queue;
		this.template = template;
		this.fileList = CollectionFactory.newArrayList();
		this.repeats = repeats;
		this.delay = delay;
		this.sonic = sonic;
		
		// Walk through the list and resolve any directories
		for(File file: files) {
			if(file.isDirectory()) {
				File[] children = file.listFiles();
				for(File child: children) {
					if(child.isFile())
						fileList.add(child);
				}
			} else {
				fileList.add(file);
			}
		}
	}

	@Override
	public void execute() throws Exception {
		if(canceled) return;
		
		int i = 0;
		for(int r = 0; r<repeats; r++) {
			for(File file: fileList) {
				if(delay != 0 && i > 0)
					sleep(delay);
				
				if(file.getPath().toLowerCase().endsWith(".esbmsg")) {
					sonic.sendMessage(queue, ESBMessage.readFromFile(file));
				} else {
					Message message = composeMessage(template, readFile(file), file);
					replaceFields(message, i+1);
					sonic.sendMessage(queue, message);
				}
				
				reportProgress(i++);
				if(canceled) return;
			}
		}
	}
	
	/**
	 * Replace tokens like %i (message number) in the correlationid
	 * 
	 * @param message
	 * @throws JMSException 
	 */
	private void replaceFields(final Message message, final int seqNum) throws JMSException {
		if(message.getJMSCorrelationID() == null)
			return;
		
		message.setJMSCorrelationID(
				message.getJMSCorrelationID().replaceAll(
						"\\%i", Integer.toString(seqNum)));
	}

	private void sleep(final int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
	}

	private Message composeMessage(final Message template, final byte[] content, final File file) throws JMSException {
		TextMessage message;
		String fileName = file.getName().toLowerCase();
		
		// TODO Do more sensible things with non-text files
		if(fileName.endsWith(".xml")) {
			message = MessageFactory.createXMLMessage();
		} else {
			message = MessageFactory.createTextMessage();
		}
		
		// Copy the correlation id and other custom headers to the new message
		if(template != null) {
			MessageFactory.copyHeaders(template, message);
			MessageFactory.copyProperties(template, message);
		}
		
		// HACK This uses the JVM default encoding to convert to a String.
		message.setText(new String(content));
		
		return message;
	}
	
	private byte[] readFile(File file)
	{
		if(!file.exists() || !file.isFile())
			return new byte[0];
		
	    try {	    	
	    	FileInputStream fin = new FileInputStream(file);
	    	byte[] content = new byte[(int)file.length()];
	    	fin.read(content);
	    	fin.close();
	    	return content;
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    return new byte[0];
	}
	

	@Override
	public int getProgressMaximum() {
		return repeats * fileList.size();
	}

	@Override
	public String toString() {
		return "Sending " + getProgressMaximum() + " message(s) to " + queue;
	}

	public void cancel() {
		this.canceled = true;
	}

}
