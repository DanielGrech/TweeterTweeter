package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.UI.EndlessAdapter;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView.OnRefreshListener;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseFragment extends DialogFragment{

	private static final String TAG = BaseFragment.class.getSimpleName();

	public static final int ELEMENTS_PER_PAGE = 100;

	/*
	 * This method MUST be called from a background thread, as it is
	 * free to do network comms or loading from a db..
	 */
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

		new DataLoadingTask(DataLoadingTask.CURRENT).execute();

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
				new DataLoadingTask(DataLoadingTask.NEWEST).execute();
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

	protected class EndlessListAdapter extends EndlessAdapter {
		private RotateAnimation mRotate = null;

		private ImageView mImageView = null;
		
		public EndlessListAdapter(SimpleCursorAdapter sca) {
			super(sca);

			mRotate = 
				new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,0.5f, 
						Animation.RELATIVE_TO_SELF,	0.5f);
			mRotate.setDuration(600);
			mRotate.setRepeatMode(Animation.RESTART);
			mRotate.setRepeatCount(Animation.INFINITE);
			
			mImageView = new ImageView(getActivity());
			mImageView.setImageResource(R.drawable.ic_popup_sync);

		}
		
		@Override
		protected View getPendingView(ViewGroup parent) {
			mImageView.startAnimation(mRotate);

			return(mImageView);
		}

		@Override
		protected boolean cacheInBackground() throws TwitterException{
			Log.i(TAG, "GETTING OLDER DATA");
			getOlder();
			return false;
		}
		
		@Override
		protected void appendCachedData() {
			Log.i(TAG, "APPENDING DATA TO LIST!");
			appendData();
		}
		
		@Override
		protected boolean onException(View pendingView, Exception e){
			Log.i(TAG, "ERROR LOADING EXTRA DATA", e);
			
			Toast.makeText(getActivity(), "Error loading data..", 
					Toast.LENGTH_LONG).show();
		
			return false;
		}
		
		public ListAdapter getAdapter() {
			return getWrappedAdapter();
		}
	}
	
	protected class DataLoadingTask extends AsyncTask<Void, Void, Void> {
		private boolean hasError = false;

		public static final int CURRENT = 0;
		
		public static final int NEWEST = 1;
		
		public static final int OLDEST = 2;
		
		private int mType;
		
		public DataLoadingTask(int type) {
			mType = type;
		}
		
		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void ...args) {
			try {
				if(mAdapter != null) {
					synchronized(mAdapter) {
						switch(mType) {
							case CURRENT: getCurrent(); break;
							case NEWEST: getNewest(); break;
							case OLDEST: getOlder(); break;
						}
					}
				}
				else {
					switch(mType) {
						case CURRENT: getCurrent(); break;
						case NEWEST: getNewest(); break;
						case OLDEST: getOlder(); break;
					}
				}
				//pageNum++;
			} catch (Exception e) {
				Log.e(TAG, "Error getting data",e);
				hasError = true;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			Log.i(TAG, "POST EXECUTING");
			//Check if the refresh view is showing..
			if(mListView != null && mListView.isRefreshing()) {
				mListView.onRefreshComplete();
			}

			if(hasError) {
				Toast.makeText(getActivity(), "Error getting data", Toast.LENGTH_SHORT).show();
			}	
			else {
				appendData();
			}
		}
	}
}
