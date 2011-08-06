package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class DataFetcher {
	public static final String TAG = DataFetcher.class.getSimpleName();

	public static final int FETCH_NEWEST = 0;

	public static final int FETCH_OLDER = 1;

	public abstract int fetchData(String account, String user, int type) throws TwitterException;

	protected int count;

	protected Twitter mTwitter;

	protected TTApplication mApplication;

	public DataFetcher(TTApplication app) {
		mApplication = app;

	}

	public int fetch(String account, String user, int type) {
		mTwitter = mApplication.getTwitter(account);
		if (mTwitter == null) {
			Log.d(TAG, "Twitter connection info not initialized");
			return 0;
		}
		try{
			count = 0;
			return fetchData(account, user, type);
		} catch (TwitterException e) {
			Log.e(TAG, "Error connecting to Twitter service", e);
			return -1;
		} catch (NullPointerException e) {
			Log.e(TAG, "Null pointer getting data. Maybe there was none?", e);
			return 0;
		} catch (RuntimeException e) {
			Log.e(TAG, "Failed to fetch data", e);
			return -1;
		} 
	}
}
