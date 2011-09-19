package org.instedd.mobilegw.ui;

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.instedd.mobilegw.CommTest;
import org.instedd.mobilegw.ConfigTicketManager;
import org.instedd.mobilegw.GatewayTester;
import org.instedd.mobilegw.ModemTester;
import org.instedd.mobilegw.Settings;
import org.instedd.mobilegw.SkypeAuthorizer;
import org.instedd.mobilegw.ConfigTicketService.ConfigTicketHandler;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog implements ConfigTicketHandler
{
	private Settings settings;
	
	private JComboBox modemComPort;
	private JTextField modemComBaudRate;
	private JTextField modemManufacturer;
	private JTextField modemModel;
	private JCheckBox modemAppendPlus;
	private JTextField gatewayUrl;
	private JTextField gatewayUsername;
	private JPasswordField gatewayPassword;
	private JTextField httpProxyHost, httpProxyPort;
	private JTextField httpsProxyHost, httpsProxyPort;
	private JTabbedPane rootTabbedPane;
	private JLabel loopTestLabel;
	private JTextField mobileNumber;
	private JCheckBox skypeEnabed;
	private JLabel generateCodeLabel;
	private JLabel codeLabel;
	private JLabel codeInstructions;
	private JTextField codeField;
	private Component modemTab, skypeTab, gatewayTab;
	
	private boolean codePolling;
	private Logger logger;

	public SettingsDialog(Frame owner)
	{
		super(owner);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.logger = Logger.getLogger(SettingsDialog.class.getName());
		initialize();
	}

	public void loadSettings(Settings settings)
	{
		this.settings = settings;

		modemComPort.setModel(new DefaultComboBoxModel(getAvailableComPorts()));
		modemComPort.setSelectedItem(settings.getComPort());
		modemComBaudRate.setText(Integer.toString(settings.getComBaudRate()));
		modemManufacturer.setText(settings.getModemManufacturer());
		modemModel.setText(settings.getModemModel());
		mobileNumber.setText(settings.getMobileNumber());
		modemAppendPlus.setSelected(settings.getAppendPlus());

		gatewayUrl.setText(settings.getGatewayUrl());
		gatewayUsername.setText(settings.getGatewayUsername());
		gatewayPassword.setText(settings.getGatewayPassword());
		httpProxyHost.setText(settings.getHttpProxyHost());
		httpProxyPort.setText(settings.getHttpProxyPort());
		httpsProxyHost.setText(settings.getHttpsProxyHost());
		httpsProxyPort.setText(settings.getHttpsProxyPort());
		
		skypeEnabed.setSelected(settings.getSkypeEnabled());
	}

	private void initialize()
	{
		setSize(600, 400);
		setLocationRelativeTo(null);
		rootPane.putClientProperty("Window.style", "small");
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 10, 5, 10);

		c.gridy = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		add(rootTabbedPane = getRootTabbedPane(), c);

		c.gridy = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(getOkCancelPanel(), c);
	}

	private Component getOkCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		panel.add(new JButton(new OkAction()), c);
		panel.add(new JButton(new CancelAction()), c);
		return panel;
	}

	private JTabbedPane getRootTabbedPane()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Modem", modemTab = getModemTab());
		tabbedPane.addTab("Gateway", gatewayTab = getGatewayTab());
		tabbedPane.addTab("Skype", skypeTab = getSkypeTab());
		return tabbedPane;
	}

	private Component getModemTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 5, 10);
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridx = 0;

		panel.add(new JLabel("Serial port:"), c);
		panel.add(new JLabel("Baud Rate:"), c);
		panel.add(new JLabel("Manufacturer:"), c);
		panel.add(new JLabel("Model:"), c);
		panel.add(new JLabel("Mobile number:"), c);
		
		c.gridwidth = 2;
		
		JPanel appendPanel = new JPanel();
		appendPanel.setLayout(new BoxLayout(appendPanel, BoxLayout.X_AXIS));
		appendPanel.add(new JLabel("Append a \"+\" in outgoing addresses:"), c);
		appendPanel.add(modemAppendPlus = new JCheckBox(), c);
		panel.add(appendPanel, c);
		
		panel.add(getLoopTestLabel(), c);
		panel.add(getAutodetectModemLabel(), c);
		c.gridwidth = 1;
		c.weighty = 2;
		panel.add(new JPanel(), c);

		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		panel.add(modemComPort = new JComboBox(), c);
		c.gridy = GridBagConstraints.RELATIVE;
		modemComPort.setEditable(true);
		panel.add(modemComBaudRate = new JTextField(), c);
		panel.add(modemManufacturer = new JTextField(), c);
		panel.add(modemModel = new JTextField(), c);
		panel.add(mobileNumber = new JTextField(), c);

		return panel;
	}

	private Component getLoopTestLabel()
	{
		loopTestLabel = new JLabel("<html><a href='#'>Loop test</a></html>");
		loopTestLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		loopTestLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (validateValues()) {
					saveValues();
					ModemTester modemTester = new ModemTester(settings);
					ProgressDialog progressDialog = new ProgressDialog(SettingsDialog.this);
					modemTester.startTest(progressDialog);
				}
			}
		});

		return loopTestLabel;
	}
	
	private Component getAutodetectModemLabel()
	{
		JLabel autodetectLabel = new JLabel("<html><a href='#'>Autodetect modem</a></html>");
		autodetectLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		autodetectLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				final CommTest commTester = new CommTest();
				ProgressDialog progressDialog = new ProgressDialog(SettingsDialog.this);
				commTester.test(progressDialog, new Runnable() {
					@Override
					public void run()
					{
						modemManufacturer.setText(commTester.getManufacturer().replaceAll(" ", ""));
						modemModel.setText(commTester.getModel());
						modemComPort.setSelectedItem(commTester.getFoundPort());
						modemComBaudRate.setText(Integer.toString(commTester.getFoundBauds()));
					}
				});
			}
		});
		return autodetectLabel;
	}

	private Component getGatewayTab()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 5, 10);

		c.gridy = 0;
		c.weightx = 0;
		panel.add(new JLabel("Gateway URL:"), c);

		c.weightx = 3;
		panel.add(gatewayUrl = new JTextField(), c);

		c.gridy = 1;
		c.weightx = 0;
		panel.add(new JLabel("Username:"), c);

		c.weightx = 3;
		panel.add(gatewayUsername = new JTextField(), c);

		c.gridy = 2;
		c.weightx = 0;
		panel.add(new JLabel("Password:"), c);

		c.weightx = 3;
		panel.add(gatewayPassword = new JPasswordField(), c);
		
		c.gridy = 3;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(new JLabel("HTTP Proxy:"), c);
		c.gridx = 1;
		c.weightx = 2;
		panel.add(httpProxyHost = new JTextField(), c);
		c.gridx = 2;
		c.weightx = 1;
		panel.add(httpProxyPort = new JTextField(), c);

		c.gridy = 4;
		c.gridx = 0;
		c.weightx = 0;
		panel.add(new JLabel("HTTPS Proxy:"), c);
		c.gridx = 1;
		c.weightx = 2;
		panel.add(httpsProxyHost = new JTextField(), c);
		c.gridx = 2;
		c.weightx = 1;
		panel.add(httpsProxyPort = new JTextField(), c);
		
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 4;
		panel.add(getTestGatewayLabel(), c);
		
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 4;
		panel.add(getAutoconfigureCodeLabel(), c);
		
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0;
		c.gridwidth = 1;
		panel.add(codeLabel = new JLabel("Generated code:"), c);
		
		c.gridx = 1;
		c.weightx = 1;
		panel.add(codeField = new JTextField(), c);
		codeField.setEditable(false);
		
		c.gridx = 0;
		c.gridy = 8;
		c.gridwidth = 4;
		panel.add(codeInstructions = new JLabel("<html>Enter the code above in the application to complete the automated configuration process.</html>"), c);
		
		c.gridy = 9;
		c.weighty = 2;
		c.gridwidth = 4;
		panel.add(new JPanel(), c);

		setConfigurationCodeVisible(false);
		return panel;
	}

	protected void startConfigureCode() {
		if (codePolling) return;
		codePolling = true;
		
		// Check if mobile number is set
		mobileNumber.setBackground(Color.WHITE);
		if (mobileNumber.getText().trim().isEmpty()) { 
			JOptionPane.showMessageDialog(SettingsDialog.this, "Please enter the mobile phone number to proceed with automated configuration", "Error", JOptionPane.ERROR_MESSAGE);
			mobileNumber.setBackground(Color.PINK);
			rootTabbedPane.setSelectedComponent(modemTab);
			codePolling = false;
			return;
		}
		
		// Setup ticket manager
		ConfigTicketManager ticketManager;
		try {
			ticketManager = new ConfigTicketManager(settings.getNuntiumUrl(), this, logger);
		} catch (MalformedURLException urlex) {
			JOptionPane.showMessageDialog(SettingsDialog.this, "The URL for contacting the configuration server was invalid. Ensure that the LGW_NUNTIUM_URL environment value has a valid value and restart this application.", "Error", JOptionPane.ERROR_MESSAGE);
			codePolling = false;
			return;
		}
		
		// Show the configuration code
		try {
			String code = ticketManager.start(mobileNumber.getText());
			setConfigurationCodeVisible(true);
			codeField.setText(code);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(SettingsDialog.this, "There was an error reaching the configuration server:\n" + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
			codePolling = false;
			return;
		}		
	}
	
	private void setConfigurationCodeVisible(boolean visible) {
		codeField.setVisible(visible);
		codeLabel.setVisible(visible);
		codeInstructions.setVisible(visible);
	}
	
	private JLabel getAutoconfigureCodeLabel() {
		generateCodeLabel = new JLabel("<html><a href='#'>Generate code for automatic configuration</a></html>");
		generateCodeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		final SettingsDialog dialog = this;
		
		generateCodeLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dialog.startConfigureCode();
			}
		});
		
		return generateCodeLabel;
	}

	private Component getTestGatewayLabel()
	{
		JLabel testGatewayLabel = new JLabel("<html><a href='#'>Test gateway</a></html>");
		testGatewayLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		testGatewayLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (validateValues()) {
					saveValues();
					GatewayTester commTester = new GatewayTester();
					ProgressDialog progressDialog = new ProgressDialog(SettingsDialog.this);
					commTester.test(settings, progressDialog);
				}
			}
		});
		return testGatewayLabel;
	}
	
	private Component getSkypeTab()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(skypeEnabed = new JCheckBox("Use Skype for delivering messages"));
		skypeEnabed.setAlignmentX(LEFT_ALIGNMENT);
				
		panel.add(Box.createVerticalStrut(5));
		panel.add(getAuthorizeSkypeLink());

		return panel;
	}
	
	private Component getAuthorizeSkypeLink()
	{
		JLabel authorizeLink = new JLabel("<html><a href='#'>Authorize mobile number in skype</a></html>");
		authorizeLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		authorizeLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (validateValues()) {
					saveValues();
					SkypeAuthorizer skypeAuthorizer = new SkypeAuthorizer();
					ProgressDialog progressDialog = new ProgressDialog(SettingsDialog.this);
					skypeAuthorizer.authorize(settings, progressDialog);
				}
			}
		});
		return authorizeLink;
	}
	
	@SuppressWarnings("unchecked")
	private Vector<String> getAvailableComPorts()
	{
		Vector<String> comPorts = new Vector<String>();
		Enumeration<CommPortIdentifier> portIdentifiers = CommPortIdentifier.getPortIdentifiers();

		while (portIdentifiers.hasMoreElements()) {
			CommPortIdentifier portIdentifier = portIdentifiers.nextElement();
			if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL)
				comPorts.add(portIdentifier.getName());
		}

		return comPorts;
	}

	private boolean validateValues()
	{
		boolean valid = true;

		// Clear other errors
		mobileNumber.setBackground(Color.WHITE);
		
		// Validate the modem com port
		modemComPort.setBackground(Color.WHITE);
		if (modemComPort.getSelectedItem() == null) {
			modemComPort.setBackground(Color.PINK);
			rootTabbedPane.setSelectedIndex(0);
			valid = false;
		}

		// Validate modem baud rate
		modemComBaudRate.setBackground(Color.WHITE);
		try {
			Integer.parseInt(modemComBaudRate.getText());
		} catch (NumberFormatException e) {
			modemComBaudRate.setBackground(Color.PINK);
			rootTabbedPane.setSelectedIndex(0);
			valid = false;
		}

		// Validate gateway URL
		gatewayUrl.setBackground(Color.WHITE);
		try {
			new URL(gatewayUrl.getText());
		} catch (MalformedURLException e) {
			gatewayUrl.setBackground(Color.PINK);
			rootTabbedPane.setSelectedIndex(1);
			valid = false;
		}

		return valid;
	}

	private void saveValues()
	{
		settings.setComPort(modemComPort.getSelectedItem().toString());
		settings.setComBaudRate(Integer.parseInt(modemComBaudRate.getText()));
		settings.setModemManufacturer(modemManufacturer.getText());
		settings.setModemModel(modemModel.getText());
		settings.setMobileNumber(mobileNumber.getText());
		settings.setGatewayUrl(gatewayUrl.getText());
		settings.setGatewayUsername(gatewayUsername.getText());
		settings.setGatewayPassword(new String(gatewayPassword.getPassword()));
		settings.setSkypeEnabled(skypeEnabed.isSelected());
		settings.setHttpProxyHost(httpProxyHost.getText());
		settings.setHttpProxyPort(httpProxyPort.getText());
		settings.setHttpsProxyHost(httpsProxyHost.getText());
		settings.setHttpsProxyPort(httpsProxyPort.getText());
		settings.setAppendPlus(modemAppendPlus.isSelected());
	}

	private class OkAction extends AbstractAction
	{
		public OkAction()
		{
			this.putValue(NAME, "OK");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (validateValues()) {
				saveValues();
				
				try {
					settings.save();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(SettingsDialog.this, ex.getMessage(), "Unable to save settings", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				setVisible(false);
			}
		}
	}

	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			putValue(NAME, "Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}

	@Override
	public void handleConfiguration(Map<String, String> data) {
		try {
			String url = settings.getNuntiumUrl().toString() + "/" + data.get("account") + "/qst";
			gatewayUrl.setText(url);
			gatewayUsername.setText(data.get("channel"));
			gatewayPassword.setText(data.get("password"));
			JOptionPane.showMessageDialog(SettingsDialog.this, "You have successfully configured this gateway.\n" + data.get("message"), "Success", JOptionPane.INFORMATION_MESSAGE);
			setConfigurationCodeVisible(false);
			codePolling = false;
		} catch (MalformedURLException e) {
			JOptionPane.showMessageDialog(SettingsDialog.this, "There was an error processing the URL, please restart the application and try again", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
}