package org.instedd.mobilegw.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.instedd.mobilegw.Controller;
import org.instedd.mobilegw.Daemon;
import org.instedd.mobilegw.DaemonListener;
import org.instedd.mobilegw.DaemonState;
import org.instedd.mobilegw.Settings;
import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueueListener;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String MOCK_MESSAGES_TAB_TITLE = "Simulate Messages";
	
	private JPanel contentPane = null;
	private EventList eventList = null;
	private Controller controller;
	private JToolBar toolBar;
	private StartAction startAction;
	private StopAction stopAction;
	private Logger logger;
	private SettingsAction settingsAction;
	private MessageQueueView incomingMessageQueueView;
	private MessageQueueView outgoingMessageQueueView;
	private MockMessagesView mockMessagesView;
	private JPanel statusBar;
	private JTabbedPane tabbedPane;
	private Settings settings;
	
	public MainWindow() {
		controller = new Controller();
		controller.getIncomingQueue().addMessageQueueListener(
				new MessageQueueListener() {
					@Override
					public void messagesEnqueued(Message[] enqueuedMessages) {
						incomingMessageQueueView.refresh();
					}

					@Override
					public void messagesDequeued(Message[] dequeuedMessages) {
						incomingMessageQueueView.refresh();
					}

					@Override
					public void messagesDeleted() {
						incomingMessageQueueView.refresh();
					}
				});

		controller.getOutgoingQueue().addMessageQueueListener(
				new MessageQueueListener() {
					@Override
					public void messagesEnqueued(Message[] enqueuedMessages) {
						outgoingMessageQueueView.refresh();
					}

					@Override
					public void messagesDequeued(Message[] dequeuedMessages) {
						outgoingMessageQueueView.refresh();
					}

					@Override
					public void messagesDeleted() {
						outgoingMessageQueueView.refresh();
					}
				});

		initialize();
		logger = Logger.getLogger(MainWindow.class.getName());
		logger.addHandler(getLoggingHandler());
	}

	private void initialize() {
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setTitle("Mobile Gateway");
		
		settings = new Settings();
		
		// Main content pane
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		this.setContentPane(contentPane);

		// Tab panel
		tabbedPane = new JTabbedPane();
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		// Event list
		eventList = new EventList();
		tabbedPane.addTab("Status", new JScrollPane(eventList));
		tabbedPane.addTab("Incoming Queue",
				incomingMessageQueueView = new MessageQueueView(controller
						.getIncomingQueue()));
		tabbedPane.addTab("Outgoing Queue",
				outgoingMessageQueueView = new MessageQueueView(controller
						.getOutgoingQueue()));

		// Mock messages
		mockMessagesView = new MockMessagesView(controller.getMockMessagesStore(), settings);
		updateVisibleTabs(settings);
		
		// Status bar
		statusBar = new JPanel();
		statusBar.setPreferredSize(new Dimension(10, 25));
		statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		contentPane.add(statusBar, BorderLayout.SOUTH);

		// ToolBar
		toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);

		startAction = new StartAction();
		styleToolbarButton(toolBar.add(startAction));

		stopAction = new StopAction();
		stopAction.setEnabled(false);
		styleToolbarButton(toolBar.add(stopAction));

		toolBar.addSeparator();
		
		settingsAction = new SettingsAction();
		styleToolbarButton(toolBar.add(settingsAction));
		
		// Listen when application exits and save settings if necessary
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try { settings.save(); } 
				catch (IOException e1) { e1.printStackTrace(); }
			}
		});
	}

	private void styleToolbarButton(JButton button) {
		button.setHideActionText(false);
		button.setVerticalTextPosition(SwingConstants.CENTER);
		button.setHorizontalTextPosition(SwingConstants.RIGHT);
		button.setFocusable(false);
	}
	
	private void updateVisibleTabs(Settings settings) {
		int currentMockTabIndex = tabbedPane.indexOfTab(MOCK_MESSAGES_TAB_TITLE);
		if (settings.getMockMessagesMode() && currentMockTabIndex < 0) {
			tabbedPane.addTab(MOCK_MESSAGES_TAB_TITLE, mockMessagesView);
		} else if (!settings.getMockMessagesMode() && currentMockTabIndex >= 0){
			tabbedPane.removeTabAt(currentMockTabIndex);
		}
	}

	public Handler getLoggingHandler() {
		return eventList.getLoggingHandler();
	}

	@SuppressWarnings("serial")
	private class StartAction extends AbstractAction {
		public StartAction() {
			putValue(NAME, "Start");
			putValue(SMALL_ICON, new ImageIcon(MainWindow.class
					.getResource("control_play_blue.png")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {
				public void run() {
					try {
						logger.info("Starting...");
						controller.start(settings, eventList
								.getLoggingHandler(), new UIDaemonListener());
						stopAction.setEnabled(true);
					} catch (Throwable ex) {
						startAction.setEnabled(true);
						settingsAction.setEnabled(true);
					}
				}
			}).start();
			startAction.setEnabled(false);
			settingsAction.setEnabled(false);
		}
	}

	@SuppressWarnings("serial")
	private class StopAction extends AbstractAction {
		public StopAction() {
			putValue(NAME, "Stop");
			putValue(SMALL_ICON, new ImageIcon(MainWindow.class
					.getResource("control_stop_blue.png")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new Thread(new Runnable() {
				public void run() {
					logger.info("Stopping...");
					controller.stop();
					startAction.setEnabled(true);
					settingsAction.setEnabled(true);
				}
			}).start();
			stopAction.setEnabled(false);
		}
	}

	@SuppressWarnings("serial")
	private class SettingsAction extends AbstractAction {
		public SettingsAction() {
			putValue(NAME, "Settings");
			putValue(SMALL_ICON, new ImageIcon(MainWindow.class
					.getResource("brick_edit.png")));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsDialog settingsDialog = new SettingsDialog(MainWindow.this);
			settingsDialog.loadSettings(settings);
			settingsDialog.addPropertyChangeListener("settings", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent pce) {
					updateVisibleTabs((Settings) pce.getNewValue());
				}
			});
			
			settingsDialog.setVisible(true);
		}
	}

	private class UIDaemonListener implements DaemonListener {
		private Map<Daemon, JLabel> daemons = new HashMap<Daemon, JLabel>();

		public UIDaemonListener() {
			statusBar.removeAll();
		}

		@Override
		public void stateChanged(Daemon daemon, DaemonState oldState,
				DaemonState newState) {
			JLabel label = getDaemonLabel(daemon);
			if (newState == DaemonState.RUNNING
					|| newState == DaemonState.STOPPING) {
				label.setIcon(new ImageIcon(MainWindow.class
						.getResource("bullet_green.png")));
				label.setToolTipText(null);
			} else if (newState == DaemonState.FAILED) {
				label.setIcon(new ImageIcon(MainWindow.class
						.getResource("bullet_red.png")));
				label.setToolTipText(daemon.getFailMessage());
			} else {
				label.setIcon(new ImageIcon(MainWindow.class
						.getResource("bullet_black.png")));
				label.setToolTipText(null);
			}
		}

		private JLabel getDaemonLabel(Daemon daemon) {
			JLabel label = daemons.get(daemon);
			if (label == null) {
				label = new JLabel(daemon.getName());
				statusBar.add(label);
				daemons.put(daemon, label);
			}
			return label;
		}
	}
}
