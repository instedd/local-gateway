package org.instedd.mobilegw.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.Manifest;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

import org.instedd.mobilegw.Main;

public class WindowsUpdater extends Updater
{
	private final Manifest manifest;
	private final Runnable runnable;

	protected WindowsUpdater(Runnable runnable)
	{
		this.runnable = runnable;
		try {
			manifest = new Manifest(Main.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public void main()
	{
		boolean updating = false;
		
		try {
			updating = checkForUpdates();
		} catch (Throwable e) {
			showError("Error checking for updates: " + e.getMessage());
		}

		if (!updating)
			runnable.run();
	}
	
	private boolean checkForUpdates() throws IOException
	{
		String installerUrlString = manifest.getMainAttributes().getValue("Installer-Url");
		String currentVersionString = manifest.getMainAttributes().getValue("Version");
		if (installerUrlString == null || currentVersionString == null)
			return false;
		
		Version availableVersion = getAvailableVersion(installerUrlString);
		Version currentVersion = Version.parse(currentVersionString);
		
		// Check if the version available is newer
		if (currentVersion.compareTo(availableVersion) <= 0)
			return false;
		
		// Ask the user if an upgrade is desired
		String message = "There is a new version of this application.\n\n" +
			"     Current version: " + currentVersion.toString() + "\n" +
			"     Available version: " + availableVersion.toString() + "\n\n" +
			"Do you want to download and install the new version?";
		int result = JOptionPane.showConfirmDialog(null, message, "Upgrade available", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (result != JOptionPane.YES_OPTION)
			return false;
		
		downloadAndExecuteInstaller(installerUrlString);
		
		return true;
	}

	private void downloadAndExecuteInstaller(String installerUrlString) throws IOException
	{
		URL url = new URL(installerUrlString);
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		final ProgressMonitorInputStream inputStream = new ProgressMonitorInputStream(null, "Downloading", conn.getInputStream());
		inputStream.getProgressMonitor().setMaximum(conn.getContentLength());
		final File tempFile = File.createTempFile("Installer", ".msi");
		final FileOutputStream tempFileStream = new FileOutputStream(tempFile);
		new Thread(new Runnable() {
			@Override
			public void run()
			{
				try {
					byte[] buffer = new byte[1024];
					int count;
					try {
						while ((count = inputStream.read(buffer)) >= 0) {
							tempFileStream.write(buffer, 0, count);
						}
					} catch (InterruptedIOException e) {
						// The user canceled the file download
						runnable.run();
						tempFileStream.close();
						tempFile.delete();						
						return;
					} finally {
						conn.disconnect();
					}

					tempFileStream.close();
					
					Runtime.getRuntime().exec(new String[] { "msiexec", "/i", tempFile.getAbsolutePath() });
					System.exit(0);
					
				} catch (Exception e) {
					showError("Error downloading upgrade: " + e.getMessage());
				}
			}
		}).start();
	}

	private void showError(String message)
	{
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	private Version getAvailableVersion(String installerUrlString)
	{
		try {
			URL url = new URL(installerUrlString + ".version");
			System.out.println("Checking updates at " + url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			String version = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
			conn.disconnect();
			
			return Version.parse(version);
		} catch (Throwable e) {
			throw new Error(e);
		}
	}
}
