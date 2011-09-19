package org.instedd.mobilegw.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.text.DateFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class EventList extends JList
{
	private static final int MAX_ITEMS = 1000;
	private static final long serialVersionUID = 1L;
	private DefaultListModel listModel;

	public EventList()
	{
		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new EventCellRenderer());
	}

	public Handler getLoggingHandler()
	{
		return new Handler() {
			@Override
			public void publish(final LogRecord record)
			{
				EventQueue.invokeLater(new Runnable() {
					public void run()
					{
						int index = listModel.getSize();
						listModel.add(index, record);
						
						// Remove old items
						if (listModel.size() > MAX_ITEMS) {
							listModel.removeRange(0, listModel.size() - MAX_ITEMS - 1);
						}
						
						EventList.this.ensureIndexIsVisible(index);
					}
				});
			}

			@Override
			public void flush()
			{
			}

			@Override
			public void close() throws SecurityException
			{
			}
		};
	}

	private class EventCellRenderer extends JLabel implements ListCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private DateFormat dateFormat = DateFormat.getDateTimeInstance();
		private ImageIcon errorIcon = new ImageIcon(EventCellRenderer.class.getResource("bullet_red.png"));
		private ImageIcon warningIcon = new ImageIcon(EventCellRenderer.class.getResource("bullet_orange.png"));
		private ImageIcon infoIcon = new ImageIcon(EventCellRenderer.class.getResource("bullet_black.png"));
		
		public EventCellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			LogRecord record = (LogRecord) value;
			String text = String.format("<html><span style='color:gray'>%1$s</span> - %2$s</html>", 
					dateFormat.format(record.getMillis()), record.getMessage());
			setText(text);

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			if (record.getLevel() == Level.SEVERE) {
				setIcon(errorIcon);
			} else if (record.getLevel() == Level.WARNING){
				setIcon(warningIcon);
			} else {
				setIcon(infoIcon);
			}
			
			return this;
		}
	}
}
