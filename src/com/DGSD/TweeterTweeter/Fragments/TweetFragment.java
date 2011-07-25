package com.DGSD.TweeterTweeter.Fragments;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.StatusData;

public class TweetFragment extends DialogFragment {

	
	
	private String mAccount = null;
	private String mUser = null;
	private String mId = null;
	private String mCreatedAt = null;
	private String mText = null;
	private String mUserName = null;
	private String mScreenName = null;
	private String mImg = null;
	private String mFav = null;
	private String mSrc = null;
	private String mInReply = null;
	private String mOrigTweet = null;
	private String mRetweetCount = null;
	private String mPlaceName = null;
	private String mLat = null;
	private String mLong = null;
	private String mMediaEnt = null;
	private String mHashEnt = null;
	private String mUrlEnt = null;
	private String mUserEnt = null;
	
	public static TweetFragment newInstance(ContentValues statusVals) {
		TweetFragment f = new TweetFragment();

		Bundle args = new Bundle();

		args.putParcelable("values", statusVals);

		f.setArguments(args);

		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		parseValues((ContentValues)getArguments().getParcelable("values"));
	}
	
	public void parseValues(ContentValues values) {
		mAccount = values.getAsString(StatusData.C_ACCOUNT);
		mUser = values.getAsString(StatusData.C_USER);
		mId = values.getAsString(StatusData.C_ID);
		mCreatedAt = values.getAsString(StatusData.C_CREATED_AT);
		mText = values.getAsString(StatusData.C_TEXT);
		mUserName = values.getAsString(StatusData.C_USER_NAME);
		mScreenName = values.getAsString(StatusData.C_SCREEN_NAME);
		mImg = values.getAsString(StatusData.C_IMG);
		mFav = values.getAsString(StatusData.C_FAV);
		mSrc = values.getAsString(StatusData.C_SRC);
		mInReply = values.getAsString(StatusData.C_IN_REPLY);
		mOrigTweet = values.getAsString(StatusData.C_ORIG_TWEET);
		mRetweetCount = values.getAsString(StatusData.C_RETWEET_COUNT);
		mPlaceName = values.getAsString(StatusData.C_PLACE_NAME);
		mLat = values.getAsString(StatusData.C_LAT);
		mLong = values.getAsString(StatusData.C_LONG);
		mMediaEnt = values.getAsString(StatusData.C_MEDIA_ENT);
		mHashEnt = values.getAsString(StatusData.C_HASH_ENT);
		mUrlEnt = values.getAsString(StatusData.C_URL_ENT);
		mUserEnt = values.getAsString(StatusData.C_USER_ENT);
		
		System.err.println("mAccount = " + mAccount);
		System.err.println("mUser = " + mUser);
		System.err.println("mId = " + mId);
		System.err.println("mCreatedAt = " + mCreatedAt);
		System.err.println("mText = " + mText);
		System.err.println("mUserName = " + mUserName);
		System.err.println("mScreenName = " + mScreenName);
		System.err.println("mImg = " + mImg);
		System.err.println("mFav = " + mFav);
		System.err.println("mSrc = " + mSrc);
		System.err.println("mInReply = " + mInReply);
		System.err.println("mOrigTweet = " + mOrigTweet);
		System.err.println("mRetweetCount = " + mRetweetCount);
		System.err.println("mPlaceName = " + mPlaceName);
		System.err.println("mLat = " + mLat);
		System.err.println("mLong = " + mLong);
		System.err.println("mMediaEnt = " + mMediaEnt);
		System.err.println("mHashEnt = " + mHashEnt);
		System.err.println("mUrlEnt = " + mUrlEnt);
		System.err.println("mUserEnt = " + mUserEnt);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		TextView tv = new TextView(getActivity());
		tv.setText("mAccount = " + mAccount+"\n"+
				"mUser = " + mUser+"\n"+
				"mId = " + mId+"\n"+
				"mCreatedAt = " + mCreatedAt+"\n"+
				"mText = " + mText+"\n"+
				"mUserName = " + mUserName+"\n"+
				"mScreenName = " + mScreenName+"\n"+
				"mImg = " + mImg+"\n"+
				"mFav = " + mFav+"\n"+
				"mSrc = " + mSrc+"\n"+
				"mInReply = " + mInReply+"\n"+
				"mOrigTweet = " + mOrigTweet+"\n"+
				"mRetweetCount = " + mRetweetCount+"\n"+
				"mPlaceName = " + mPlaceName+"\n"+
				"mLat = " + mLat+"\n"+
				"mLong = " + mLong+"\n"+
				"mMediaEnt = " + mMediaEnt+"\n"+
				"mHashEnt = " + mHashEnt+"\n"+
				"mUrlEnt = " + mUrlEnt+"\n"+
				"mUserEnt = " + mUserEnt+"\n");
		
		return tv;
	}

}
