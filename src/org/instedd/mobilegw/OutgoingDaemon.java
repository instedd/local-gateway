package org.instedd.mobilegw;

import java.util.logging.Logger;

import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;
import org.instedd.mobilegw.messaging.MessageQueueListener;

/**
 * Monitors the outgoing queue and sends messages
 */
public class OutgoingDaemon extends Daemon
{
	private final MessageQueue mtQueue;
	private final OutgoingHandler outgoingHandler;
	private final Logger logger;

	public OutgoingDaemon(MessageQueue mtQueue, OutgoingHandler outgoingHandler, Logger logger)
	{
		this.mtQueue = mtQueue;
		this.outgoingHandler = outgoingHandler;
		this.logger = logger;
		
		mtQueue.addMessageQueueListener(new OutgoingQueueListener());
	}

	@Override
	protected void process() throws InterruptedException
	{
		try {
			while (getState() == DaemonState.RUNNING) {
				Message[] messages = mtQueue.dequeue(1, 60 * 5);
				for (Message message : messages) {

					if (message.retries > 5) {
						logger.warning("Message with id " + message.id + " was deleted because it exceeded the number of reries");
						mtQueue.delete(message.id);
						continue;
					}

					if (outgoingHandler.sendMessage(message))
						mtQueue.delete(message.id);
				}

				// Sleep only if there was no messages. Otherwise, continue dequeuing.
				if (messages.length == 0)
					break;
			}
		} catch (Exception e) {
			logger.severe("Error during message delivery: " + e.getMessage());
		}
	}

	public interface OutgoingHandler
	{
		boolean sendMessage(Message message) throws InterruptedException;
	}

	private class OutgoingQueueListener implements MessageQueueListener
	{
		@Override
		public void messagesDeleted()
		{
		}

		@Override
		public void messagesDequeued(Message[] dequeuedMessages)
		{
		}

		@Override
		public void messagesEnqueued(Message[] enqueuedMessages)
		{
			wakeUp();
		}
	}
}
