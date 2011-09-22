package org.instedd.mobilegw;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListModel;

import org.instedd.mobilegw.helpers.PhoneHelper;
import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.DirectedMessage.Direction;

public class MockPhone {

	public final static String PREFIX = "+999";
	
	private String number;
	private DirectedMessageStore store;
	private List<MockPhoneListener> listeners;
	
	private DefaultListModel listModel;

	public MockPhone(String number, DirectedMessageStore store) {
		this.number = number;
		this.store = store;
		this.listeners = new ArrayList<MockPhoneListener>();
		this.listModel = new DefaultListModel();
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
		String phone = PhoneHelper.withSmsProtocol(number);
		for(DirectedMessage message : store.iterateMessages(phone)) {
			addMessage(message);
		} return this;
	}
	
	public void addListener(MockPhoneListener listener) {
		this.listeners.add(listener);
	}
	
	public void sendMessage(String text) throws Exception {
		DirectedMessage message = new DirectedMessage();
		message.id = UUID.randomUUID().toString();
		message.from = PhoneHelper.withSmsProtocol(this.getNumber());
		message.to = PhoneHelper.withSmsProtocol("0"); // TODO: Use configured number
		message.direction = Direction.AT;
		message.when = new Date();
		message.text = text;
		store.addMessage(message);
	}
	
	private void addMessage(DirectedMessage message) {
		listModel.addElement(message);
		for(MockPhoneListener listener : listeners) {
			listener.messageAdded(message);
		}
	}
	
	public static interface MockPhoneListener {

		void messageAdded(DirectedMessage message);
		
		void messageDeleted(DirectedMessage message);
		
	}

	
}
