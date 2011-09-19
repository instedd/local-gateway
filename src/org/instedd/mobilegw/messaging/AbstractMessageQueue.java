package org.instedd.mobilegw.messaging;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractMessageQueue implements MessageQueue
{

	private Collection<MessageQueueListener> listeners = new LinkedList<MessageQueueListener>();

	@Override
	public void addMessageQueueListener(MessageQueueListener listener)
	{
		this.listeners.add(listener);
	}

	void onMessagesEnqueued(Message[] messages)
	{
		if (messages.length > 0) {
			for (MessageQueueListener listener : listeners) {
				listener.messagesEnqueued(messages);
			}
		}
	}

	void onMessagesDequeued(Message[] messages)
	{
		
		if (messages.length > 0) {
			for (MessageQueueListener listener : listeners) {
				listener.messagesDequeued(messages);
			}
		}
	}

	void onMessagesDeleted()
	{
		for (MessageQueueListener listener : listeners) {
			listener.messagesDeleted();
		}
	}
}