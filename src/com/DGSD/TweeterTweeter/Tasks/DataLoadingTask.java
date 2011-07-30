package com.DGSD.TweeterTweeter.Tasks;

import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.BaseFragment;
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
			if(mFragment.getEndlessAdapter() != null) {
				synchronized(mFragment.getEndlessAdapter()) {
					switch(mType) {
						case CURRENT: 
							return mFragment.getCurrent();
						case NEWEST: 
							mFragment.getNewest();
							return mFragment.getCurrent();
						case OLDEST: 
							mFragment.getOlder();
							return mFragment.getCurrent();
					}
				}
			} else {
				switch(mType) {
					case CURRENT: 
						return mFragment.getCurrent();
					case NEWEST: 
						if(mFragment.getNewest()) {
							return mFragment.getCurrent();
						} else {
							return null;
						}
					case OLDEST: 
						if(mFragment.getOlder()) {
							return mFragment.getCurrent();
						} else {
							return null;
						}
				}
			}
			
			return null;
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
		
		if(hasError) {
			Toast.makeText(mFragment.getActivity(), "Error getting data", 
					Toast.LENGTH_SHORT).show();
		}	
		else {
			switch(mType) {
				case NEWEST:
					mFragment.attachData(cursor);
					break;
				case OLDEST:
					mFragment.attachData(cursor);
					break;
				case CURRENT:
					Log.i(TAG, "Changing cursor from DataLoadingTask");
					mFragment.changeCursor(cursor);
					break;
			}
		}
	}
}