package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.EndlessListAdapter;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView.OnRefreshListener;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseFragment extends DialogFragment{

	private static final String TAG = BaseFragment.class.getSimpleName();

	public static final int ELEMENTS_PER_PAGE = 50;

	public abstract void getNewest() throws TwitterException;
	
	public abstract void getCurrent() throws TwitterException;
	
	public abstract void getOlder() throws TwitterException;
	
	public abstract void appendData();
	
	protected TTApplication mApplication;

	protected PullToRefreshListView mListView;
	
	protected EndlessListAdapter mAdapter;
	
	protected Cursor mCursor;
	
	protected String mAccountId;

	protected String mUserName;

	//The type of data returned from the updater service
	protected int mType = -1;

	//What page of tweets we want to load..

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mApplication = (TTApplication) getActivity().getApplication();		

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.list_fragment_layout, container, false);

		mListView = (PullToRefreshListView) root.findViewById(R.id.list);

		new DataLoadingTask(BaseFragment.this, DataLoadingTask.CURRENT).execute();

		Log.i(TAG, "Returning root from onCreateView");

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.i(TAG, "STARTING REFRESH!");
				new DataLoadingTask(BaseFragment.this, DataLoadingTask.NEWEST).execute();
			}
		});
		
		mListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(mAdapter != null) {
					mAdapter.setKeepApending(true);
				}
				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});
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

	public EndlessListAdapter getAdapter() {
		return mAdapter;
	}
	
	public PullToRefreshListView getListView() {
		return mListView;
	}
}
