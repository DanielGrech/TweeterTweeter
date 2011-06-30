package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView.OnRefreshListener;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseFragment extends DialogFragment{

	private static final String TAG = BaseFragment.class.getSimpleName();

	protected static final int ELEMENTS_PER_PAGE = 100;
	
	/*
	 * This method MUST be called from a background thread, as it is
	 * free to do network comms or loading from a db..
	 */
	public abstract void setupList() throws TwitterException;
	
	public abstract void postSetup(boolean isUpdate);

	protected TTApplication mApplication;

	protected PullToRefreshListView mListView;

	protected ListAdapter mAdapter;
	
	protected String mAccountId;
	
	protected String mUserName;
	
	//The type of data returned from the updater service
	protected int mType = -1;
	
	//What page of tweets we want to load..
	protected int pageNum = 1;

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mApplication = (TTApplication) getActivity().getApplication();		
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.list_fragment_layout, container, false);

		mListView = (PullToRefreshListView) root.findViewById(R.id.list);
		
		new DataLoadingTask(false).execute();

		Log.i(TAG, "Returning root from onCreateView");

		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mListView.setOnRefreshListener(new OnRefreshListener() {
		    @Override
		    public void onRefresh() {
		    	Log.i(TAG, "STARTING A REFRESH FOR TYPE: " + mType + " ACCOUNT: " + mAccountId);
		    	Intent intent = new Intent(getActivity(), UpdaterService.class);
		    	intent.putExtra(UpdaterService.DATA_TYPE, mType);
		    	intent.putExtra(UpdaterService.ACCOUNT, mAccountId);
		    	intent.putExtra(UpdaterService.USER, mUserName);
		    	getActivity().startService(intent);
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
		
		mListView = null;

		Log.i(TAG, "Destroying view");
	}


	protected class DataLoadingTask extends AsyncTask<Void, Void, Void> {
		private boolean hasError = false;
		
		private boolean mIsUpdate;
		
		public DataLoadingTask(boolean isUpdate) {
			mIsUpdate = isUpdate;
		}
		
		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void ...args) {
			try {
				//Paging p = new Paging(pageNum, ELEMENTS_PER_PAGE);
				Log.i(TAG, "DOING IN BACKGROUND");
				if(mAdapter != null) {
					synchronized(mAdapter) {
						setupList();
					}
				}
				else {
					setupList();
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
			if(mListView.isRefreshing()) {
				mListView.onRefreshComplete();
			}
			
			if(hasError) {
				Toast.makeText(getActivity(), "Error getting data", Toast.LENGTH_SHORT).show();
			}	
			else {
				postSetup(mIsUpdate);
			}

		}
	}
}
