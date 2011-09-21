package org.instedd.mobilegw;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.instedd.mobilegw.OutgoingDaemon.OutgoingHandler;
import org.instedd.mobilegw.messaging.DbDirectedMessageStore;
import org.instedd.mobilegw.messaging.DbLastIdStore;
import org.instedd.mobilegw.messaging.DbMessageQueue;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;
import org.instedd.mobilegw.messaging.MessageQueueListener;
import org.instedd.mobilegw.messaging.QueueStateTransferClient;
import org.smslib.modem.SerialModemGateway;

public class Controller
{
	private static final int SKYPE_THRESHOLD = 10;
	private Connection dbConnection;
	private MessageChannelDaemon modemDaemon;
	private QueueStateTransferDaemon queueStateTransferDaemon;
	private DbMessageQueue moQueue;
	private DbMessageQueue mtQueue;
	private DbLastIdStore lastIdStore;
	private Logger logger;
	private OutgoingDaemon outgoingDaemon;
	private SkypeDaemon skypeDaemon;
	private boolean useSkype;
	private DbDirectedMessageStore mockMessagesStore;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		}
	}

	public Controller() {
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:" + new File(Main.getWorkingDirectory(), "sms.db").getAbsolutePath());
			dbConnection.setAutoCommit(false);

			moQueue = new DbMessageQueue(dbConnection, "MOMessages");
			mtQueue = new DbMessageQueue(dbConnection, "MTMessages");
			mockMessagesStore = new DbDirectedMessageStore(dbConnection, "MockMessages");
			lastIdStore = new DbLastIdStore(dbConnection);
		} catch (Exception ex) {
			throw new Error(ex);
		}
		
		mtQueue.addMessageQueueListener(new OutgoingQueueListener());
	}

	public MessageQueue getIncomingQueue()
	{
		return moQueue;
	}

	public MessageQueue getOutgoingQueue()
	{
		return mtQueue;
	}

	public DirectedMessageStore getMockMessagesStore() {
		return mockMessagesStore;
	}

	public void start(Settings settings, Handler logginHandler, DaemonListener daemonListener)
	{
		try {
			settings.applySystemProperties();
			
			SerialModemGateway gateway = new SerialModemGateway("modem", settings.getComPort(), settings.getComBaudRate(), settings.getModemManufacturer(), settings.getModemModel());
			gateway.setOutbound(true);

			logger = Logger.getAnonymousLogger();
			logger.addHandler(logginHandler);

			checkIfSkypeShouldBeUsed();
			
			modemDaemon = null;
			skypeDaemon = null;
			
			if (settings.getMockMessagesMode()) {
				// Initialize the modem daemon as a mock daemon
				modemDaemon = new MockMessageChannelDaemon(mockMessagesStore, moQueue, mtQueue, logger);
			} else {
				// Initialize the modem daemon
				modemDaemon = new ModemDaemon(moQueue, logger, settings.getAppendPlus()).withGateway(gateway);
			}
			
			modemDaemon.addListener(new ControllerDaemonListener());
			modemDaemon.addListener(daemonListener);
			modemDaemon.start();
			
			// Initialize the skype daemon
			if (!settings.getMockMessagesMode() && settings.getSkypeEnabled()) {
				skypeDaemon = new SkypeDaemon(new SkypeMessageNotificationHandler(), logger);
				skypeDaemon.addListener(new ControllerDaemonListener());
				skypeDaemon.addListener(daemonListener);
				skypeDaemon.start();
			}
						
			// Initialize the QST client			
			QueueStateTransferClient qstClient = new QueueStateTransferClient(new URL(settings.getGatewayUrl()), settings.getGatewayUsername(), settings.getGatewayPassword());
			queueStateTransferDaemon = new QueueStateTransferDaemon(moQueue, mtQueue, qstClient, lastIdStore, settings, logger);
			queueStateTransferDaemon.addListener(new ControllerDaemonListener());
			queueStateTransferDaemon.addListener(daemonListener);
			queueStateTransferDaemon.start();
			
			// Initialize the outgoing daemon
			outgoingDaemon = new OutgoingDaemon(mtQueue, new OutgoingMessageRouter(), logger);
			outgoingDaemon.start();

		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public void stop()
	{
		try {
			if (outgoingDaemon != null)
				outgoingDaemon.stop();
			if (modemDaemon != null)
				modemDaemon.stop();
			if (queueStateTransferDaemon != null)
				queueStateTransferDaemon.stop();
			if (skypeDaemon != null)
				skypeDaemon.stop();
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	private boolean isModemAvailable() {
		return modemDaemon != null && modemDaemon.getState() == DaemonState.RUNNING;
	}

	private boolean isSkypeAvailable() {
		return skypeDaemon != null && skypeDaemon.getState() == DaemonState.RUNNING;
	}

	private void checkIfSkypeShouldBeUsed()
	{
		try {
			if (!useSkype && mtQueue.getMessageCount() > SKYPE_THRESHOLD) {
				logger.info("Too many messages in queue. Switching to Skype");
				useSkype = true;
			}
		} catch (Exception e) {
			logger.severe(e.getMessage());
		}
	}

	private final class SkypeMessageNotificationHandler implements MessageNotificationHandler
	{
		@Override
		public void failed(Message message)
		{
			logger.warning("Message reported as failed");
		}

		@Override
		public void delivered(Message message)
		{
			try {
				mtQueue.delete(message.id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class OutgoingMessageRouter implements OutgoingHandler
	{
		@Override
		public boolean sendMessage(Message message) throws InterruptedException
		{
			MessageChannel[] channels = getChannels();
			for (MessageChannel channel : channels) {
				try {
					return channel.sendMessage(message);
				} catch (Exception e) {
					logger.warning("Error sending message: " + e.getMessage());
				}
			}
			
			logger.warning("No channel available for sending message");
			return false;
		}
		
		private MessageChannel[] getChannels() {
			List<MessageChannel> channels = new ArrayList<MessageChannel>();
			
			if (isModemAvailable()) {
				channels.add(modemDaemon);
			}
			if (isSkypeAvailable()) {
				if (useSkype) channels.add(0, skypeDaemon);
				else channels.add(skypeDaemon);
			}
			
			return (MessageChannel[]) channels.toArray(new MessageChannel[channels.size()]);
		}
	}
	
	private class OutgoingQueueListener implements MessageQueueListener
	{
		@Override
		public void messagesDeleted()
		{
			try {
				if (useSkype && mtQueue.getMessageCount() == 0) {
					logger.info("Queue is empty. Switching to mobile phone");
					useSkype = false;
				}
			} catch (Exception e) {
				logger.severe(e.getMessage());
			}
		}

		@Override
		public void messagesDequeued(Message[] dequeuedMessages)
		{
		}

		@Override
		public void messagesEnqueued(Message[] enqueuedMessages)
		{
			checkIfSkypeShouldBeUsed();
		}
		
	}
	
	private class ControllerDaemonListener implements DaemonListener
	{
		@Override
		public void stateChanged(Daemon daemon, DaemonState oldState, DaemonState newState)
		{
			logger.info("Daemon " + daemon.getClass().getSimpleName() + " changed state to " + newState);
			
			if (outgoingDaemon == null)
				return;
			
			boolean atLeastOneChannelRunning = isModemAvailable() || isSkypeAvailable();
			if (outgoingDaemon.getState() == DaemonState.RUNNING) {
				if (!atLeastOneChannelRunning)
					outgoingDaemon.stop();
			} else {
				if (atLeastOneChannelRunning)
					outgoingDaemon.start();
			}
		}
	}
}
