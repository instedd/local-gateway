package org.instedd.mobilegw;

import java.io.IOException;
import java.util.logging.Logger;

import org.instedd.mobilegw.messaging.LastIdStore;
import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;
import org.instedd.mobilegw.messaging.MessageQueueListener;
import org.instedd.mobilegw.messaging.QueueStateTransferClient;

public class QueueStateTransferDaemon extends Daemon
{
	private QueueStateTransferClient client;
	private final MessageQueue moQueue;
	private final MessageQueue mtQueue;
	private final LastIdStore lastIdStore;
	private final Settings settings;

	public QueueStateTransferDaemon(MessageQueue moQueue, MessageQueue mtQueue, QueueStateTransferClient client, LastIdStore lastIdStore, Settings settings, Logger logger)
	{
		super(logger);
		this.moQueue = moQueue;
		this.mtQueue = mtQueue;
		this.client = client;
		this.lastIdStore = lastIdStore;
		this.settings = settings;
		setName("Gateway");
		
		moQueue.addMessageQueueListener(new IncomingQueueListener());
	}

	@Override
	public synchronized void start()
	{
		
		String mobileNumber = settings.getMobileNumber();
		if (mobileNumber != null && mobileNumber.length() > 0)
		{
			try {
				client.setAddress("sms://" + mobileNumber);
			} catch (UnsupportedOperationException e) {
				logger.warning("Gateway server seems to be an old version and does not support setting the mobile number.");
			} catch (IOException e) {
				logger.warning("Error setting the mobile number on the gateway server: " + e.getMessage());
			}
		}
		
		super.start();
	}
	
	@Override
	protected void process() throws InterruptedException
	{
		try {
			// Send all queued MO messages to the server
			boolean continueSending;
			do {
				continueSending = sendMOMessages();
			} while (continueSending);

			// Receive all MT messages waiting at the server
			boolean continueReceiving;
			do {
				continueReceiving = receiveMTMessages();
			} while (continueReceiving);
			
			clearFailed();
		} catch (Exception e) {
			setFailed(e.getMessage());
		}
	}

	private boolean receiveMTMessages() throws Exception
	{
		String lastId = lastIdStore.getLastId();
		Message[] messages = client.receive(10, lastId);

		if (messages != null && messages.length > 0) {
			mtQueue.enqueue(messages);
			lastIdStore.setLastId(messages[messages.length - 1].id);
			return true;
		}

		return false;
	}

	private boolean sendMOMessages() throws Exception
	{
		Message[] messages = moQueue.dequeue(10, 30);
		if (messages != null && messages.length > 0) {
			String lastId = client.send(messages);
			moQueue.deleteBatch(lastId);
			return true;
		}

		return false;
	}
	
	private final class IncomingQueueListener implements MessageQueueListener
	{
		@Override
		public void messagesEnqueued(Message[] enqueuedMessages)
		{
			wakeUp();
		}

		@Override
		public void messagesDequeued(Message[] dequeuedMessages)
		{
		}

		@Override
		public void messagesDeleted()
		{
		}
	}
}
