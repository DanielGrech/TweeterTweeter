package com.DGSD.TweeterTweeter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.AlarmManager;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.DGSD.TweeterTweeter.DataFetchers.FetchFavourites;
import com.DGSD.TweeterTweeter.DataFetchers.FetchFollowers;
import com.DGSD.TweeterTweeter.DataFetchers.FetchFollowing;
import com.DGSD.TweeterTweeter.DataFetchers.FetchMentions;
import com.DGSD.TweeterTweeter.DataFetchers.FetchProfileInfo;
import com.DGSD.TweeterTweeter.DataFetchers.FetchRetweetsBy;
import com.DGSD.TweeterTweeter.DataFetchers.FetchRetweetsOf;
import com.DGSD.TweeterTweeter.DataFetchers.FetchStatusUpdates;
import com.DGSD.TweeterTweeter.DataFetchers.FetchTimeline;
import com.DGSD.TweeterTweeter.Utils.Log;

public class TTApplication extends Application implements
OnSharedPreferenceChangeListener {

	private static final String TAG = TTApplication.class.getSimpleName();

	public static final int INTERVAL_NEVER = -1;

	public static String CONSUMER_KEY;

	public static String CONSUMER_SECRET;
	
	public static String MAPS_KEY;

	private HashMap<String,Twitter> mTwitterList;

	private HashSet<String> mAccountList;

	private HashMap<String, AccessToken > mAccessTokenList;

	private boolean mServiceRunning;

	private TwitterSession mSession;

	private SharedPreferences mPrefs;

	private StatusData mStatusData;

	private long mInterval = AlarmManager.INTERVAL_HOUR;

	/* Interface to updaters which fetch data from network */
	private FetchStatusUpdates mFetchStatusUpdates;

	private FetchTimeline mFetchTimeline;

	private FetchFavourites    mFetchFavourites;

	private FetchMentions      mFetchMentions;

	private FetchRetweetsOf  mFetchRetweetsOf;

	private FetchRetweetsBy  mFetchRetweetsBy;

	private FetchFollowers     mFetchFollowers;

	private FetchFollowing     mFetchFollowing;

	private FetchProfileInfo   mFetchProfileInfo;

	private String mSelectedAccount;

	@Override
	public void onCreate() {  
		super.onCreate();

		/*
		 * SET STRICT MODE!
		 */
		/*if(Log.LOG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			.detectDiskReads()
			.detectDiskWrites()
			.detectNetwork()   // or .detectAll() for all detectable problems
			.penaltyLog()
			.build());

			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectLeakedSqlLiteObjects()
			.penaltyLog()
			.penaltyDeath()
			.build());
		}*/

		mSelectedAccount = "account1";

		CONSUMER_KEY = getResources().getString(R.string.consumer_key);
		CONSUMER_SECRET = getResources().getString(R.string.consumer_secret);
		//TODO: Need to change this to production before release
		MAPS_KEY = getResources().getString(R.string.maps_key_debug);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		mPrefs.registerOnSharedPreferenceChangeListener(this);

		mStatusData = new StatusData(this);

		if(mAccessTokenList == null) {
			mAccessTokenList = new HashMap<String, AccessToken> ();
		}

		if(mTwitterList == null) {
			mTwitterList = new HashMap<String, Twitter>();
		}

		if(mSession == null) {
			mSession = new TwitterSession(this);
		}
		
		setupAccountList();
		
		mFetchStatusUpdates = new FetchStatusUpdates(this);

		mFetchTimeline = new FetchTimeline(this);

		mFetchFavourites = new FetchFavourites(this);

		mFetchMentions = new FetchMentions(this);

		mFetchRetweetsOf = new FetchRetweetsOf(this);

		mFetchRetweetsBy = new FetchRetweetsBy(this);

		mFetchFollowers = new FetchFollowers(this);

		mFetchFollowing = new FetchFollowing(this);

		mFetchProfileInfo = new FetchProfileInfo(this);

		Log.i(TAG, "onCreated");
	}

	public void setupAccountList() {
		mAccountList = mSession.getAccountList();

		if(mAccountList != null) {
			for(String account : mAccountList) {
				AccessToken at = mSession.getAccessToken(account);

				Twitter t = 
						new TwitterFactory(new ConfigurationBuilder().setIncludeEntitiesEnabled(true)
								.build() ).getInstance();

				mAccessTokenList.put(account,at);
				mTwitterList.put(account,t);

				configureToken(at, t);
			}
		}
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

	public synchronized void setSelectedAccount(String account) {
		mSelectedAccount = account;
	}

	public String getSelectedAccount(){
		return mSelectedAccount;
	}

	public synchronized Twitter getTwitter(String accountId) {
		return mTwitterList.get(accountId);
	}

	public synchronized HashSet<String> getAccountList() {
		HashSet<String> retval = new HashSet<String>();

		for(String a: mAccountList) {
			try {
				retval.add(mSession.getUsername(a));
			} catch(NullPointerException e) {
				Log.e(TAG, "Null pointer getting screen name");
			}
		}

		return retval;
	}

	public synchronized LinkedList<String[]> getAccountListWithImage() {
		LinkedList<String[]> retval = new LinkedList<String[]>();

		for(String a: mAccountList) {
			try {
				retval.add(new String[]{mSession.getUsername(a), mSession.getUserImage(a)});
			} catch(NullPointerException e) {
				Log.e(TAG, "Null pointer getting screen name");
			}
		}

		return retval;
	}
	
	public TwitterSession getTwitterSession() {
		return mSession;
	}

	public AccessToken getAccessToken(String accountId) {
		return mAccessTokenList.get(accountId);
	}

	public SharedPreferences getPrefs() {
		return mPrefs;
	}

	public boolean isServiceRunning() {
		return mServiceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) { 
		mServiceRunning = serviceRunning;
	}

	public StatusData getStatusData() { 
		return mStatusData;
	}

	public long getInterval() {
		return mInterval;
	}
	
	public String getUserName(String account) {
		return mSession.getUsername(account);
	}
	
	public String getUserImage(String account) {
		return mSession.getUserImage(account);
	}

	public void updateStatus(String accountId, String tweet, 
			GeoLocation location) throws TwitterException {
		Twitter twitter = mTwitterList.get(accountId);

		StatusUpdate status = new StatusUpdate(tweet);

		status.displayCoordinates(true);

		if(location != null) {
			status.setLocation(location);
		}

		twitter.updateStatus(status);
	}

	public synchronized int fetchStatusUpdates(String accountId, String user, 
			int type) { 
		return mFetchStatusUpdates.fetch(accountId, user, type);
	}

	public synchronized int fetchTimeline(String accountId, String user, 
			int type) { 
		return mFetchTimeline.fetch(accountId, user, type);
	}

	public synchronized int fetchFavourites(String accountId, String user, 
			int type) { 
		return mFetchFavourites.fetch(accountId, user, type);
	}

	public synchronized int fetchMentions(String accountId, String user, 
			int type) { 
		return mFetchMentions.fetch(accountId, user, type);
	}

	public synchronized int fetchRetweetsOf(String accountId, String user, 
			int type) { 
		return mFetchRetweetsOf.fetch(accountId, user, type);
	}

	public synchronized int fetchRetweetsBy(String accountId, String user, 
			int type) { 
		return mFetchRetweetsBy.fetch(accountId, user, type);
	}

	public synchronized int fetchFollowers(String accountId, String user, 
			int type) { 
		return mFetchFollowers.fetch(accountId, user, type);
	}

	public synchronized int fetchFollowing(String accountId, String user, 
			int type) { 
		return mFetchFollowing.fetch(accountId, user, type);
	}

	public synchronized int fetchProfileInfo(String accountId, String user, 
			int type) { 
		return mFetchProfileInfo.fetch(accountId, user, type);
	}

	public boolean isFollowing(String account, String personId) {
		return getStatusData().contains(StatusData.FOLLOWING_TABLE, account, 
				StatusData.C_ID, personId);
	}
	
	
	public synchronized void addNewFavourite(String account, 
			String tweetid) throws TwitterException{

		Twitter twitter = mTwitterList.get(account);

		if (twitter == null) {
			throw new TwitterException("Twitter connection info not initialized");
		}

		//Get the requested status
		Status status = twitter.showStatus(Long.valueOf(tweetid));

		if(status == null) {
			throw new TwitterException("Cant find requested status");
		}

		//Insert status into pending favourites.
		getStatusData().insertOrIgnore(StatusData.FAVOURITES_PENDING_TABLE, 
				StatusData.createTimelineContentValues(account, null, status));

		//Insert status into local favourites.
		getStatusData().insertOrIgnore(StatusData.FAVOURITES_TABLE, 
				StatusData.createTimelineContentValues(account, getUserName(account), status));
	}

	public synchronized void removeFavourite(String account, 
			String tweetid) throws TwitterException{

		//Remove favourite from local db..
		getStatusData().removeFavourite(account, tweetid);

		//Insert into pending unfavourite table
		ContentValues values = new ContentValues();
		values.put(StatusData.C_ID, tweetid);

		getStatusData().insertOrIgnore(StatusData.UNFAVOURITES_PENDING_TABLE, values);
	}

	public void updateCurrentFavourites(String account) throws TwitterException {
		Twitter twitter = mTwitterList.get(account);

		if (twitter == null) {
			throw new TwitterException("Twitter connection info not initialized");
		}

		//Get currently pending favourites
		Cursor cursor = getStatusData().getPendingFavourites(account, 
				new String[]{StatusData.C_ID});

		try{
			if(cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						Log.i(TAG, "Syncing new favourite");
						//For each new pending favourite, add it to favourites
						twitter.createFavorite(Long.valueOf(cursor.getString(0)));

						//Remove from pending list..
						getStatusData().removePendingFavourite(account, 
								cursor.getString(0));

					} while (cursor.moveToNext());
				}
			}
		} catch(RuntimeException e) {
			Log.e(TAG, "Error creating favourite", e);
		} finally {
			cursor.close();
		}

		//Get currently pending unfavourite.
		cursor = getStatusData().getPendingUnfavourites(account, 
				new String[]{StatusData.C_ID});

		try{
			if(cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						Log.i(TAG, "Syncing new unfavourites");

						//For each new pending favourite, add it to favourites
						twitter.destroyFavorite(Long.valueOf(cursor.getString(0)));

						//Remove from pending list..
						getStatusData().removePendingUnfavourite(account, 
								cursor.getString(0));
					} while (cursor.moveToNext());
				}
			}
		} catch(RuntimeException e) {
			Log.e(TAG, "Error creating favourite", e);
		} finally {
			cursor.close();
		}
	}
}
