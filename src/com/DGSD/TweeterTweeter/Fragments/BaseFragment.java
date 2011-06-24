package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;

public abstract class BaseFragment extends DialogFragment{

	private static final String TAG = BaseFragment.class.getSimpleName();

	protected static final int ELEMENTS_PER_PAGE = 100;
	
	/*
	 * This method MUST be called from a background thread, as it is
	 * free to do network comms or loading from a db..
	 */
	public abstract void setupList() throws TwitterException;
	//public abstract Cursor loadData();
	//public abstract Cursor loadNewDataItems();

	protected TTApplication mApplication;

	protected ListView mListView;

	protected BaseAdapter mAdapter;
	
	protected String mAccountId;
	
	protected String mUserName;
	
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

		mListView = (ListView) root.findViewById(R.id.list);
		
		new DataLoadingTask().execute();

		Log.i(TAG, "Returning root from onCreateView");

		return root;
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
		private boolean mRefreshing;

		private boolean hasError = false;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void ...args) {
			if(mRefreshing)
				return null;

			try {
				//Paging p = new Paging(pageNum, ELEMENTS_PER_PAGE);

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
			if(hasError) {
				Toast.makeText(getActivity(), "Error getting data", Toast.LENGTH_SHORT).show();
			}	
			else {
				mListView.setAdapter(mAdapter);
			}

		}
	}
}
