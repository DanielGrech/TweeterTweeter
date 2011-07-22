package com.DGSD.TweeterTweeter.Tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.BaseFragment;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.Utils.Log;

public class DataLoadingTask extends AsyncTask<Void, Void, Cursor> {
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
	protected Cursor doInBackground(Void ...args) {
		try {
			if(mFragment.getAdapter() != null) {
				synchronized(mFragment.getAdapter()) {
					switch(mType) {
						case CURRENT: return mFragment.getCurrent();
						case NEWEST: return mFragment.getNewest();
						case OLDEST: return mFragment.getOlder();
					}
				}
			} else {
				switch(mType) {
					case CURRENT: return mFragment.getCurrent();
					case NEWEST: return mFragment.getNewest();
					case OLDEST: return mFragment.getOlder();
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
	protected void onPostExecute(Cursor cursor) {
		if(this.isCancelled()) {
			Log.i(TAG, "Returning from cancelled task");
			return;
		}
		
		Log.i(TAG, "POST EXECUTING");
		//Check if the refresh view is showing..
		if(mFragment.getListView() != null) {
			
			try{
				PullToRefreshListView lv = 
					(PullToRefreshListView)mFragment.getListView();
				
				if(lv.isRefreshing()) {
					lv.onRefreshComplete();
				}
			} catch(ClassCastException e) {
				//ignore..
			}
		}

		if(hasError) {
			Toast.makeText(mFragment.getActivity(), "Error getting data", Toast.LENGTH_SHORT).show();
		}	
		else {			
			mFragment.setCursor(cursor);
			mFragment.appendData();
		}
	}
}