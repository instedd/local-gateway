package org.instedd.mobilegw;

import java.util.Map;

/**
 * Service for managing ticket configurations. Invoke {@link #requestTicket()} to obtain
 * the code first, and then {@link #getConfig(ConfigTicketHandler)} periodically to keep the
 * ticket alive and obtain the configuration itself.
 */
public interface ConfigTicketService {

	public class TicketData {
		private String code;
		private String secretKey;
		
		public TicketData(String code, String secretKey) {
			this.code = code;
			this.secretKey = secretKey;
		}
		
		public String getCode() {
			return code;
		}
		public String getSecretKey() {
			return secretKey;
		}
	}
	
	/**
	 * Request ticket from nuntium, this must be the initial request
	 * @return 4-digit code to be presenConfigTicketHandlerted to the user
	 * @throws Exception 
	 */
	TicketData requestTicket(String address) throws Exception;
	
	/**
	 * Gets the configuration provided by nuntium if it was completed
	 * @return whether the config ticket was fulfilled
	 * @throws Exception 
	 */
	boolean getConfig(TicketData data, ConfigTicketHandler handler) throws Exception;
	
	/**
	 * Interface for managing the configuration of a ticket
	 */
	public static interface ConfigTicketHandler {
		void handleConfiguration(Map<String,String> data);
	}
	
}
