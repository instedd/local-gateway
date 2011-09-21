package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.instedd.mobilegw.MockPhone;
import org.instedd.mobilegw.messaging.DirectedMessage;

public class MockPhoneView extends JPanel {

	private static final long serialVersionUID = 997021994297618679L;
	
	private MockPhone phone;	
	private JList messageList;

	
	public MockPhoneView(MockPhone phone) {
		super();
		this.phone = phone;
		
		initialize();
	}
	
	private void initialize() {
		setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
		setMinimumSize(new Dimension(300, 100));
		setPreferredSize(new Dimension(300, 100));
		
		setLayout(new BorderLayout());
		setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), "Phone " + phone.getNumber())));
		
		// Messages list
		messageList = new JList(phone.getListModel());
		messageList.setCellRenderer(new MockMessageCellRenderer());
		
		add(messageList, BorderLayout.CENTER);
	}
	
	public MockPhone getPhone() {
		return phone;
	}

	private class MockMessageCellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, yyyy-MM-dd");
		
		public MockMessageCellRenderer() {
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			DirectedMessage message = (DirectedMessage) value;			
			setText("<html>" + message.text + "<br><span style='font-weight:normal; font-style:italic;'>at " + dateFormat.format(message.when) + "</span></html>");
			setBorder(new EmptyBorder(5, 2, 5, 30));
			return this;
		}
	}
}
