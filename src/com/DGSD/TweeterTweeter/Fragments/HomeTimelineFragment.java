package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

		mType = UpdaterService.DATATYPES.HOME_TIMELINE;
		
		if(mUserName == null) {
			mUserName = mApplication.getTwitterSession().getUsername(mAccountId);
		}
		
		Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDetach();
		try{
			if(mCursor != null) {
				mCursor.close();
			}
		}catch(RuntimeException e) {
			Log.e(TAG, "Error closing cursor", e);
		}
		Log.i(TAG, "Destroying Fragment");
	}
	
	@Override
	public synchronized Cursor getCurrent() {
		Log.i(TAG, "Getting current");
		return mApplication.getStatusData().getStatusUpdates(mAccountId, null);
	}

	@Override
	public synchronized Cursor getNewest() {
		Log.i(TAG, "Getting newest");
		
		mApplication.fetchStatusUpdates(mAccountId, mUserName, 
				DataFetcher.FETCH_NEWEST);
		
		return getCurrent();
	}
	
	@Override
	public synchronized Cursor getOlder() {
		Log.i(TAG, "Getting older");
		
		mApplication.fetchStatusUpdates(mAccountId, mUserName, 
				DataFetcher.FETCH_OLDER);
		
		return getCurrent();
	}
	
}
