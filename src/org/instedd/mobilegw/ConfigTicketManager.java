package org.instedd.mobilegw;

import java.net.URL;
import java.util.logging.Logger;

import org.instedd.mobilegw.ConfigTicketService.ConfigTicketHandler;
import org.instedd.mobilegw.ConfigTicketService.TicketData;

public class ConfigTicketManager {

	private ConfigTicketDaemon daemon;
	private ConfigTicketService service;
	
	public ConfigTicketManager(URL baseUrl, ConfigTicketHandler handler, Logger logger) {
		this.service = new NuntiumConfigTicketService(baseUrl);
		this.daemon = new ConfigTicketDaemon(handler, service, logger);
	}
	
	public String start(String address) throws Exception {
		TicketData data = service.requestTicket(address);
		this.daemon.setTicketData(data);
		this.daemon.start();
		return data.getCode();
	}
	
	public void stop() {
		this.daemon.stop();
	}
	
	/**
	 * Daemon for polling nuntium on the status of the configuration ticket 
	 */
	public static class ConfigTicketDaemon extends Daemon {

		private ConfigTicketService service;
		private ConfigTicketHandler handler;
		private TicketData data;
		
		public ConfigTicketDaemon(ConfigTicketHandler handler,
				ConfigTicketService service, Logger logger) {
			super(logger);
			this.service = service;
			this.handler = handler;
		}
		
		public void setTicketData(TicketData data) {
			this.data = data;
		}

		@Override
		protected void process() throws InterruptedException {
			try {
				if (service.getConfig(data, handler)) {
					this.stop();
				}
			} catch (Exception ex) {
				throw new Error(ex);
			}
		}
		
	}
	
}
