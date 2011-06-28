package com.DGSD.TweeterTweeter.Services;

import java.util.HashSet;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.DGSD.TweeterTweeter.Utils.Log;

import com.DGSD.TweeterTweeter.TTApplication;

public class UpdaterService extends Service {
	private static final String TAG = UpdaterService.class.getSimpleName();

	public static final String DATA_TYPE = "data_type";
	
	public static final String SEND_DATA = 
		"com.DGSD.TweeterTweeter.SEND_DATA";
	
	public static final String NO_DATA = 
		"com.DGSD.TweeterTweeter.NO_DATA";
	
	private static final int TIMELINE_DELAY = 60000; // wait a minute

	/*private static final int FAVOURITES_DELAY = 60000; // wait a minute

	private static final int FOLLOWERS_DELAY = 60000; // wait a minute

	private static final int RETWEET_DELAY = 60000; // wait a minute */

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

		if(mTimelineUpdater == null) {
    		mTimelineUpdater = new TimelineUpdater();
    		mFollowersUpdater = new FollowersUpdater();
    		mFavouritesUpdater = new FavouritesUpdater();
    		mRetweetUpdater = new RetweetUpdater();
    		
    		//Start all updating threads
    		mTimelineUpdater.start();
    		mFavouritesUpdater.start();
    		mFollowersUpdater.start();
    		mRetweetUpdater.start();
		}
		else {
			//We already have a thread object.
			//The thread is either working already or sleeping
			//If sleeping, wake it up, else let it keep working :D
			if(mTimelineUpdater.isSleeping()) {
				mTimelineUpdater.interrupt();
			}
			if(mFavouritesUpdater.isSleeping()) {
				mTimelineUpdater.interrupt();
			}
			if(mFollowersUpdater.isSleeping()) {
				mTimelineUpdater.interrupt();
			}
			if(mRetweetUpdater.isSleeping()) {
				mTimelineUpdater.interrupt();
			}
		}

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

		//Incase any UI elements are waiting for a refresh..
		Intent intent = new Intent(NO_DATA); 
		sendBroadcast(intent); 
		
		Log.d(TAG, "onDestroyed");
	}

	private abstract class Updater extends Thread {

		protected UpdaterService updaterService = UpdaterService.this;

		/*
		 * If a thread is already running and is asleep, interupt it to awaken it
		 */
		protected boolean is_asleep;
		
		protected abstract void runUpdate(String account);

		public synchronized boolean isSleeping() {
			return is_asleep;
		}
		
		protected void sendData(int type) {
			Intent intent = new Intent(SEND_DATA);
			intent.putExtra(DATA_TYPE, type);
			updaterService.sendBroadcast(intent);
		}
		
		protected void sendNoData(int type) {
			Intent intent = new Intent(NO_DATA);
			intent.putExtra(DATA_TYPE, type);
			updaterService.sendBroadcast(intent);
		}
		
		@Override
		public void run() {
			while (updaterService.runFlag) {
				is_asleep = false;
				HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();

				try {
					if(accounts != null) {
						for(String a : accounts) {
							runUpdate(a);
						}
					}

					is_asleep = true;
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
			if(mApplication.fetchStatusUpdates(account) > 0) {
				sendData(DATATYPES.HOME_TIMELINE);
			} else {
				sendNoData(DATATYPES.HOME_TIMELINE);
			}

			if(mApplication.fetchMentions(account) > 0 ) {
				sendData(DATATYPES.MENTIONS);
			} else {
				sendNoData(DATATYPES.MENTIONS);
			}
		}
	} 

	private class FavouritesUpdater extends Updater {
		public void runUpdate(String account) {
			if(mApplication.fetchFavourites(account) > 0) {
				sendData(DATATYPES.FAVOURITES);
			} else {
				sendNoData(DATATYPES.FAVOURITES);
			}
		}
	} 

	private class RetweetUpdater extends Updater {
		public void runUpdate(String account) {
			if(mApplication.fetchRetweetsByMe(account) > 0){
				sendData(DATATYPES.RETWEETS_BY);
			} else {
				sendNoData(DATATYPES.RETWEETS_BY);
			}
			
			if(mApplication.fetchRetweetsOfMe(account) > 0){
				sendData(DATATYPES.RETWEETS_OF);
			} else {
				sendNoData(DATATYPES.RETWEETS_OF);
			}
		}
	} 

	private class FollowersUpdater extends Updater {
		public void runUpdate(String account) {
			if(mApplication.fetchFollowers(account) > 0) {
				sendData(DATATYPES.FOLLOWERS);
			} else {
				sendNoData(DATATYPES.FOLLOWERS);
			}
			
			if(mApplication.fetchFollowing(account) > 0) {
				sendData(DATATYPES.FOLLOWING);
			} else {
				sendNoData(DATATYPES.FOLLOWING);
			}
			
			if(mApplication.fetchProfileInfo(account) > 0) {
				sendData(DATATYPES.PROFILE_INFO);
			} else {
				sendNoData(DATATYPES.PROFILE_INFO);
			}
		}
	}
	
	public static class DATATYPES {
		public static int HOME_TIMELINE = 0;
		public static int FAVOURITES = 1;
		public static int MENTIONS = 2;
		public static int RETWEETS_BY = 3;
		public static int RETWEETS_OF = 4;
		public static int TIMELINE = 5;
		public static int FOLLOWERS = 6;
		public static int FOLLOWING = 7;
		public static int PROFILE_INFO = 8;
	}
}