package org.instedd.mobilegw.messaging;

public interface MessageQueueListener
{
	void messagesEnqueued(Message[] enqueuedMessages);
	void messagesDequeued(Message[] dequeuedMessages);
	void messagesDeleted();
}
