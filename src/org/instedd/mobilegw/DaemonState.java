/**
 * 
 */
package org.instedd.mobilegw;

public enum DaemonState
{
	/**
	 * The daemon is stopped and the thread is not running
	 */
	STOPPED,
	
	/**
	 * The stop() method was called and the daemon is waiting for cleanup
	 */
	STOPPING,
	
	/**
	 * The daemon thread is running
	 */
	RUNNING,
	
	/**
	 * The daemon is marked as temporarily failed
	 */
	FAILED
}