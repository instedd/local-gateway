package org.instedd.mobilegw;

public interface ProgressListener
{
	void start(String title);
	void statusChange(String status);
	void completed(boolean successful);
	boolean isCanceled();
	void setCancelListener(Runnable cancelListener);
	void end();
}
