package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;

import org.instedd.mobilegw.MockPhone;
import org.instedd.mobilegw.messaging.DirectedMessage;

public class MockPhoneView extends JPanel {

	private static final long serialVersionUID = 997021994297618679L;
	
	private MockPhone phone;	

	private JTextArea messageArea;
	private JList messagesList;
	
	public MockPhoneView(MockPhone phone) {
		super();
		this.phone = phone;
		
		initialize();
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
		messagesList = new JList(phone.getListModel()) {
			private static final long serialVersionUID = 684598854120339844L;
			public boolean getScrollableTracksViewportWidth() {
	            return true;
	        }
		};
		
		messagesList.setCellRenderer(new MockMessageCellRenderer());		
		
		ComponentListener l = new ComponentAdapter() {
	        public void componentResized(ComponentEvent e) {
	        	messagesList.setFixedCellHeight(10);
	        	messagesList.setFixedCellHeight(-1);
	        }
	    };

	    messagesList.addComponentListener(l);
	    add(new JScrollPane(messagesList), BorderLayout.CENTER);
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
		c.gridy = 1;
		c.ipady = 40;
		actionsPanel.add(messageArea, c);
		
		JButton sendButton = new JButton("Send");
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
		
		add(actionsPanel, BorderLayout.SOUTH);
	}
	
	public MockPhone getPhone() {
		return phone;
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
			textArea.setText(message.text + "123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789");
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
