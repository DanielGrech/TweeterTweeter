package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import com.DGSD.TweeterTweeter.DataFetchers.DataFetcher;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

public class RetweetsByFragment extends BaseStatusFragment {

	private static final String TAG = RetweetsByFragment.class.getSimpleName();

	public static RetweetsByFragment newInstance(String accountId, String user){
		RetweetsByFragment f = new RetweetsByFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();

		args.putString("accountId", accountId);

		args.putString("username", user);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mAccountId = getArguments().getString("accountId");

		mUserName = getArguments().getString("username");
		
		if(mUserName == null) {
			mUserName = mApplication.getTwitterSession().getUsername(mAccountId);
		}
		
		Log.i(TAG, "onCreate");
	}

	@Override
	public int getType() {
		return UpdaterService.DATATYPES.RETWEETS_BY;
	}
	
	
	@Override
	public synchronized Cursor getCurrent() {
		Log.i(TAG, "Getting current");
		return mApplication.getStatusData().getRetweetsBy(mAccountId, mUserName, null);
	}

	@Override
	public synchronized boolean getNewest() {
		Log.i(TAG, "Getting newest");
		
		if(mApplication.fetchRetweetsBy(mAccountId, mUserName, 
				DataFetcher.FETCH_NEWEST) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public synchronized boolean getOlder() {
		Log.i(TAG, "Getting oldest");
		
		if(mApplication.fetchRetweetsBy(mAccountId, mUserName, 
				DataFetcher.FETCH_OLDER) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String getTitle() {
		return "Retweeted by you";
	}
}
