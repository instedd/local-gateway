package org.instedd.mobilegw;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.instedd.mobilegw.helpers.PhoneHelper;
import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;

public class MockPhone {

	public final static String PREFIX = "+999";
	
	private String number;
	private DirectedMessageStore store;
	private DefaultListModel listModel;
	
	public MockPhone(String number, DirectedMessageStore store) {
		this.number = number;
		this.store = store;
		this.listModel = new DefaultListModel();
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public MockPhone initialize() {
		String phone = PhoneHelper.withSmsProtocol(number);
		for(DirectedMessage message : store.iterateMessages(phone)) {
			listModel.addElement(message);
		} return this;
	}
	
	public ListModel getListModel() {
		return listModel;
	}
	
}
