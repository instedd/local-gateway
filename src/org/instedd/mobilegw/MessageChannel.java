package org.instedd.mobilegw;

import org.instedd.mobilegw.messaging.Message;

public interface MessageChannel
{
	/**
	 * Sends a message synchronous or asynchronously
	 * 
	 * @param message
	 * @return true if the delivery of the message is confirmed, false otherwise. If the
	 * channel returns false, it should use the MessageNotificationHandler to notify the final delivery
	 */
	boolean sendMessage(Message message) throws Exception;
}
