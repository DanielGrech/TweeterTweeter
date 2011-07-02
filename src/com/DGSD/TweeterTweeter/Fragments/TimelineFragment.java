package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.Utils.Log;

public class TimelineFragment extends BaseStatusFragment {

	private static final String TAG = TimelineFragment.class.getSimpleName();

	public static TimelineFragment newInstance(String accountId, String user){
		TimelineFragment f = new TimelineFragment();

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

		Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public synchronized void setupList() throws TwitterException {
		Log.i(TAG, "Setting up list");

		if(mUserName == null) {
			//We have this info in the db already..
			mCursor = mApplication.getStatusData().getTimeline(mAccountId, mUserName, FROM);
		} else {
			mCursor = mApplication.getStatusData().getTimeline(mAccountId, 
					mApplication.getTwitterSession().getUsername(mAccountId), FROM);
		}
	}
}
