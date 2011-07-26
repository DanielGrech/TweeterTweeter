package com.DGSD.TweeterTweeter;

import android.content.ContentValues;

public class TweetData {
	public String account = null;
	public String user = null;
	public String id = null;
	public String date = null;
	public String text = null;
	public String userName = null;
	public String screenName = null;
	public String img = null;
	public String fav = null;
	public String src = null;
	public String inReply = null;
	public String origTweet = null;
	public String retweetCount = null;
	public String placeName = null;
	public String lat = null;
	public String lon = null;
	public String mediaEnt = null;
	public String hashEnt = null;
	public String urlEnt = null;
	public String userEnt = null;

	public TweetData(ContentValues c) {
		parseValues(c);
	}

	public void parseValues(ContentValues values) {
		account = values.getAsString(StatusData.C_ACCOUNT);
		user = values.getAsString(StatusData.C_USER);
		id = values.getAsString(StatusData.C_ID);
		date = values.getAsString(StatusData.C_CREATED_AT);
		text = values.getAsString(StatusData.C_TEXT);
		userName = values.getAsString(StatusData.C_USER_NAME);
		screenName = values.getAsString(StatusData.C_SCREEN_NAME);
		img = values.getAsString(StatusData.C_IMG);
		fav = values.getAsString(StatusData.C_FAV);
		src = values.getAsString(StatusData.C_SRC);
		inReply = values.getAsString(StatusData.C_IN_REPLY);
		origTweet = values.getAsString(StatusData.C_ORIG_TWEET);
		retweetCount = values.getAsString(StatusData.C_RETWEET_COUNT);
		placeName = values.getAsString(StatusData.C_PLACE_NAME);
		lat = values.getAsString(StatusData.C_LAT);
		lon = values.getAsString(StatusData.C_LONG);
		mediaEnt = values.getAsString(StatusData.C_MEDIA_ENT);
		hashEnt = values.getAsString(StatusData.C_HASH_ENT);
		urlEnt = values.getAsString(StatusData.C_URL_ENT);
		userEnt = values.getAsString(StatusData.C_USER_ENT);

		System.err.println("mAccount = " + account);
		System.err.println("mUser = " + user);
		System.err.println("mId = " + id);
		System.err.println("mCreatedAt = " + date);
		System.err.println("mText = " + text);
		System.err.println("mUserName = " + userName);
		System.err.println("mScreenName = " + screenName);
		System.err.println("mImg = " + img);
		System.err.println("mFav = " + fav);
		System.err.println("mSrc = " + src);
		System.err.println("mInReply = " + inReply);
		System.err.println("mOrigTweet = " + origTweet);
		System.err.println("mRetweetCount = " + retweetCount);
		System.err.println("mPlaceName = " + placeName);
		System.err.println("mLat = " + lat);
		System.err.println("mLong = " + lon);
		System.err.println("mMediaEnt = " + mediaEnt);
		System.err.println("mHashEnt = " + hashEnt);
		System.err.println("mUrlEnt = " + urlEnt);
		System.err.println("mUserEnt = " + userEnt);
	}
}