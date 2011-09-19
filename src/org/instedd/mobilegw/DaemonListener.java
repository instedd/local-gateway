package org.instedd.mobilegw;

public interface DaemonListener
{
	void stateChanged(Daemon daemon, DaemonState oldState, DaemonState newState);
}
