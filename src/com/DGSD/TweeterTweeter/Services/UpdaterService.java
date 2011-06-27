package com.DGSD.TweeterTweeter.Services;

import java.util.HashSet;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.DGSD.TweeterTweeter.Utils.Log;

import com.DGSD.TweeterTweeter.TTApplication;

public class UpdaterService extends Service {
	private static final String TAG = UpdaterService.class.getSimpleName();

	public static final String SEND_DATA= 
		"com.DGSD.TweeterTweeter.SEND_DATA";

	private static final int TIMELINE_DELAY = 60000; // wait a minute

	private static final int FAVOURITES_DELAY = 60000; // wait a minute

	private static final int FOLLOWERS_DELAY = 60000; // wait a minute

	private static final int RETWEET_DELAY = 60000; // wait a minute

	private boolean runFlag = false;

	private TimelineUpdater mTimelineUpdater;

	private FavouritesUpdater mFavouritesUpdater;

	private FollowersUpdater mFollowersUpdater;

	private RetweetUpdater mRetweetUpdater;

	private TTApplication mApplication;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (TTApplication) getApplication(); 

		Log.d(TAG, "onCreated");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		this.runFlag = true;

		mTimelineUpdater = new TimelineUpdater();
		mFollowersUpdater = new FollowersUpdater();
		mFavouritesUpdater = new FavouritesUpdater();
		mRetweetUpdater = new RetweetUpdater();
		
		//Start all updating threads
		mTimelineUpdater.start();

		mFavouritesUpdater.start();

		mFollowersUpdater.start();

		mRetweetUpdater.start();

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

		mRetweetUpdater.interrupt();
		mRetweetUpdater = null;

		mApplication.setServiceRunning(false); 

		Log.d(TAG, "onDestroyed");
	}

	private abstract class Updater extends Thread {
		
		protected UpdaterService updaterService = UpdaterService.this;
		
		protected abstract void runUpdate(String account);
		
		@Override
		public void run() {
			while (updaterService.runFlag) {
				HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();
			
				try {
					if(accounts != null) {
							for(String a : accounts) {
								runUpdate(a);
							}
					}
					
					Thread.sleep(TIMELINE_DELAY);
				}catch (InterruptedException e) {
					updaterService.runFlag = false;
				}
			}
		}
	}
	
	/**
	 * Threads that performs the actual update from the online service
	 */
	private class TimelineUpdater extends Updater {
		public void runUpdate(String account) {
			// Get the timeline from the cloud & save to db
			int newUpdates = mApplication.fetchStatusUpdates(account); 

			//Get any new mentions..
			mApplication.fetchMentions(account);

			if (newUpdates > 0) { 
				Log.d(TAG, "We have new stat-i");
				
				Intent intent = new Intent(SEND_DATA); 
				//intent.putExtra(NEW_STATUS_EXTRA_COUNT, newUpdates); 
				updaterService.sendBroadcast(intent); 
			}
		}
	} 

	private class FavouritesUpdater extends Updater {
		public void runUpdate(String account) {
			mApplication.fetchFavourites(account);
		}
	} 
	
	private class RetweetUpdater extends Updater {
		public void runUpdate(String account) {
			mApplication.fetchRetweetsByMe(account);
			mApplication.fetchRetweetsOfMe(account);
		}
	} 
	
	private class FollowersUpdater extends Updater {
		public void runUpdate(String account) {
			mApplication.fetchFollowers(account);
			mApplication.fetchFollowing(account);
			mApplication.fetchProfileInfo(account);
		}
	}
}