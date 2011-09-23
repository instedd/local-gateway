package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.instedd.mobilegw.MockPhone;
import org.instedd.mobilegw.Settings;
import org.instedd.mobilegw.helpers.PhoneHelper;
import org.instedd.mobilegw.messaging.DirectedMessage;
import org.instedd.mobilegw.messaging.DirectedMessageStore;
import org.instedd.mobilegw.messaging.DirectedMessageStoreListener;
import org.instedd.mobilegw.ui.MockPhoneView.MockPhonesViewHandler;

public class MockMessagesView extends JPanel implements MockPhonesViewHandler {

	private static final long serialVersionUID = 5796933430384777706L;
	
	private JPanel phonesPanel;
	private JTextField phoneField;
	
	private List<MockPhoneView> phones;
	private DirectedMessageStore store;

	private Settings settings;
	private boolean messagingEnabled;
	
	public MockMessagesView(DirectedMessageStore store, Settings settings) {
		super();
		
		this.store = store;
		this.settings = settings;
		this.phones = new ArrayList<MockPhoneView>();
		
		initialize();	
		loadPhones();
		setMessagingEnabled(false);
	}
	
	public boolean addPhone(String number) {
		if (hasPhone(number)) return false;
		MockPhone phone = new MockPhone(number, store, settings.getMobileNumber());
	
		MockPhoneView view = new MockPhoneView(phone, this);
		view.setMessagingEnabled(messagingEnabled);
		phone.initialize();
		phones.add(view);
		
		phonesPanel.add(view);
		phonesPanel.setSize(300 * phones.size(), 0);
		
		updateSettings();
		
		return true;
	}

	@Override
	public void removePhone(MockPhoneView view) {
		phones.remove(view);
		redrawPhones();
		updateSettings();
	}

	public void setMessagingEnabled(boolean enabled) {
		this.messagingEnabled = enabled;
		for (MockPhoneView phoneView : this.phones){
			phoneView.setMessagingEnabled(enabled);
		}
	}
	
	private boolean hasPhone(String phone) {
		for (MockPhoneView view : phones) {
			if (view.getPhone().getNumber().equals(phone)) {
				return true;
			}
		} return false;
	}

	private void redrawPhones() {
		phonesPanel.removeAll();
		for (MockPhoneView view : phones) {
			phonesPanel.add(view);
		} phonesPanel.setSize(300 * phones.size(), 0);
	}

	private void loadPhones() {
		for (String phone : settings.getMockedPhones()) {
			addPhone(phone);
		}
	}

	private void updateSettings() {
		List<String> numbers = new ArrayList<String>();
		for (MockPhoneView view : phones) {
			numbers.add(view.getPhone().getNumber());
		}
		String[] numbersArray = (String[]) numbers.toArray(new String[numbers.size()]);
		settings.setMockedPhones(numbersArray);
	}

	private void scrollToPhone(String string) {
		// TODO Check how to scroll to a specific phone		
	}

	private void initialize() {
		setLayout(new BorderLayout());
		
		phonesPanel = new JPanel();
		phonesPanel.setLayout(new BoxLayout(phonesPanel, BoxLayout.X_AXIS));
		
		JScrollPane phonesScrollPane = new JScrollPane(phonesPanel);
		phonesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		phonesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		add(phonesScrollPane, BorderLayout.CENTER);
		
		add(getAddPhonePanel(), BorderLayout.SOUTH);
	
		store.addDirectedMessageStoreListener(new DirectedMessageStoreListener() {
			@Override
			public void messageAdded(DirectedMessage message) {
				String number = PhoneHelper.withLeadingPlus(message.to);
				if(message.isAO() && !hasPhone(number)) {
					addPhone(number);
				}
			}
		});
	}

	private JPanel getAddPhonePanel() {
		JPanel addPhonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addPhonePanel.add(new JLabel("Add a new phone: "));
		
		JTextField fixedPrefixField = new JTextField(MockPhone.PREFIX);
		fixedPrefixField.setEditable(false);
		addPhonePanel.add(fixedPrefixField);
		
		phoneField = new JTextField();
		phoneField.setColumns(10);
		
		final JLabel errorLabel = new JLabel();
		errorLabel.setForeground(Color.red);
		errorLabel.setVisible(false);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				errorLabel.setVisible(false);
				String phoneNumber = phoneField.getText();
				if (PhoneHelper.isValidNumericPhone(phoneNumber)) {
					if (!addPhone(MockPhone.PREFIX + phoneNumber)) {
						errorLabel.setText("Phone already open");
						errorLabel.setVisible(true);
					}
					scrollToPhone(MockPhone.PREFIX + phoneNumber);
				} else {
					errorLabel.setText("Invalid phone number");
					errorLabel.setVisible(true);
				}
			}
		});
		
		addPhonePanel.add(phoneField);
		addPhonePanel.add(addButton);
		addPhonePanel.add(errorLabel);
		
		return addPhonePanel;
	}
	
}
