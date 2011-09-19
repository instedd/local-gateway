package org.instedd.mobilegw;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.instedd.mobilegw.messaging.Message;

import com.skype.Profile;
import com.skype.SMS;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.SMS.FailureReason;
import com.skype.SMS.Status;

public class SkypeDaemon extends Daemon implements MessageChannel {

	private List<QueuedMessage> pendingMessages = Collections.synchronizedList(new LinkedList<QueuedMessage>());
	private final MessageNotificationHandler notificationHandler;
	
	public SkypeDaemon(MessageNotificationHandler notificationHandler, Logger logger)
	{
		super(logger);
		this.notificationHandler = notificationHandler;
		setWaitInterval(2000);
		setName("Skype");
	}
	
	@Override
	protected void process() throws InterruptedException
	{
		// Minimal verification that Skype is up and running
		try {
			if (!Skype.isRunning()) {
				setFailed("Skype is not running");
				return;
			}
			
			Profile profile = Skype.getProfile();
			if (profile == null || !isOnlineStatus(profile.getStatus())) {
				setFailed("Skype is not connected");
				return;
			}
		} catch (SkypeException e) {
			setFailed("Skype error: " + e.getMessage());
			return;
		}
		clearFailed();
		
		// Looks for delivery reports
		ListIterator<QueuedMessage> iterator = pendingMessages.listIterator();
		while (iterator.hasNext()) {
			QueuedMessage msg = iterator.next();
			
			// Obtain the message status
			Status status;
			try {
				status = msg.sms.getStatus();
			} catch (SkypeException e) {
				logger.warning("Error obtaining delivery report from Skype: " + e.getMessage());
				setFailed("Skype error: " + e.getMessage());
				break;
			}
			
			if (status == Status.DELIVERED) 
			{
				logger.info("Skype reported message as delivered");
				iterator.remove();
				notificationHandler.delivered(msg.message);
			} else if (status == Status.FAILED) {
				
				// If the message has failed the reason is analyzed. For permanent reasons
				// like insufficient credit the daemon is stopped completely. Otherwise it's marked
				// as failed and wont be used again until previous checks passed.
				
				FailureReason failureReason;
				try {
					failureReason = msg.sms.getFailureReason();
				} catch (SkypeException e) {
					failureReason = FailureReason.UNKNOWN;
				}
				
				logger.info("Skype reported message as failed: " + failureReason);
				iterator.remove();
				notificationHandler.failed(msg.message);
				
				if (isFatalSkypeError(failureReason)) {
					logger.warning("Delivery through Skype has been stopped. Solve the issue and restart the gateway.");
					stop();
					break;
				} else {
					setFailed("Skype failure: " + failureReason.toString());
				}
			}
		}
	}

	private boolean isOnlineStatus(Profile.Status status)
	{
		switch (status) {
			case AWAY:
			case DND:
			case INVISIBLE:
			case ONLINE:
			case SKYPEME:
				return true;

			case LOGGEDOUT:
			case NA:
			case OFFLINE:
			case UNKNOWN:
			default:
				return false;
		}
	}

	private boolean isFatalSkypeError(FailureReason failureReason)
	{
		switch (failureReason) {
			case INSUFFICIENT_FUNDS:
			case INVALID_CONFIRMATION_CODE:
			case IP_BLOCKED:
			case NODE_BLOCKED:
			case NO_SMS_CAPABILITY:
			case SERVER_CONNECT_FAILED:
			case USER_BLOCKED:
				return true;
	
			case MISC_ERROR:
			case UNKNOWN:
				return false;
	
			default:
				return true;
		}
	}

	public boolean sendMessage(Message message) throws Exception
	{
		String recipient = message.to.replace("sms://", "");
		if (!recipient.startsWith("+"))
			recipient = "+" + recipient;
		logger.info("Sending message with Skype: " + message.toString());
		SMS sms = Skype.sendSMS(recipient, message.text);
		pendingMessages.add(new QueuedMessage(message, sms));
		return false;
	}
	
	private class QueuedMessage
	{
		private Message message;
		private SMS sms;
		
		public QueuedMessage(Message message, SMS sms)
		{
			this.message = message;
			this.sms = sms;
		}
	}
}
