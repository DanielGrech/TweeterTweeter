package com.DGSD.TweeterTweeter.Fragments;

import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;
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
	public synchronized void setupList() {
		Log.i(TAG, "Setting up list");

		mCursor = mApplication.getStatusData().getStatusUpdates(mAccountId);
		//getActivity().startManagingCursor(mCursor);

		if(mAdapter == null) {
			mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
					mCursor, FROM, TO, 0);
		}

		((SimpleCursorAdapter)mAdapter).setViewBinder(mViewBinder);
	}

}
