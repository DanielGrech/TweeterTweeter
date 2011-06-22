package com.DGSD.TweeterTweeter.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.github.droidfu.widgets.WebImageView;

public abstract class BaseStatusFragment extends BaseFragment {

	private static final String TAG = BaseStatusFragment.class.getSimpleName();
	
	protected static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER,
	    StatusData.C_TEXT, StatusData.C_IMG, StatusData.C_FAV };
	
	protected static final int[] TO = {R.id.timeline_date, R.id.timeline_source, R.id.timeline_tweet,
		R.id.timeline_profile_image, R.id.timeline_favourite_star }; 
	
	//Adjust data from database for display
	protected static final ViewBinder mViewBinder = new ViewBinder() { 

		@Override
	    public boolean setViewValue(View view, Cursor cursor, int columnIndex) { 
	    	switch(view.getId()){
	    		case R.id.timeline_date:
	    			long timestamp = -1;
	    			try{
	    				timestamp = Long.valueOf( cursor.getString(columnIndex) );
	    			}catch(NumberFormatException e){
	    				Log.e(TAG, "Error converting time string", e);
	    			}
	    			
	    			((TextView)view).setText( 
	    					DateUtils.getRelativeTimeSpanString(view.getContext(), timestamp) );
	    			
	    			return true;
	    			
	    		case R.id.timeline_favourite_star:
	    			if(cursor.getInt(columnIndex) == 1) {
	    				view.setVisibility(View.VISIBLE);
	    			}
	    			else {
	    				view.setVisibility(View.GONE);
	    			}
	    			
	    			return true;
	    		
	    		case R.id.timeline_profile_image:
	    			String url = "";
	    			url = cursor.getString(columnIndex);
	    			
	    			WebImageView wiv = (WebImageView) view;
	    			
	    			wiv.setImageUrl(url);
	    			if(url != "") {
	    				wiv.loadImage();
	    			}
	    			
	    			return true;
	    	}
			
			return false;
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
}
