package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;

import com.DGSD.TweeterTweeter.DataFetchers.DataFetcher;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

public class HomeTimelineFragment extends BaseStatusFragment {

	private static final String TAG = HomeTimelineFragment.class.getSimpleName();

	public static HomeTimelineFragment newInstance(String accountId){
		HomeTimelineFragment f = new HomeTimelineFragment();

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
		return UpdaterService.DATATYPES.HOME_TIMELINE;
	}
	
	@Override
	public synchronized Cursor getCurrent() {
		Log.i(TAG, "Getting current");
		return mApplication.getStatusData().getStatusUpdates(mAccountId, null);
	}

	@Override
	public synchronized boolean getNewest() {
		Log.i(TAG, "Getting newest");
		
		if(mApplication.fetchStatusUpdates(mAccountId, mUserName, 
				DataFetcher.FETCH_NEWEST) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public synchronized boolean getOlder() {
		Log.i(TAG, "Getting older");
		
		if(mApplication.fetchStatusUpdates(mAccountId, mUserName, 
				DataFetcher.FETCH_OLDER) > 0) {
			return true;
		} else {
			return false;
		}
	}
	
}
