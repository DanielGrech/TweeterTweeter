package com.DGSD.TweeterTweeter.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;

public abstract class BaseFragment extends DialogFragment{
	
	private static final String TAG = BaseFragment.class.getSimpleName();
	
	public abstract void setupList();
	//public abstract Cursor loadData();
	//public abstract Cursor loadNewDataItems();
	
	protected TTApplication mApplication;
	
	protected ListView mListView;
	
	protected SimpleCursorAdapter mAdapter;
	
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
				
				setupList();
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
