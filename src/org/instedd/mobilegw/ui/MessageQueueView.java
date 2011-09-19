package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;

public class MessageQueueView extends JPanel
{
	private static final long serialVersionUID = 1L;
	public JTable messageTable = null;
	private final MessageQueue queue;
	private final static DateFormat dateFormat = DateFormat.getDateTimeInstance();
	private JButton cancelButton;
	private JButton forceButton;

	public MessageQueueView(MessageQueue queue)
	{
		super();
		this.queue = queue;

		initialize();
		refresh();
	}

	private void initialize()
	{
		setLayout(new BorderLayout());
		
		// Message grid
		messageTable = new JTable() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		add(new JScrollPane(messageTable));
		messageTable.getSelectionModel().addListSelectionListener(new MessageTableSelectionListener());
		
		// Tool bar
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(buttonsPanel, BorderLayout.NORTH);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cancelSelectedMessages();
			}
		});
		buttonsPanel.add(cancelButton);
		
		forceButton = new JButton("Force");
		forceButton.setEnabled(false);
		forceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				forceSelectedMessages();
			}
		});
		buttonsPanel.add(forceButton);
	}
	
	protected void cancelSelectedMessages()
	{
		ArrayList<String> selectedMessageIds = new ArrayList<String>();
		for (int row : messageTable.getSelectedRows()) {
			String id = (String) messageTable.getModel().getValueAt(row, 0);
			selectedMessageIds.add(id);
		}
		
		for (String id : selectedMessageIds) {
			try {
				queue.delete(id);
			} catch (Exception e) {
			}
		}
	}
	
	protected void forceSelectedMessages()
	{
		for (int row : messageTable.getSelectedRows()) {
			String id = (String) messageTable.getModel().getValueAt(row, 0);
			try {
				queue.force(id);
			} catch (Exception e) {
			}
		}
		refresh();
	}

	public void refresh()
	{
		ArrayList<String[]> rows = new ArrayList<String[]>();
		for (Message message : queue.iterateMessages()) {
			rows.add(new String[] {
					message.id,
					message.when.toString(),
					message.from,
					message.to,
					message.text,
					Integer.toString(message.retries),
					formatRetryTime(message.retryTime)
			});
		}

		messageTable.setModel(new DefaultTableModel(
				(String[][]) rows.toArray(new String[0][rows.size()]),
				new String[] { "Id", "When", "From", "To", "Text", "Retries", "Next Retry" }
		));
	}

	private String formatRetryTime(Date retryTime)
	{
		if (retryTime == null)
			return "Immediate";
		return dateFormat.format(retryTime);
	}
	
	public class MessageTableSelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			boolean hasSelectedRows = messageTable.getSelectedRows().length > 0;
			cancelButton.setEnabled(hasSelectedRows);
			forceButton.setEnabled(hasSelectedRows);
		}
	}
}
