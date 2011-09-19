package org.instedd.mobilegw;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class Daemon
{
	private Thread thread;
	private DaemonState state = DaemonState.STOPPED;
	private Object mutexObject = new Object();
	private long waitInterval = 5000;
	protected Logger logger;
	private List<DaemonListener> listeners = new LinkedList<DaemonListener>();
	private String failMessage;
	private String name;

	public Daemon() {
		this(Logger.getLogger(Daemon.class.getName()));
	}
	
	public Daemon(Logger logger) {
		this.logger = logger;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addListener(DaemonListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(DaemonListener listener) {
		listeners.remove(listener);
	}
	
	public void setWaitInterval(long waitInterval)
	{
		this.waitInterval = waitInterval;
	}
	
	public DaemonState getState()
	{
		return state;
	}
	
	private void setState(DaemonState newState)
	{
		DaemonState oldState = state;
		state = newState;
		for (DaemonListener listener : listeners) {
			listener.stateChanged(this, oldState, newState);
		}
	}
	
	public boolean isRunning() {
		return state == DaemonState.RUNNING || state == DaemonState.FAILED;
	}

	protected void setFailed(String message) {
		if (state == DaemonState.RUNNING) {
			failMessage = message;
			setState(DaemonState.FAILED);
		}
	}
	
	protected void clearFailed() {
		if (state == DaemonState.FAILED) {
			failMessage = null;
			setState(DaemonState.RUNNING);
		}
	}
	
	public String getFailMessage()
	{
		return failMessage;
	}
	
	public synchronized void start()
	{
		if (state == DaemonState.STOPPED) {
			thread = new Thread(new DaemonThread());
			thread.setDaemon(true);
			thread.start();
			setState(DaemonState.RUNNING);
		} else {
			throw new IllegalStateException("The daemon cannot be started because it is not stopped.");
		}
	}

	public synchronized void stop()
	{
		if (isRunning()) {
			setState(DaemonState.STOPPING);
			if (Thread.currentThread() != thread) {
				try {
					// Send a notify, so the thread stops immediately
					wakeUp();
					
					thread.join(waitInterval * 2);
					setState(DaemonState.STOPPED);
				} catch (InterruptedException e) {
					throw new Error(e);			
				}
			}
		}
	}
	
	protected void wakeUp()
	{
		synchronized (mutexObject) {
			mutexObject.notify();
		}
	}

	/**
	 * This method does the actual job
	 * 
	 * @throws InterruptedException
	 */
	protected abstract void process() throws InterruptedException;

	private class DaemonThread implements Runnable
	{
		@Override
		public void run()
		{
			while (state != DaemonState.STOPPING) {
				try {
					process();
				} catch (InterruptedException e) {
					break;
				} catch (Throwable e) {
					logger.severe(e.getMessage());
				}

				try {
					synchronized (mutexObject) {
						mutexObject.wait(waitInterval);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
}
