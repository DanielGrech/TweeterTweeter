package com.DGSD.TweeterTweeter.UI;

import twitter4j.TwitterException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.Fragments.BaseFragment;
import com.DGSD.TweeterTweeter.Utils.Log;

public class EndlessListAdapter extends EndlessAdapter {
	private static final String TAG = EndlessListAdapter.class.getSimpleName();
	
	private RotateAnimation mRotate = null;

	private ImageView mImageView = null;
	
	private BaseFragment mFragment;
	
	public EndlessListAdapter(BaseFragment fragment, SimpleCursorAdapter sca) {
		super(sca);
		
		mFragment = fragment;

		mRotate = 
			new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,0.5f, 
					Animation.RELATIVE_TO_SELF,	0.5f);
		mRotate.setDuration(600);
		mRotate.setRepeatMode(Animation.RESTART);
		mRotate.setRepeatCount(Animation.INFINITE);
		
		mImageView = new ImageView(mFragment.getActivity());
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
		mFragment.getOlder();
		return false;
	}
	
	@Override
	protected void appendCachedData() {
		Log.i(TAG, "APPENDING DATA TO LIST!");
		mFragment.appendData();
	}
	
	@Override
	protected boolean onException(View pendingView, Exception e){
		Log.i(TAG, "ERROR LOADING EXTRA DATA", e);
		
		Toast.makeText(mFragment.getActivity(), "Error loading data..", 
				Toast.LENGTH_LONG).show();
	
		return false;
	}
	
	public ListAdapter getAdapter() {
		return getWrappedAdapter();
	}
}