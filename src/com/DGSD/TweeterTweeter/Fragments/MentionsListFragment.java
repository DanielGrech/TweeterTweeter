package com.DGSD.TweeterTweeter.Fragments;

import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;

public class MentionsListFragment extends BaseStatusFragment {

	private static final String TAG = MentionsListFragment.class.getSimpleName();
	
	public static MentionsListFragment newInstance(String accountId){
		MentionsListFragment f = new MentionsListFragment();

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
	public synchronized void setupList() {
		Log.i(TAG, "Setting up list");
		
		mCursor = mApplication.getStatusData().getMentions(mAccountId);
		getActivity().startManagingCursor(mCursor);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
				mCursor, FROM, TO, 0);
		
		((SimpleCursorAdapter)mAdapter).setViewBinder(mViewBinder);
	}

}