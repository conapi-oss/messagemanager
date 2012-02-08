package nl.queuemanager.core.tasks;

import java.util.List;

import javax.inject.Inject;

import nl.queuemanager.core.Pair;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.jms.JMSQueue;

import com.google.inject.assistedinject.Assisted;

public class MoveMessageListTask extends Task {
	private final JMSDomain domain;
	private final JMSQueue toQueue;
	private final List<Pair<JMSQueue, String>> messageList;

	@Inject
	private MoveMessageListTask(@Assisted JMSQueue toQueue, @Assisted List<Pair<JMSQueue, String>> messageList, JMSDomain domain) {
		super(toQueue.getBroker());
		this.toQueue = toQueue;
		this.messageList = messageList;
		this.domain = domain;
	}

	@Override
	public void execute() throws Exception {
		int i = 0;
		for(Pair<JMSQueue, String> messageInfo: messageList) {
			final JMSQueue fromDst = messageInfo.first();
			final String messageID = messageInfo.second();
			domain.forwardMessage(fromDst, toQueue, messageID);
			dispatchEvent(new TaskEvent(TaskEvent.EVENT.TASK_PROGRESS, i++, this));
		}
	}

	@Override
	public int getProgressMaximum() {
		return messageList.size();
	}

	@Override
	public String toString() {
		return "Moving " + messageList.size() + " message to " + toQueue;
	}
}
