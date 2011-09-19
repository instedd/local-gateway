package org.instedd.mobilegw;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.Service;
import org.smslib.Message.MessageTypes;
import org.smslib.Service.ServiceStatus;
import org.smslib.modem.SerialModemGateway;

import com.skype.CommandFailedException;
import com.skype.Skype;


public class SkypeAuthorizer
{
	private static Pattern codePattern = Pattern.compile("the code for phone \\d+ is (\\w+)", Pattern.CASE_INSENSITIVE);
	private Thread thread;
	
	public void authorize(Settings settings, ProgressListener listener)
	{
		thread = new Thread(new TestThread(settings, listener));
		thread.start();
	}
	
	private class TestThread implements Runnable
	{
	
		private final Settings settings;
		private final ProgressListener listener;

		public TestThread(Settings settings, ProgressListener listener)
		{
			this.settings = settings;
			this.listener = listener;
		}

		public void run()
		{
			listener.start("Skype callback number authorization");
			final Service smslibService = new Service();
			final Object waitObject = new Object();

			try
			{
				SerialModemGateway gateway = new SerialModemGateway("modem", settings.getComPort(), settings.getComBaudRate(), settings.getModemManufacturer(), settings.getModemModel());
				gateway.setOutbound(true);

				smslibService.addGateway(gateway);
				InboundHandler inboundHandler = new InboundHandler(waitObject, smslibService);
				smslibService.setInboundMessageNotification(inboundHandler);

				if (listener.isCanceled()) return;

				listener.statusChange("Setting up the modem");
				smslibService.startService();

				if (listener.isCanceled()) return;

				try {
					listener.statusChange("Sending authorization code");
					Skype.submitConfirmationCode("+" + settings.getMobileNumber());
				} catch (CommandFailedException cmdex) {
					// Skype may return a 580 code even if the sending was successful,
					// so we wait for the message to arrive as well if that is the case. 
					if (cmdex.getCode() != 580) {
						throw cmdex;
					}
				}
				
				if (listener.isCanceled()) return;

				listener.statusChange("Waiting for reply");
				synchronized (waitObject) {
					waitObject.wait(60000);
				}

				if (listener.isCanceled()) return;

				if (inboundHandler.getCode() == null) {
					listener.statusChange("Timeout");
					listener.completed(false);
					return;
				}

				try {
					listener.statusChange("Code received. Completing authorization");
					Skype.submitConfirmationCode("+" + settings.getMobileNumber(), inboundHandler.getCode());
				} catch (CommandFailedException cmdex) {
					// Skype may return a 580 code even if the confirmation was successful,
					// so we assume it is correct.  
					if (cmdex.getCode() != 580) {
						throw cmdex;
					}
				}
				
				listener.statusChange("Done");
				listener.completed(true);

			} catch (Throwable e) {

				listener.statusChange(e.getMessage());
				listener.completed(false);

			} finally {
				if (smslibService.getServiceStatus() == ServiceStatus.STARTED)
					smslibService.stopService();
				listener.end();
			}
		}
	}
	
	private final class InboundHandler implements IInboundMessageNotification
	{
		private final Object waitObject;
		private final Service smslibService;
		private String code;

		private InboundHandler(Object waitObject, Service smslibService)
		{
			this.waitObject = waitObject;
			this.smslibService = smslibService;
		}

		@Override
		public void process(String gatewayId, MessageTypes msgType, InboundMessage msg)
		{
			Matcher matcher = codePattern.matcher(msg.getText());
			if (matcher.matches()) {
				try {
					code = matcher.group(1);
					smslibService.deleteMessage(msg);
					synchronized (waitObject) {
						waitObject.notify();
					}
				} catch (Exception e) {
					// Ignore this error
				}
			}
		}
		
		public String getCode()
		{
			return code;
		}
	}
}
