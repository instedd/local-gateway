package org.instedd.mobilegw;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.instedd.mobilegw.helpers.PhoneHelper;
import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.DirectedMessageStoreListener;
import org.instedd.mobilegw.messaging.DirectedMessage.Direction;

public class MockPhone implements DirectedMessageStoreListener {

	public final static String NUMERIC_PREFIX = "999";
	public final static String PREFIX = "+" + NUMERIC_PREFIX;
	
	private String number;
	private DirectedMessageStore store;
	private DefaultListModel listModel;
	private String gatewayNumber;

	private static class EventThreadListModel extends DefaultListModel {
		private static final long serialVersionUID = -4389671662282607290L;
		
		private void invokeOnEventThread(Runnable r) {
			try {
				if (!SwingUtilities.isEventDispatchThread()) {
					SwingUtilities.invokeAndWait(r);
				} else {
					r.run();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		@Override
		protected void fireContentsChanged(final Object source, final int index0, final int index1) {
			invokeOnEventThread(new Runnable() {
				public void run() {
					EventThreadListModel.super.fireContentsChanged(source, index0, index1);
				}
			});
		}
		
		@Override
		protected void fireIntervalAdded(final Object source, final int index0, final int index1) {
			invokeOnEventThread(new Runnable() {
				public void run() {
					EventThreadListModel.super.fireIntervalAdded(source, index0, index1);
				}
			});
		}
		
		@Override
		protected void fireIntervalRemoved(final Object source, final int index0, final int index1) {
			invokeOnEventThread(new Runnable() {
				public void run() {
					EventThreadListModel.super.fireIntervalRemoved(source, index0, index1);
				}
			});
		}
		
	}
	
	public MockPhone(String number, DirectedMessageStore store, String gatewayNumber) {
		this.number = number;
		this.store = store;
		this.listModel = new EventThreadListModel();
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
		store.addDirectedMessageStoreListener(this);
		
		return this;
	}
	
	public void close() {
		store.removeDirectedMessageStoreListener(this);
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
		listModel.clear();
	}

	private void addMessage(final DirectedMessage message) {
		listModel.addElement(message);
	}

	@Override
	public void messageAdded(DirectedMessage message) {
		String phone = PhoneHelper.withSmsProtocol(number);
		if ((message.isAO() && message.to.equals(phone)) || (message.isAT() && message.from.equals(phone))) {
			addMessage(message);
		}
	}
	
}
