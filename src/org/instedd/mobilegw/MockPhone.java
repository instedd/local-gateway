package org.instedd.mobilegw;

import java.util.Date;
import java.util.UUID;

import javax.swing.DefaultListModel;

import org.instedd.mobilegw.helpers.PhoneHelper;
import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.DirectedMessageStoreListener;
import org.instedd.mobilegw.messaging.DirectedMessage.Direction;

public class MockPhone {

	public final static String NUMERIC_PREFIX = "999";
	public final static String PREFIX = "+" + NUMERIC_PREFIX;
	
	private String number;
	private DirectedMessageStore store;
	private DefaultListModel listModel;
	private NewMessagesListener listener;
	private String gatewayNumber;

	public MockPhone(String number, DirectedMessageStore store, String gatewayNumber) {
		this.number = number;
		this.store = store;
		this.listModel = new DefaultListModel();
		this.gatewayNumber = gatewayNumber;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	public DefaultListModel getListModel() {
		return listModel;
	}

	public MockPhone initialize() {
		// Load existing messages
		String phone = PhoneHelper.withSmsProtocol(number);
		for(DirectedMessage message : store.iterateMessages(phone)) {
			addMessage(message);
		}
		
		// Listen for new messages
		store.addDirectedMessageStoreListener(listener = new NewMessagesListener());
		
		return this;
	}
	
	public void close() {
		store.removeDirectedMessageStoreListener(listener);
	}
	
	public void sendMessage(String text) throws Exception {
		DirectedMessage message = new DirectedMessage();
		message.id = UUID.randomUUID().toString();
		message.from = PhoneHelper.withSmsProtocol(this.getNumber());
		message.to = PhoneHelper.withSmsProtocol(this.gatewayNumber, "0");
		message.direction = Direction.AT;
		message.when = new Date();
		message.text = text;
		store.addMessage(message);
	}
	
	public void clearMessages() throws Exception {
		String phone = PhoneHelper.withSmsProtocol(this.number);
		store.deleteMessages(phone);
		// TODO: Check thread safety
		listModel.clear();
	}

	private void addMessage(DirectedMessage message) {
		// TODO: Check thread safety
		listModel.addElement(message);
	}

	private final class NewMessagesListener implements
			DirectedMessageStoreListener {
		public void messageAdded(DirectedMessage message) {
			String phone = PhoneHelper.withSmsProtocol(number);
			if ((message.isAO() && message.to.equals(phone)) || (message.isAT() && message.from.equals(phone))) {
				addMessage(message);
			}
		}
	}
	
}
