package org.smslib.modem.athandler;

import java.io.IOException;

import org.smslib.GatewayException;
import org.smslib.TimeoutException;
import org.smslib.modem.ModemGateway;

public class ATHandler_SonyEricsson_F305 extends ATHandler_SonyEricsson
{

	public ATHandler_SonyEricsson_F305(ModemGateway myGateway)
	{
		super(myGateway);
	}

	@Override
	public void init() throws TimeoutException, GatewayException, IOException, InterruptedException
	{
		super.init();
		Thread.sleep(7000);
	}
}
