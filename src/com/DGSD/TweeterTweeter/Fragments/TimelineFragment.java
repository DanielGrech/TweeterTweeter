package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TimelineAdapter;

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

		Cursor cursor;
		if(mUserName == null) {
			//We have this info in the db already..
			cursor = mApplication.getStatusData().getStatusUpdates(mAccountId);
			
			getActivity().startManagingCursor(cursor);

			mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
					cursor, FROM, TO, 0);

			((SimpleCursorAdapter)mAdapter).setViewBinder(mViewBinder);
		}
		else {
			//We need to download the info from the network..
			ResponseList<Status> favsList = mApplication.getTwitter(mAccountId).getUserTimeline(mUserName);
			
			mAdapter = new TimelineAdapter(getActivity(), favsList);
		}
	}

}
