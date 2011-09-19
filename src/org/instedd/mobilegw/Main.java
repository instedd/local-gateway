package org.instedd.mobilegw;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.instedd.mobilegw.ui.MainWindow;
import org.instedd.mobilegw.updater.Updater;

public class Main
{
	private static File workingDirectory;
	
	public static void main(String[] args) throws Exception
	{
		Updater updater = Updater.createUpdater(new Runnable() {
			@Override
			public void run()
			{
				try {
					setWorkingDirectory();
					configureLogging();
					logStartup();
					
					MainWindow mainWindow = new MainWindow();
					mainWindow.setVisible(true);
				} catch (Exception ex) {
					System.out.println("Unhandled exception: " + ex.toString());
				}
			}
		});
		updater.main();
	}

	private static void logStartup()
	{
		Logger logger = Logger.getAnonymousLogger();
		logger.info("Starting Mobile Gateway");
		logger.info("Environment variables:");
		for (Entry<String, String> env : System.getenv().entrySet()) {
			logger.info(env.getKey() + " = " + env.getValue());
		}
		
		logger.info("System properties:");
		for (Entry<Object, Object> prop : System.getProperties().entrySet()) {
			logger.info(prop.getKey().toString() + " = " + prop.getValue().toString());
		}
	}

	private static void configureLogging() throws IOException
	{
		Logger rootLogger = Logger.getLogger("");
		
		// Remove default handlers
		Handler[] handlers = rootLogger.getHandlers();
		for (Handler handler : handlers) {
			rootLogger.removeHandler(handler);
		}
		
		// Add the file handler
		String logFilePattern = new File(getWorkingDirectory(), "sms-%g.log").getAbsolutePath();
		FileHandler fileHandler = new FileHandler(logFilePattern, 
				5000000,   // Log file size limit
				3,         // Number of rotating log files
				true       // Append
				);
		fileHandler.setFormatter(new SimpleFormatter());
		rootLogger.addHandler(fileHandler);
		rootLogger.setLevel(Level.INFO);
	}

	public static File getWorkingDirectory()
	{
		return workingDirectory;
	}
	
	private static void setWorkingDirectory()
	{
		String osname = System.getProperty("os.name").toLowerCase();

		if (osname.startsWith("windows")) {
			// Windows Vista added a different environment variable for local settings
			String appData = System.getenv("LOCALAPPDATA");
			
			// Use APPDATA for Windows XP and previous versions
			if (appData == null) {
				appData = System.getenv("APPDATA");
			}
			workingDirectory = new File(appData, "Instedd\\MobileGateway");
		} else {
			String userHome = System.getProperty("user.home");
			if (osname.startsWith("mac os"))
				workingDirectory = new File(userHome, "Library/Application Support/Instedd/MobileGateway");
			else
				workingDirectory = new File(userHome, ".MobileGateway");
		}
		
		workingDirectory.mkdirs();
	}
}
