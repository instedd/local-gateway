package org.instedd.mobilegw;

import org.instedd.mobilegw.messaging.Message;

public interface MessageNotificationHandler
{
	void delivered(Message message);
	
	void failed(Message message);
}
