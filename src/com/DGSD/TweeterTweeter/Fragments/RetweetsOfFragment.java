package com.DGSD.TweeterTweeter.Fragments;

import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;

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
		
		mType = UpdaterService.DATATYPES.RETWEETS_OF;
		
		Log.i(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public synchronized void setupList() {
		Log.i(TAG, "Setting up list");
		
		mCursor = mApplication.getStatusData().getRetweetsOf(mAccountId);
		getActivity().startManagingCursor(mCursor);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
				mCursor, FROM, TO, 0);
		
		((SimpleCursorAdapter)mAdapter).setViewBinder(mViewBinder);
	}

}
