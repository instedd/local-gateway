package org.instedd.mobilegw.updater;

public abstract class Updater
{
	public static Updater createUpdater(Runnable runnable)
	{
		String osname = System.getProperty("os.name").toLowerCase();

		if (osname.startsWith("windows")) {
			return new WindowsUpdater(runnable);
		}

		return new NullUpdater(runnable);
	}

	public abstract void main();

	private static class NullUpdater extends Updater
	{
		private final Runnable runnable;

		public NullUpdater(Runnable runnable)
		{
			this.runnable = runnable;
		}

		@Override
		public void main()
		{
			runnable.run();
		}
	}
}
