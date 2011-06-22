package com.DGSD.TweeterTweeter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	private static final String TAG = UpdaterService.class.getSimpleName();

	private static final int TIMELINE_DELAY = 60000; // wait a minute
	
	private static final int FAVOURITES_DELAY = 60000; // wait a minute
	
	private static final int FOLLOWERS_DELAY = 60000; // wait a minute

	private boolean runFlag = false;

	private TimelineUpdater mTimelineUpdater;
	
	private FavouritesUpdater mFavouritesUpdater;
	
	private FollowersUpdater mFollowersUpdater;
	
	private TTApplication mApplication; 

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (TTApplication) getApplication(); 
		mTimelineUpdater = new TimelineUpdater();
		mFollowersUpdater = new FollowersUpdater();
		mFavouritesUpdater = new FavouritesUpdater();

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		this.runFlag = true;
		
		//Start all updating threads
		mTimelineUpdater.start();
		
		mFavouritesUpdater.start();
		
		mFollowersUpdater.start();
		
		mApplication.setServiceRunning(true); 

		Log.d(TAG, "onStarted");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.runFlag = false;
		mTimelineUpdater.interrupt();
		mTimelineUpdater = null;
		
		mFavouritesUpdater.interrupt();
		mFavouritesUpdater = null;
		
		mFollowersUpdater.interrupt();
		mFollowersUpdater = null;
		
		mApplication.setServiceRunning(false); 

		Log.d(TAG, "onDestroyed");
	}

	/**
	 * Threads that performs the actual update from the online service
	 */
	private class TimelineUpdater extends Thread {
		public TimelineUpdater() {
			super("UpdaterService-TimelineUpdater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Timeline Updater running");
				try {

					// Get the timeline from the cloud & save to db
					int newUpdates = mApplication.fetchStatusUpdates(); 

					if (newUpdates > 0) { 
						Log.d(TAG, "We have new stat-i");
					}

					Log.d(TAG, "Timeline Updater ran");
					Thread.sleep(TIMELINE_DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} //Timeline Updater
	
	private class FavouritesUpdater extends Thread {
		public FavouritesUpdater() {
			super("UpdaterService-FavouritesUpdater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Favourites Updater running");
				try {
					// Get favourites from the cloud & save to db
					mApplication.fetchFavourites();

					Log.d(TAG, "Favourites Updater ran");
					Thread.sleep(FAVOURITES_DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} //Favourites Updater
	
	private class FollowersUpdater extends Thread {
		public FollowersUpdater() {
			super("UpdaterService-FollowersUpdater");
		}

		@Override
		public void run() {
			UpdaterService updaterService = UpdaterService.this;

			while (updaterService.runFlag) {
				Log.d(TAG, "Followers Updater running");
				try {
					// Get followers from the cloud & save to db
					mApplication.fetchFollowers();

					// Get following from the cloud & save to db
					mApplication.fetchFollowing();
					
					// Get information for the current user..
					mApplication.fetchProfileInfo();
					
					Log.d(TAG, "Followers Updater ran");
					Thread.sleep(FOLLOWERS_DELAY);
				} catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	} //Favourites Updater
}