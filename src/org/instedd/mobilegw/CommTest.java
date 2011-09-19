package org.instedd.mobilegw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.smslib.helper.CommPortIdentifier;
import org.smslib.helper.SerialPort;

public class CommTest
{
	static int bauds[] = { 9600, 14400, 19200, 28800, 33600, 38400, 56000, 57600, 115200 };
	private String foundPort;
	private int foundBauds;
	private String manufacturer;
	private String model;

	public void test(ProgressListener listener, Runnable completedAction)
	{
		Thread thread = new Thread(new CommTestThread(listener, completedAction));
		thread.start();
	}
	
	private class CommTestThread implements Runnable
	{
		private final ProgressListener listener;
		private final Runnable completedAction;

		public CommTestThread(ProgressListener listener, Runnable completedAction)
		{
			this.listener = listener;
			this.completedAction = completedAction;
		}

		public void run()
		{
			listener.start("Searching devices");
			Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
			boolean found = false;
			while (portList.hasMoreElements()) {
				if (listener.isCanceled())
					break;

				CommPortIdentifier portId = portList.nextElement();
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

					for (int i = 0; i < bauds.length; i++) {
						if (listener.isCanceled())
							break;

						listener.statusChange("Trying " + portId.getName() + " at " + bauds[i]);
						if (searchWithPortAndBauds(portId, bauds[i]))
							found = true;
					}

					if (found)
						break;
				}
			}

			if (found) {
				listener.statusChange("Found " + manufacturer + " " + model + " at " + foundPort);
				listener.completed(true);
				completedAction.run();
			} else {
				listener.statusChange("Could not find any device");
				listener.completed(false);
			}
			listener.end();
		}

		private boolean searchWithPortAndBauds(CommPortIdentifier portId, int baudRate)
		{
			SerialPort serialPort = null;

			try {
				InputStream inStream;
				OutputStream outStream;
				String response;
				serialPort = portId.open("SMSLibCommTester", 1971);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
				serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				inStream = serialPort.getInputStream();
				outStream = serialPort.getOutputStream();
				serialPort.enableReceiveTimeout(300);

				// Flush input stream
				readResponse(inStream);

				writeCommand(outStream, "AT");
				Thread.sleep(300);
				response = readResponse(inStream);
				if (response.indexOf("OK") >= 0) {
					// Echo off
					writeCommand(outStream, "ATE0");
					readResponse(inStream);

					try {
						writeCommand(outStream, "AT+CGMI");
						
						String answer = readResponse(inStream);
						if (isErrorResponse(answer))
							return false;
						
						manufacturer = cleanResponse(answer);
						writeCommand(outStream, "AT+CGMM");
						
						answer = readResponse(inStream);
						if (isErrorResponse(answer))
							return false;
						
						model = cleanResponse(answer);
						foundPort = portId.getName();
						foundBauds = baudRate;
						return true;
					} catch (Exception e) {
						// Ignore the error
					}
				}
			} catch (Exception e) {
				// Ignore the error
			} finally {
				if (serialPort != null) {
					serialPort.close();
				}
			}

			return false;
		}

		private String readResponse(InputStream inStream) throws IOException
		{
			int c;
			String response = "";
			c = inStream.read();
			while (c != -1) {
				response += (char) c;
				c = inStream.read();
			}
			return response;
		}
		
		private boolean isErrorResponse(String response) {
			return "ERROR".equals(response);
		}

		private String cleanResponse(String response)
		{
			return response.replaceAll("\\s+OK\\s+", "").replaceAll("\n", "").replaceAll("\r", "");
		}

		private void writeCommand(OutputStream outStream, String command) throws IOException
		{
			for (char c : command.toCharArray()) {
				outStream.write(c);
			}
			outStream.write('\r');
		}
	}

	public String getFoundPort()
	{
		return foundPort;
	}

	public int getFoundBauds()
	{
		return foundBauds;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public String getModel()
	{
		return model;
	}
}
