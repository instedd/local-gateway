package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.instedd.mobilegw.MockPhone;
import org.instedd.mobilegw.Settings;
import org.instedd.mobilegw.messaging.DirectedMessageStore;

public class MockMessagesView extends JPanel {

	private static final long serialVersionUID = 5796933430384777706L;
	
	private JPanel phonesPanel;
	
	private Map<String, MockPhoneView> phones;
	private DirectedMessageStore store;

	private JTextField phoneField;
	
	public MockMessagesView(DirectedMessageStore store, Settings settings) {
		super();
		
		this.store = store;
		this.phones = new HashMap<String, MockPhoneView>();
		
		initialize();
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
	}

	private JPanel getAddPhonePanel() {
		JPanel addPhonePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addPhonePanel.add(new JLabel("Add a new phone: "));
		
		JTextField fixedPrefixField = new JTextField(MockPhone.PREFIX);
		fixedPrefixField.setEditable(false);
		addPhonePanel.add(fixedPrefixField);
		
		phoneField = new JTextField();
		phoneField.setColumns(10);
		addPhonePanel.add(phoneField);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPhone(MockPhone.PREFIX + phoneField.getText());
			}
		});
		addPhonePanel.add(addButton);
		
		return addPhonePanel;
	}

	public boolean addPhone(String number) {
		if (this.phones.containsKey(number)) return false;
		MockPhone phone = new MockPhone(number, store).initialize();
		MockPhoneView view = new MockPhoneView(phone);
		phones.put(number, view);
		phonesPanel.add(view);
		phonesPanel.setSize(300 * phones.size(), 400);
		return true;
	}
	
}
