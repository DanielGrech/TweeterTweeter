package com.DGSD.TweeterTweeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
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

	private HashMap<String,Twitter> mTwitterList;
	
	private HashSet<String> mAccountList;
	
	private HashMap<String, AccessToken > mAccessTokenList;

	private boolean mServiceRunning;

	private TwitterSession mSession;

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

		if(mAccessTokenList == null) {
			mAccessTokenList = new HashMap<String, AccessToken> ();
		}

		if(mTwitterList == null) {
			mTwitterList = new HashMap<String, Twitter>();
		}
		
		if(mSession == null) {
			mSession = new TwitterSession(this);

			mAccountList = mSession.getAccountList();
			
			if(mAccountList != null) {
    			for(String account : mAccountList) {
    				AccessToken at = mSession.getAccessToken(account);
    				Twitter t = new TwitterFactory().getInstance();
    				
    				mAccessTokenList.put(account,at);
    				mTwitterList.put(account,t);
    				
    				configureToken(at, t);
    			}
			}
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
			SharedPreferences sharedPreferences, String key) { 
		Log.i(TAG, "Setting mTwitterList to null");
		mTwitterList = null;
	}

	public boolean isHoneycombTablet() {
		// Can use static final constants like HONEYCOMB, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		&& (this.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK)
				== Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private void configureToken(AccessToken at, Twitter t) {
		if (at != null) {
			try{
				t.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
			}catch(IllegalStateException e){
				e.printStackTrace();
			}
			t.setOAuthAccessToken(at);
		}
	}

	public synchronized Twitter getTwitter(String accountId) {
		return mTwitterList.get(accountId);
	}

	public TwitterSession getTwitterSession() {
		return mSession;
	}

	public AccessToken getAccessToken(String accountId) {
		return mAccessTokenList.get(accountId);
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
	public synchronized int fetchStatusUpdates(String accountId) {  
		Log.d(TAG, "Fetching status updates");
		Twitter twitter = mTwitterList.get(accountId);
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return 0;
		}
		try {
			ResponseList<Status> timeline = twitter.getHomeTimeline();

			long latestStatusCreatedAtTime = this.getStatusData()
			.getLatestStatusCreatedAtTime(accountId);
			int count = 0;

			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(accountId, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

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
	public synchronized void fetchFavourites(String accountId) {  
		Log.d(TAG, "Fetching Favourites");
		Twitter twitter = mTwitterList.get(accountId);
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return;
		}
		try {
			ResponseList<Status> timeline = twitter.getFavorites();

			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(accountId, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				Log.d(TAG, "Got update with id " + status.getId() + ". Saving");

				this.getStatusData().insertOrIgnore(StatusData.FAVOURITES_TABLE, values);
			}

			Log.d(TAG, "Finished getting favourites");

			return;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch favourites", e);
			return;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return;
		}
	}

	// Connects to the online service and puts the latest followers into DB.
	public synchronized void fetchFollowers(String accountId) {  
		Log.d(TAG, "Fetching Followers");
		Twitter twitter = mTwitterList.get(accountId);
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return;
		}
		try {
			ArrayList<Long> mIds = new ArrayList<Long>();
			long cursor = -1;

			//Get the ids of all followers..
			IDs ids;
			do{
				ids =  twitter.getFollowersIDs(cursor);

				long[] idArray = ids.getIDs();

				for(int i = 0, size=idArray.length; i<size ;i++)
					mIds.add(idArray[i]);
			}while( (cursor = ids.getNextCursor()) != 0);

			long tempIds[] = new long[mIds.size()];
			for(int i = 0, size = mIds.size(); i < size; i++)
				tempIds[i] = mIds.get(i);

			ResponseList<User> users = twitter.lookupUsers(tempIds);

			ContentValues values;
			for (User u : users) {
				values = StatusData.createUserContentValues(accountId, u);

				Log.d(TAG, "Got user: " + u.getScreenName() + ". Saving");

				this.getStatusData().insertOrIgnore(StatusData.FOLLOWERS_TABLE, values);
			}

			Log.d(TAG, "Finished getting followers");

			return;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch followers", e);
			return;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return;
		}
	}

	// Connects to the online service and puts the latest following into DB.
	public synchronized void fetchFollowing(String accountId) {  
		Log.d(TAG, "Fetching Friends");
		Twitter twitter = mTwitterList.get(accountId);
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return;
		}
		try {
			ArrayList<Long> mIds = new ArrayList<Long>();
			long cursor = -1;

			//Get the ids of all friends..
			IDs ids;
			do{
				ids =  twitter.getFriendsIDs(cursor);

				long[] idArray = ids.getIDs();

				for(int i = 0, size=idArray.length; i<size ;i++)
					mIds.add(idArray[i]);
			}while( (cursor = ids.getNextCursor()) != 0);

			long tempIds[] = new long[mIds.size()];
			for(int i = 0, size = mIds.size(); i < size; i++)
				tempIds[i] = mIds.get(i);

			ResponseList<User> users = twitter.lookupUsers(tempIds);

			ContentValues values;
			for (User u : users) {
				values = StatusData.createUserContentValues(accountId, u);

				Log.d(TAG, "Got user: " + u.getScreenName() + ". Saving");

				this.getStatusData().insertOrIgnore(StatusData.FOLLOWING_TABLE, values);
			}

			Log.d(TAG, "Finished getting friends");

			return;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch friends", e);
			return;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return;
		}
	}

	// Connects to the online service and puts the latest following into DB.
	public synchronized void fetchProfileInfo(String accountId) {  
		Log.d(TAG, "Fetching profile");
		Twitter twitter = mTwitterList.get(accountId);
		if (twitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return;
		}
		try {

			User u = twitter.showUser(twitter.getId());

			if(u != null){
				ContentValues values = StatusData.createUserContentValues(accountId, u);

				Log.d(TAG, "Got profile");

				this.getStatusData().insertOrIgnore(StatusData.PROFILE_TABLE, values);
			}

			Log.d(TAG, "Finished getting profile");

			return;

		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch profile", e);
			return;
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return;
		}
	}
}
