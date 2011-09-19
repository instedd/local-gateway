package org.instedd.mobilegw;

import java.util.UUID;

import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.Message.MessageTypes;
import org.smslib.Service.ServiceStatus;
import org.smslib.modem.SerialModemGateway;

public class ModemTester
{
	private final Settings settings;
	private Thread thread;

	public ModemTester(Settings settings)
	{
		this.settings = settings;
	}

	public void startTest(ProgressListener listener)
	{
		thread = new Thread(new TestThread(listener));
		thread.start();
	}

	private class TestThread implements Runnable
	{
		private final ProgressListener listener;
		private boolean received;

		public TestThread(ProgressListener listener)
		{
			this.listener = listener;
		}

		public void run()
		{
			listener.start("Modem Loopback Test");
			final Service smslibService = new Service();
			received = false;
			final String messageText = UUID.randomUUID().toString();
			final Object waitObject = new Object();
			
			listener.setCancelListener(new Runnable() {
				@Override
				public void run()
				{
					synchronized (waitObject) {
						waitObject.notify();
					}
				}
			});

			try
			{
				SerialModemGateway gateway = new SerialModemGateway("modem", settings.getComPort(), settings.getComBaudRate(), settings.getModemManufacturer(), settings.getModemModel());
				gateway.setOutbound(true);

				smslibService.addGateway(gateway);
				smslibService.setInboundMessageNotification(new IInboundMessageNotification()
				{
					@Override
					public void process(String gatewayId, MessageTypes msgType, InboundMessage msg)
					{
						if (msg.getText().contains(messageText)) {
							try {
								received = true;
								smslibService.deleteMessage(msg);
								synchronized (waitObject) {
									waitObject.notify();
								}
							} catch (Exception e) {
								// Ignore this error
							}
						}
					}
				});
				
				if (listener.isCanceled()) return;

				listener.statusChange("Setting up the modem");
				smslibService.startService();
				
				if (listener.isCanceled()) return;

				listener.statusChange("Sending SMS");
				OutboundMessage message = new OutboundMessage(settings.getMobileNumber(), messageText);
				smslibService.sendMessage(message);

				if (listener.isCanceled()) return;
				
				listener.statusChange("Waiting for reply");
				synchronized (waitObject) {
					waitObject.wait(60000);
				}
				
				if (listener.isCanceled()) return;

				if (received) {
					listener.statusChange("Message received successfully");
					listener.completed(true);
				} else {
					listener.statusChange("Timeout");
					listener.completed(false);
				}

			} catch (Throwable ex) {
				listener.statusChange(ex.getMessage());
				listener.completed(false);

				if (smslibService != null)
					smslibService.stopService();
			} finally {
				if (smslibService.getServiceStatus() == ServiceStatus.STARTED)
					smslibService.stopService();
				listener.end();
			}
		}
	}
}
