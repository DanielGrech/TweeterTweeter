package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;

public class FavouritesListFragment extends BaseStatusFragment {

	private static final String TAG = FavouritesListFragment.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void setupList() {
		Log.i(TAG, "Setting up list");
		
		Cursor cursor = mApplication.getStatusData().getFavourites();
		getActivity().startManagingCursor(cursor);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
				cursor, FROM, TO, 0);
		
		mAdapter.setViewBinder(mViewBinder);
	}

}
