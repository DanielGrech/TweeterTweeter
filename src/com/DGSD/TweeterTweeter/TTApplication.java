package com.DGSD.TweeterTweeter;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class TTApplication extends Application implements
OnSharedPreferenceChangeListener {

	private static final String TAG = TTApplication.class.getSimpleName();

	public static String CONSUMER_KEY;

	public static String CONSUMER_SECRET;

	private Twitter mTwitter;

	private boolean mServiceRunning;

	private TwitterSession mSession;

	private AccessToken mAccessToken;

	private SharedPreferences prefs;

	private StatusData statusData;

	@Override
	public void onCreate() {  
		super.onCreate();

		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.prefs.registerOnSharedPreferenceChangeListener(this);

		this.statusData = new StatusData(this);

		CONSUMER_KEY = getResources().getString(R.string.consumer_key);

		CONSUMER_SECRET = getResources().getString(R.string.consumer_secret);

		if(mTwitter == null) {
			mTwitter = new TwitterFactory().getInstance();
		}

		if(mSession == null) {
			mSession = new TwitterSession(this);

			mAccessToken = mSession.getAccessToken();

			configureToken();
		}

		Log.i(TAG, "onCreated");
	}

	@Override
	public void onTerminate() {  
		super.onTerminate();
		Log.i(TAG, "onTerminated");
	}

	@Override
	public synchronized void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences, String key) { // 
		mTwitter = null;
	}

	public boolean isHoneycombTablet() {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		&& (this.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				== Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private void configureToken() {
		if (mAccessToken != null) {
			try{
				mTwitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			}catch(IllegalStateException e){
				e.printStackTrace();
			}
			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public synchronized Twitter getTwitter() {
		return mTwitter;
	}

	public TwitterSession getTwitterSession() {
		return mSession;
	}

	public AccessToken getAccessToken() {
		return mAccessToken;
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public boolean isServiceRunning() {
		return mServiceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) { 
		mServiceRunning = serviceRunning;
	}

	public StatusData getStatusData() { 
		return statusData;
	}

	// Connects to the online service and puts the latest statuses into DB.
	// Returns the count of new statuses
	public synchronized int fetchStatusUpdates() {  
		Log.d(TAG, "Fetching status updates");
		Twitter twitter = this.getTwitter();
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return 0;
		}
		try {
			ResponseList<Status> timeline = mTwitter.getHomeTimeline();

			long latestStatusCreatedAtTime = this.getStatusData()
												.getLatestStatusCreatedAtTime();
			int count = 0;

			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createContentValues(Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited());

				Log.d(TAG, "Got update with id " + status.getId() + ". Saving");

				this.getStatusData().insertOrIgnore(StatusData.TIMELINE_TABLE, values);

				if (latestStatusCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			Log.d(TAG, count > 0 ? "Got " + count + " status updates"
					: "No new status updates");

			return count;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
			return 0;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return 0;
		}
	}

	// Connects to the online service and puts the latest favourites into DB.
	public synchronized void fetchFavourites() {  
		Log.d(TAG, "Fetching Favourites");
		Twitter twitter = this.getTwitter();
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return;
		}
		try {
			ResponseList<Status> timeline = mTwitter.getFavorites();

			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createContentValues(Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited());

				Log.d(TAG, "Got update with id " + status.getId() + ". Saving");

				this.getStatusData().insertOrIgnore(StatusData.FAVOURITES_TABLE, values);
			}

			Log.d(TAG, "Finished getting favourites");

			return;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch status updates", e);
			return;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return;
		}
	}
}
