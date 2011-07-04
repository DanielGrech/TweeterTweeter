package com.DGSD.TweeterTweeter.Tasks;

import android.os.AsyncTask;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.BaseFragment;
import com.DGSD.TweeterTweeter.Utils.Log;

public class DataLoadingTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = DataLoadingTask.class.getSimpleName();
	
	private boolean hasError = false;

	public static final int CURRENT = 0;
	
	public static final int NEWEST = 1;
	
	public static final int OLDEST = 2;
	
	private int mType;
	
	private BaseFragment mFragment;
	
	public DataLoadingTask(BaseFragment fragment, int type) {
		mFragment = fragment;
		
		mType = type;
	}
	
	@Override
	protected void onPreExecute() {

	}

	@Override
	protected Void doInBackground(Void ...args) {
		try {
			if(mFragment.getAdapter() != null) {
				synchronized(mFragment.getAdapter()) {
					switch(mType) {
						case CURRENT: mFragment.getCurrent(); break;
						case NEWEST: mFragment.getNewest(); break;
						case OLDEST: mFragment.getOlder(); break;
					}
				}
			}
			else {
				switch(mType) {
					case CURRENT: mFragment.getCurrent(); break;
					case NEWEST: mFragment.getNewest(); break;
					case OLDEST: mFragment.getOlder(); break;
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
		if(mFragment.getListView() != null && mFragment.getListView().isRefreshing()) {
			mFragment.getListView().onRefreshComplete();
		}

		if(hasError) {
			Toast.makeText(mFragment.getActivity(), "Error getting data", Toast.LENGTH_SHORT).show();
		}	
		else {
			mFragment.appendData();
		}
	}
}