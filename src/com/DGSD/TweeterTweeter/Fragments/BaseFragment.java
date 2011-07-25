package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.app.DialogFragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.UI.Adapters.EndlessListAdapter;
import com.DGSD.TweeterTweeter.UI.Adapters.TimelineCursorAdapter;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseFragment extends DialogFragment {

	private static final String TAG = BaseFragment.class.getSimpleName();

	public static final int ELEMENTS_PER_PAGE = 50;

	public abstract Cursor getNewest() throws TwitterException;

	public abstract Cursor getCurrent() throws TwitterException;

	public abstract Cursor getOlder() throws TwitterException;

	public abstract void appendData();

	protected TTApplication mApplication;

	protected ListView mListView;

	protected ListAdapter mAdapter;

	protected Cursor mCursor;

	protected String mAccountId;

	protected String mUserName;
	
	protected AsyncTask<Void, Void, Cursor> mCurrentTask;
	
	protected ActionMode mCurrentActionMode;
	
	//The type of data returned from the updater service
	protected int mType = -1;

	//What page of tweets we want to load..

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mApplication = (TTApplication) getActivity().getApplication();		

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(mListView != null) {
			mListView.setFastScrollEnabled(true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle b){
		super.onSaveInstanceState(b);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mAdapter = null;

		mCursor = null;

		mListView = null;

		Log.i(TAG, "Destroying view");
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    if(mListView != null) {
	    	mListView.setVisibility(View.GONE);
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    if(mListView != null) {
	    	mListView.setVisibility(View.VISIBLE);
	    }
	}

	protected void showPanel(View panel, boolean slideUp) {
		panel.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				slideUp ? R.anim.slide_in : R.anim.slide_out_top));
		panel.setVisibility(View.VISIBLE);
	}

	protected void hidePanel(View panel, boolean slideDown) {
		panel.startAnimation(AnimationUtils.loadAnimation(getActivity(),
				slideDown ? R.anim.slide_out : R.anim.slide_in_top));
		panel.setVisibility(View.GONE);
	}

	public ListAdapter getAdapter() {
		return mAdapter;
	}

	public ListView getListView() {
		return mListView;
	}
	
	public void setCursor(Cursor c) {
		mCursor = c;
		if(mAdapter != null && ((EndlessListAdapter)mAdapter).getAdapter() != null) {
			((SimpleCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).changeCursor(mCursor);
			((SimpleCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).notifyDataSetChanged();
		}
	}
}
