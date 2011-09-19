package org.instedd.mobilegw;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.instedd.mobilegw.messaging.QueueStateTransferClient;

public class GatewayTester
{
	public void test(Settings settings, ProgressListener listener)
	{
		new Thread(new GatewayTestThread(settings, listener)).start();
	}
	
	private class GatewayTestThread implements Runnable
	{
		private final Settings settings;
		private final ProgressListener listener;
		
		public GatewayTestThread(Settings settings, ProgressListener listener)
		{
			this.settings = settings;
			this.listener = listener;
		}
		
		@Override
		public void run()
		{
			listener.start("Gateway test");
			try {
				listener.statusChange("Contacting gateway");
				settings.applySystemProperties();
				QueueStateTransferClient client = new QueueStateTransferClient(new URL(settings.getGatewayUrl()), settings.getGatewayUsername(), settings.getGatewayPassword());
				client.getLastSentMessageId();
				listener.statusChange("Gateway contacted successfully");
				listener.completed(true);
			} catch (MalformedURLException e) {
				listener.statusChange("Invalid URL");
				listener.completed(false);
			} catch (IOException e) {
				listener.statusChange(e.getMessage());
				listener.completed(false);
			} finally {
				listener.end();
			}
		}
		
	}
}
