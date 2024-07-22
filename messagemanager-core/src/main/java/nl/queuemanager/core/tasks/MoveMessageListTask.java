package nl.queuemanager.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import nl.queuemanager.core.Pair;
import nl.queuemanager.core.jms.JMSDomain;
import nl.queuemanager.core.task.Task;
import nl.queuemanager.core.task.TaskEvent;
import nl.queuemanager.jms.JMSQueue;

import jakarta.inject.Inject;
import java.util.List;

public class MoveMessageListTask extends Task {
	private final JMSDomain domain;
	private final JMSQueue toQueue;
	private final List<Pair<JMSQueue, String>> messageList;

	@Inject
	private MoveMessageListTask(
			@Assisted JMSQueue toQueue, 
			@Assisted List<Pair<JMSQueue, String>> messageList, 
			JMSDomain domain, 
			EventBus eventBus) 
	{
		super(toQueue.getBroker(), eventBus);
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
			eventBus.post(new TaskEvent(TaskEvent.EVENT.TASK_PROGRESS, i++, this));
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
