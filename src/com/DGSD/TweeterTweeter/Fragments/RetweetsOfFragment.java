package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import com.DGSD.TweeterTweeter.DataFetchers.DataFetcher;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

public class RetweetsOfFragment extends BaseStatusFragment {

	private static final String TAG = RetweetsOfFragment.class.getSimpleName();

	public static RetweetsOfFragment newInstance(String accountId){
		RetweetsOfFragment f = new RetweetsOfFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		
		args.putString("accountId", accountId);

		f.setArguments(args);

		return f;
	}


	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mAccountId = getArguments().getString("accountId");

		if(mUserName == null) {
			mUserName = mApplication.getTwitterSession().getUsername(mAccountId);
		}
		
		Log.i(TAG, "onCreate");
	}

	@Override
	public int getType() {
		return UpdaterService.DATATYPES.RETWEETS_OF;
	}
	
	@Override
	public synchronized Cursor getCurrent() {
		Log.i(TAG, "Getting current");
		return mApplication.getStatusData().getRetweetsOf(mAccountId, null);
	}

	@Override
	public synchronized boolean getNewest() {
		Log.i(TAG, "Getting newest");
		
		if(mApplication.fetchRetweetsOf(mAccountId, mUserName, 
				DataFetcher.FETCH_NEWEST) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public synchronized boolean getOlder() {
		Log.i(TAG, "Getting oldest");
		
		if(mApplication.fetchRetweetsOf(mAccountId, mUserName, 
				DataFetcher.FETCH_OLDER) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String getTitle() {
		return "Retweets of you";
	}
}
