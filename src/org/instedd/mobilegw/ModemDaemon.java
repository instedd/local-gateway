package org.instedd.mobilegw;

import java.util.UUID;
import java.util.logging.Logger;

import org.ajwcc.pduUtils.gsm3040.PduUtils;
import org.instedd.mobilegw.messaging.Message;
import org.instedd.mobilegw.messaging.MessageQueue;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.AGateway.GatewayStatuses;
import org.smslib.Message.MessageEncodings;
import org.smslib.Message.MessageTypes;
import org.smslib.modem.SerialModemGateway;

public class ModemDaemon extends Daemon implements MessageChannel
{
	private final Service smslibService;
	private final MessageQueue moQueue;
	private final Logger logger;
	private final boolean appendPlus;

	public ModemDaemon(MessageQueue moQueue, Logger logger, boolean appendPlus)
	{
		this.moQueue = moQueue;
		this.logger = logger;
		this.smslibService = new Service();
		this.appendPlus = appendPlus;
		setName("Modem");

		smslibService.setInboundMessageNotification(new InboundMessageHandler());
	}

	public void addGateway(AGateway gateway)
	{
		try {
			smslibService.addGateway(gateway);
		} catch (GatewayException e) {
			throw new Error(e);
		}
	}

	public void start()
	{
		try {
			smslibService.startService();
		} catch (Exception e) {
			logger.severe("Failed to start modem: " + e.getMessage());
			throw new Error(e);
		}
		
		super.start();

		logger.info("Modem started successfully");
	}

	public void stop()
	{
		super.stop();
		
		try {
			logger.info("Stopping modem");
			smslibService.stopService();
			logger.info("Modem stopped successfully");
		} catch (Exception e) {
			logger.severe("Failed to stop modem: " + e.getMessage());
			throw new Error(e);
		}
	}

	private class InboundMessageHandler implements IInboundMessageNotification
	{
		@Override
		public void process(String gatewayId, MessageTypes msgType, InboundMessage msg)
		{
			if (msgType == MessageTypes.INBOUND) {
				Message message = new Message();
				message.id = UUID.randomUUID().toString();
				message.from = "sms://" + msg.getOriginator().replaceAll("[^\\d]", "");
				message.to = "sms://0"; // TODO: use the local number
				message.text = msg.getText();
				message.when = msg.getDate();
				try {
					moQueue.enqueue(new Message[] { message });
					smslibService.deleteMessage(msg);
					logger.info("Message received from " + msg.getOriginator());
				} catch (Exception e) {
					logger.severe("Error receiving message: " + e.getMessage());
				}
			}
		}
	}

	public boolean sendMessage(Message message) throws Exception
	{
		OutboundMessage outboundMessage = new OutboundMessage(message.to.replace("sms://", appendPlus ? "+" : ""), message.text);
		if (needsUcs2Encoding(message.text))
			outboundMessage.setEncoding(MessageEncodings.ENCUCS2);
		logger.info("Sending message with mobile phone: " + message.toString());
		if (smslibService.sendMessage(outboundMessage) == false)
		{
			throw new Exception("Unknown error");
		}
		return true;
	}
	
	private boolean needsUcs2Encoding(String text)
	{
		byte[] septets = PduUtils.stringToUnencodedSeptets(text);
		String string = PduUtils.unencodedSeptetsToString(septets);
		return !text.equals(string);
	}

	@Override
	protected void process() throws InterruptedException
	{
		for (AGateway gateway : smslibService.getGateways()) {
			if (gateway.getStatus() != GatewayStatuses.STARTED) {
				setFailed("Modem has stopped");
				continue;
			}
			
			if (gateway instanceof SerialModemGateway) {
				SerialModemGateway modemGateway = (SerialModemGateway) gateway;
				try {
					int signalLevel = modemGateway.getSignalLevel();
					if (signalLevel > 0)
						clearFailed();
					else
						setFailed("No signal");
				} catch (Exception e) {
					setFailed("Modem error: " + e.getMessage());
				}
			}
		}
	}
}
