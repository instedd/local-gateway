package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.instedd.mobilegw.MockPhone;
import org.instedd.mobilegw.messaging.DirectedMessage;

public class MockPhoneView extends JPanel {

	private static final long serialVersionUID = 997021994297618679L;
	
	private MockPhone phone;	

	private MockPhonesViewHandler mockPhonesViewHandler;
	
	private JTextArea messageArea;
	private JList messagesList;
	private JButton sendButton;

	public MockPhoneView(MockPhone phone, MockPhonesViewHandler handler) {
		super();
		this.phone = phone;
		this.mockPhonesViewHandler = handler;
		initialize();
	}
	
	public interface MockPhonesViewHandler {
		void removePhone(String phoneNumber);
	}
	
	public MockPhone getPhone() {
		return phone;
	}

	public void setMessagingEnabled(boolean enabled) {
		messageArea.setEnabled(enabled);
		sendButton.setEnabled(enabled);		
		messageArea.setOpaque(enabled);
	}
	
	private void close() {
		this.mockPhonesViewHandler.removePhone(this.getPhone().getNumber());
	}

	private void clearMessages() {
		// TODO Auto-generated method stub
	}

	private void initialize() {
		setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
		setMinimumSize(new Dimension(300, 0));
		setPreferredSize(new Dimension(300, 0));
		
		setLayout(new BorderLayout());
		setBorder(new CompoundBorder(
			BorderFactory.createEmptyBorder(10, 5, 10, 5), 
			BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.DARK_GRAY, 2), 
				"Phone " + phone.getNumber())));
		
		// Messages list
		addMessagesList();
		
		// Add send message panel
		addActionsPanel();
	}

	private void addMessagesList() {
		final ListModel listModel = phone.getListModel(); 
		listModel.addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {}
			public void contentsChanged(ListDataEvent e) {}
			
			public void intervalAdded(ListDataEvent e) {
				int lastIndex = listModel.getSize()-1;
				if (lastIndex >= 0) messagesList.ensureIndexIsVisible(lastIndex);
			}
		});
		
		messagesList = new JList(listModel) {
			private static final long serialVersionUID = 684598854120339844L;
			public boolean getScrollableTracksViewportWidth() {
	            return true;
	        }
		};
		
		messagesList.setCellRenderer(new MockMessageCellRenderer());		
		
		// Horrible hack for multiline rows with text areas copied from Stackoverflow, but it works
		ComponentListener l = new ComponentAdapter() {
	        public void componentResized(ComponentEvent e) {
	        	messagesList.setFixedCellHeight(10);
	        	messagesList.setFixedCellHeight(-1);
	        }
	    };

	    messagesList.addComponentListener(l);
	    JScrollPane scrollPane = new JScrollPane(messagesList);
	    
	    add(scrollPane, BorderLayout.CENTER);
	}
	
	private JLabel getClosePhoneLabel() {
		JLabel closePhonelabel = new JLabel("<html><a href='#'>Close phone</a></html>");
		closePhonelabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		closePhonelabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				MockPhoneView.this.close();
			}
		});
		
		return closePhonelabel;
	}

	private JLabel getClearMessagesLabel() {
		JLabel clearMessagesLabel = new JLabel("<html><a href='#'>Clear messages</a></html>");
		clearMessagesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		clearMessagesLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				MockPhoneView.this.clearMessages();
			}
		});
		
		return clearMessagesLabel;
	}

	private void addActionsPanel() {
		JPanel actionsPanel = new JPanel(new GridBagLayout());
		actionsPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.DARK_GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.7;
		c.gridwidth = 2;
		
		JLabel sendLabel = new JLabel("Send Message:");
		sendLabel.setOpaque(true);
		actionsPanel.add(sendLabel, c);
		
		messageArea = new JTextArea();
		messageArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		c.gridy = 1;
		c.ipady = 40;
		actionsPanel.add(messageArea, c);
		
		sendButton = new JButton("Send");
		c.gridy = 2;
		c.gridx = 1;
		c.gridwidth = 1;
		c.weightx = 0.3;
		c.ipady = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		actionsPanel.add(sendButton, c);
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = messageArea.getText();
				if (text != null && text.trim().length() > 0) {
					try {
						phone.sendMessage(text);
						messageArea.setText("");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(MockPhoneView.this, "Error sending text message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		JPanel linksPanel = new JPanel();
		BoxLayout linksPanelLayout = new BoxLayout(linksPanel, BoxLayout.X_AXIS);
		linksPanel.setLayout(linksPanelLayout);
		
		JLabel closePhoneLabel = getClosePhoneLabel();
		JLabel clearMessagesLabel = getClearMessagesLabel();
		
		linksPanel.add(clearMessagesLabel);
		linksPanel.add(Box.createHorizontalStrut(5));
		linksPanel.add(closePhoneLabel);
		
		c.gridy = 2;
		c.gridx = 0;
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.insets = new Insets(0,1,5,5);
		actionsPanel.add(linksPanel, c);
		
		add(actionsPanel, BorderLayout.SOUTH);
	}
	
	private class MockMessageCellRenderer implements ListCellRenderer {

		private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, yyyy-MM-dd");
		
	    private JPanel panel;
	    private JPanel containerPanel;
	    private JTextArea textArea;
	    private JLabel time;
	    
	    public MockMessageCellRenderer() {
	    	containerPanel = new JPanel();
	    	containerPanel.setLayout(new GridBagLayout());
	    	containerPanel.setBackground(Color.white); 
	    	
	    	panel = new JPanel();
	        panel.setLayout(new BorderLayout());
	        panel.setBorder(
	        	BorderFactory.createCompoundBorder(
	        		BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
	        		BorderFactory.createEmptyBorder(2, 2, 2, 2)
	        	)
	        );
	        
	        textArea = new JTextArea();
	        textArea.setLineWrap(true);
	        textArea.setWrapStyleWord(true);
	        textArea.setEditable(false);
	        textArea.setOpaque(false);
	        
	        time = new JLabel();
	        
	        panel.add(textArea, BorderLayout.CENTER);
	        panel.add(time, BorderLayout.SOUTH);
	    }

	    @Override
	    public Component getListCellRendererComponent(final JList list,
	            final Object value, final int index, final boolean isSelected,
	            final boolean hasFocus) {

	    	DirectedMessage message = (DirectedMessage) value;			
			textArea.setText(message.text);
			time.setText("<html><span style='font-weight:normal; font-style:italic;'>at " + dateFormat.format(message.when) + "</span></html>");

			GridBagConstraints c = new GridBagConstraints();
	        c.weightx = 1.0;
			
			if (message.isAO()) {
				c.insets = new Insets(5, 5, 5, 60);
				panel.setBackground(new Color(250, 240, 240));
			} else {
				c.insets = new Insets(5, 60, 5, 5);
				panel.setBackground(new Color(240, 240, 250));
			}
	        
			containerPanel.add(panel, c);
			
	        int width = list.getWidth();
	        if (width > 0) textArea.setSize(width, Short.MAX_VALUE);
	        
	        return containerPanel;
	    }
	}	
	
}
