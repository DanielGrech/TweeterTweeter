package com.DGSD.TweeterTweeter.UI.Adapters;

import twitter4j.TwitterException;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.BaseFragment;
import com.DGSD.TweeterTweeter.Utils.Log;

public class EndlessListAdapter extends EndlessAdapter {
	private static final String TAG = EndlessListAdapter.class.getSimpleName();

	private LinearLayout mPendingView = null;

	private BaseFragment mFragment;

	public EndlessListAdapter(BaseFragment fragment, SimpleCursorAdapter sca) {
		super(sca);

		mFragment = fragment;

		setupPendingView();
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		return mPendingView;
	}

	@Override
	protected boolean cacheInBackground() throws TwitterException{
		Log.i(TAG, "GETTING OLDER DATA");
		mFragment.getOlder();
		return false;
	}

	@Override
	protected void appendCachedData() {
		Log.i(TAG, "APPENDING DATA TO LIST!");
		mFragment.attachOldData();
	}

	@Override
	protected boolean onException(View pendingView, Exception e){
		Log.i(TAG, "ERROR LOADING EXTRA DATA", e);

		Toast.makeText(mFragment.getActivity(), "Error loading data..", 
				Toast.LENGTH_LONG).show();

		return false;
	}

	public ListAdapter getAdapter() {
		return getWrappedAdapter();
	}

	private void setupPendingView() {
		mPendingView = new LinearLayout(mFragment.getActivity());

		mPendingView.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mPendingView.setGravity(Gravity.CENTER);

		ProgressBar pb = new ProgressBar(mFragment.getActivity());
		pb.setIndeterminate(true);

		LayoutParams lp = new ListView.LayoutParams( LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT );

		pb.setLayoutParams(lp);

		mPendingView.addView(pb);
	}
}