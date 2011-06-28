package com.DGSD.TweeterTweeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import twitter4j.GeoLocation;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.app.AlarmManager;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import com.DGSD.TweeterTweeter.Utils.Log;

public class TTApplication extends Application implements
OnSharedPreferenceChangeListener {

	private static final String TAG = TTApplication.class.getSimpleName();

	public static final int INTERVAL_NEVER = -1;
	
	public static String CONSUMER_KEY;
	
	public static String CONSUMER_SECRET;

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
	
	private FetchFavourites    mFetchFavourites;
	
	private FetchMentions      mFetchMentions;
	
	private FetchRetweetsOfMe  mFetchRetweetsOfMe;
	
	private FetchRetweetsByMe  mFetchRetweetsByMe;
	
	private FetchFollowers     mFetchFollowers;
	
	private FetchFollowing     mFetchFollowing;
	
	private FetchProfileInfo   mFetchProfileInfo;

	@Override
	public void onCreate() {  
		super.onCreate();

		CONSUMER_KEY = getResources().getString(R.string.consumer_key);
		CONSUMER_SECRET = getResources().getString(R.string.consumer_secret);
	
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

		mFetchStatusUpdates = new FetchStatusUpdates();
		
		mFetchFavourites = new FetchFavourites();
		
		mFetchMentions = new FetchMentions();
		
		mFetchRetweetsOfMe = new FetchRetweetsOfMe();
		
		mFetchRetweetsByMe = new FetchRetweetsByMe();
		
		mFetchFollowers = new FetchFollowers();
		
		mFetchFollowing = new FetchFollowing();
		
		mFetchProfileInfo = new FetchProfileInfo();
		
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
	
	public synchronized int fetchStatusUpdates(String accountId) { 
		return mFetchStatusUpdates.fetch(accountId);
	}
	
	public synchronized int fetchFavourites(String accountId) {  
		return mFetchFavourites.fetch(accountId);
	}
	
	public synchronized int fetchMentions(String accountId) {  
		return mFetchMentions.fetch(accountId);
	}
	
	public synchronized int fetchRetweetsOfMe(String accountId) {  
		return mFetchRetweetsOfMe.fetch(accountId);
	}
	
	public synchronized int fetchRetweetsByMe(String accountId) {  
		return mFetchRetweetsByMe.fetch(accountId);
	}
	
	public synchronized int fetchFollowers(String accountId) {
		return mFetchFollowers.fetch(accountId);
	}
	
	public synchronized int fetchFollowing(String accountId) {
		return mFetchFollowing.fetch(accountId);
	}
	
	public synchronized int fetchProfileInfo(String accountId) {  
		return mFetchProfileInfo.fetch(accountId);
	}
	
	/*
	 * Utilities to fetch data from the network
	 */
	public abstract class Fetch {
		public abstract int fetchData(String Account) throws TwitterException;
		
		protected int count;
		
		protected Twitter twitter;
		
		protected int fetch(String account) {
			twitter = mTwitterList.get(account);
			if (twitter == null) {
				Log.d(TAG, "Twitter connection info not initialized");
				return 0;
			}
			try{
				count = 0;
				return fetchData(account);
			} catch (TwitterException e) {
				Log.e(TAG, "Error connecting to Twitter service", e);
				return -1;
			} catch (RuntimeException e) {
				Log.e(TAG, "Failed to fetch data", e);
				return -1;
			} 
		}
	}
	
	public class FetchStatusUpdates extends Fetch {
		public int fetchData(String account) throws TwitterException {
			ResponseList<Status> timeline = twitter.getHomeTimeline();

			long latestCreatedAtTime = getStatusData()
				.getLatestCreatedAtTime(StatusData.TIMELINE_TABLE, account);
			
			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(account, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				getStatusData().insertOrIgnore(StatusData.TIMELINE_TABLE, values);

				if (latestCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			return count;
		}
	}

	public class FetchMentions extends Fetch {
		public int fetchData(String account) throws TwitterException {
			ResponseList<Status> timeline = twitter.getMentions();

			long latestCreatedAtTime = getStatusData()
				.getLatestCreatedAtTime(StatusData.MENTIONS_TABLE, account);
			
			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(account, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				getStatusData().insertOrIgnore(StatusData.MENTIONS_TABLE, values);
				
				if (latestCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			Log.d(TAG, "Finished getting mentions");
			return count;
		}
	}
	
	public class FetchRetweetsOfMe extends Fetch {
		public int fetchData(String account) throws TwitterException {
			ResponseList<Status> timeline = twitter.getRetweetsOfMe();

			long latestCreatedAtTime = getStatusData()
				.getLatestCreatedAtTime(StatusData.RT_OF_TABLE, account);
			
			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(account, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				getStatusData().insertOrIgnore(StatusData.RT_OF_TABLE, values);
				
				if (latestCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			Log.d(TAG, "Finished getting retweets of me");
		
			return count;
		}
	}

	public class FetchRetweetsByMe extends Fetch {
		public int fetchData(String account) throws TwitterException {
			ResponseList<Status> timeline = twitter.getRetweetedByMe();

			long latestCreatedAtTime = getStatusData()
				.getLatestCreatedAtTime(StatusData.RT_BY_TABLE, account);
			
			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(account, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				getStatusData().insertOrIgnore(StatusData.RT_BY_TABLE, values);
				
				if (latestCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			Log.d(TAG, "Finished getting retweets by me");
			
			return count;
		}
	}
	
	public class FetchFavourites extends Fetch {
		public int fetchData(String account) throws TwitterException {
			ResponseList<Status> timeline = twitter.getFavorites();

			long latestCreatedAtTime = getStatusData()
				.getLatestCreatedAtTime(StatusData.FAVOURITES_TABLE, account);
			
			ContentValues values;
			for (Status status : timeline) {
				values = StatusData.createTimelineContentValues(account, Long.toString(status.getId()), 
						Long.toString(status.getCreatedAt().getTime()), status.getUser().getScreenName(), 
						status.getText(), status.getUser().getProfileImageURL().toString(),
						status.isFavorited(), status.getSource());

				getStatusData().insertOrIgnore(StatusData.FAVOURITES_TABLE, values);
				
				if (latestCreatedAtTime < status.getCreatedAt().getTime()) {
					count++;
				}
			}

			Log.d(TAG, "Finished getting favourites");

			return count;
		}
	}

	public class FetchFollowers extends Fetch {
		public int fetchData(String account) throws TwitterException {
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
				values = StatusData.createUserContentValues(account, u);

				getStatusData().insertOrIgnore(StatusData.FOLLOWERS_TABLE, values);
			}

			Log.d(TAG, "Finished getting followers");

			return count;
		}
	}
	
	public class FetchFollowing extends Fetch {
		public int fetchData(String account) throws TwitterException {
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
				values = StatusData.createUserContentValues(account, u);

				getStatusData().insertOrIgnore(StatusData.FOLLOWING_TABLE, values);
			}

			Log.d(TAG, "Finished getting friends");
			return count;
		}
	}
	
	public class FetchProfileInfo extends Fetch {
		public int fetchData(String account) throws TwitterException {
			User u = twitter.showUser(twitter.getId());

			if(u != null){
				ContentValues values = StatusData.createUserContentValues(account, u);

				getStatusData().insertOrIgnore(StatusData.PROFILE_TABLE, values);
			}

			Log.d(TAG, "Finished getting profile");
	
			return count;
		}	
	}
}
