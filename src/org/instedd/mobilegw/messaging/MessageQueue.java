package org.instedd.mobilegw.messaging;

public interface MessageQueue
{

	/**
	 * Enqueues the given messages
	 * 
	 * @param messages
	 * @throws Exception 
	 */
	void enqueue(Message[] messages) throws Exception;

	/**
	 * Receives the specified maximum messages, specifying the time that will
	 * elapse before retry of the receive will be performed.
	 * 
	 * @param max
	 * @param retryWaitSeconds
	 * @return
	 * @throws Exception 
	 */
	Message[] dequeue(int max, int retryWaitSeconds) throws Exception;

	/**
	 * Deletes all messages prior and including the given identifier.
	 * 
	 * @param untilIdInclusive
	 * @throws Exception 
	 */
	void deleteBatch(String untilIdInclusive) throws Exception;

	/**
	 * Delete a message from the queue
	 * 
	 * @param messageId
	 * @throws Exception
	 */
	void delete(String messageId) throws Exception;

	/**
	 * Sets an event listener for the queue
	 * 
	 * @param listener
	 */
	void addMessageQueueListener(MessageQueueListener listener);

	/**
	 * Returns an iterator for the enqueued messages
	 * 
	 * @return
	 */
	Iterable<Message> iterateMessages();
	
	/**
	 * Returns the number of messages in the queue
	 * 
	 * @return
	 * @throws Exception 
	 */
	int getMessageCount() throws Exception;

	/**
	 * Force specified message to immediate processing (clears the retry time)
	 * 
	 * @param messageId
	 */
	void force(String messageId) throws Exception;
}
