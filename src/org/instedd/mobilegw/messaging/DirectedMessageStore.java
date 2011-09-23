package org.instedd.mobilegw.messaging;



/**
 * Store for directed messages, used for storing mocked messages
 * @author spalladino
 */
public interface DirectedMessageStore {

	/**
	 * Adds a new message to the store
	 * @throws Exception 
	 */
	void addMessage(DirectedMessage message) throws Exception;
	
	/**
	 * Iterates all messages in the store
	 */
	Iterable<DirectedMessage> iterateMessages();
	
	/**
	 * Iterates all messages sent to/from a specific phone 
	 */
	Iterable<DirectedMessage> iterateMessages(String phone);
	
	/**
	 * Registers a listener for the store
	 */
	void addDirectedMessageStoreListener(DirectedMessageStoreListener listener);

	/**
	 * Removes a listener from the store
	 */
	void removeDirectedMessageStoreListener(DirectedMessageStoreListener listener);
	
}
