package org.instedd.mobilegw;

import java.util.logging.Logger;

import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.DirectedMessageStoreListener;
import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;
import org.instedd.mobilegw.messaging.DirectedMessage.Direction;

public class MockMessageChannelDaemon extends MessageChannelDaemon
{
	private final MessageQueue moQueue;
	private final MessageQueue mtQueue;
	
	private final Logger logger;
	private final DirectedMessageStore store;

	public MockMessageChannelDaemon(DirectedMessageStore store, MessageQueue moQueue, MessageQueue mtQueue, Logger logger)
	{
		this.setName("Mock");
		
		this.moQueue = moQueue;
		this.mtQueue = mtQueue;
		this.logger = logger;
		this.store = store;
	
		this.store.addDirectedMessageStoreListener(new DirectedMessageStoreListener() {
			public void messageAdded(DirectedMessage message) {
				if (message.isAO()) {
					try {
						MockMessageChannelDaemon.this.moQueue.enqueue(new Message[] { message });
						MockMessageChannelDaemon.this.logger.info("Mock message received from " + message.from);
					} catch (Exception e) {
						MockMessageChannelDaemon.this.logger.severe("Error receiving mock message: " + e.getMessage());
					}
				}
			}
		});
	}

	@Override
	public void start()
	{
		try {
			//TODO: Start 
		} catch (Exception e) {
			logger.severe("Failed to start modem: " + e.getMessage());
			throw new Error(e);
		}
		
		super.start();

		logger.info("Mock messages daemon started successfully");
	}

	@Override
	public void stop()
	{
		super.stop();
		
		try {
			logger.info("Stopping mock messages daemon");
			// TODO: Stop me
			logger.info("Mock messages daemon stopped successfully");
		} catch (Exception e) {
			logger.severe("Failed to stop mock messages daemon: " + e.getMessage());
			throw new Error(e);
		}
	}

	@Override
	public boolean sendMessage(Message message) throws Exception {
		mtQueue.delete(message.id);
		store.addMessage(new DirectedMessage(message, Direction.AO));
		logger.info("Sending mock message: " + message.toString());
		return true;
	}
	
	@Override
	protected void process() throws InterruptedException {
		
	}
}
