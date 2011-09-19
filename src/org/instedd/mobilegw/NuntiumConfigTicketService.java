package org.instedd.mobilegw;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.instedd.mobilegw.helpers.ConnectionHelper;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Implementation of {@link ConfigTicketService} that handles requests to
 * Nuntium
 */
public class NuntiumConfigTicketService implements ConfigTicketService {

	private URL baseAddress;

	public NuntiumConfigTicketService(URL baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean getConfig(TicketData td, ConfigTicketHandler handler) throws Exception {
		HttpURLConnection connection = ConnectionHelper.openConnection(baseAddress, "tickets/" + td.getCode() + ".json?secret_key=" + td.getSecretKey());
		
		JSONObject response = new JSONObject(new JSONTokener(connection.getInputStream()));
	    String status = response.getString("status");
	    
	    if (status.equals("complete")) {
	    	Map<String,String> data = new HashMap<String, String>();
	    	JSONObject jsonData = response.getJSONObject("data");
	    	Iterator<String> it = jsonData.keys();
	    	while (it.hasNext()) {
	    		String key = (String) it.next();
	    		data.put(key, jsonData.getString(key));
	    	}
	    	handler.handleConfiguration(data);
		    return true;
	    } else if (!status.equals("pending")) {
	    	throw new Exception("Unknown ticket status: " + status);
	    }
		
		return false;
	}

	@Override
	public TicketData requestTicket(String address) throws Exception {
		HttpURLConnection connection = ConnectionHelper.openConnection(baseAddress, "tickets.json");
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
	    
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
	    writer.write(URLEncoder.encode("address", "UTF-8") + "=" + URLEncoder.encode(address, "UTF-8"));
	    writer.flush();

	    JSONObject response = new JSONObject(new JSONTokener(connection.getInputStream()));
	    return new TicketData(response.getString("code"), response.getString("secret_key"));
	}

}
