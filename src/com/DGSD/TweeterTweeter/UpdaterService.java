package com.DGSD.TweeterTweeter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	private static final String TAG = UpdaterService.class.getSimpleName();

	private static final int DELAY = 60000; // wait a minute

	private boolean runFlag = false;

	private Updater mUpdater;

	private TTApplication mApplication; 

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (TTApplication) getApplication(); 
		mUpdater = new Updater();

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		this.runFlag = true;
		mUpdater.start();
		mApplication.setServiceRunning(true); 

		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.runFlag = false;
		mUpdater.interrupt();
		mUpdater = null;
		mApplication.setServiceRunning(false); 

		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Thread that performs the actual update from the online service
	 */
	private class Updater extends Thread {
		public Updater() {
			super("UpdaterService-Updater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Updater running");
				try {

					// Get the timeline from the cloud & save to db
					int newUpdates = mApplication.fetchStatusUpdates(); 

					mApplication.fetchFavourites();

					if (newUpdates > 0) { 
						Log.d(TAG, "We have new stat-i");
					}

					Log.d(TAG, "Updater ran");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} // Updater
}