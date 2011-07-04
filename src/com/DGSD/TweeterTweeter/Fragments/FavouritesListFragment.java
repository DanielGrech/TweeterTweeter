package com.DGSD.TweeterTweeter.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.DGSD.TweeterTweeter.Utils.DataFetchers.DataFetcher;

public class FavouritesListFragment extends BaseStatusFragment {

	private static final String TAG = FavouritesListFragment.class.getSimpleName();

	public static FavouritesListFragment newInstance(String accountId, String user){
		FavouritesListFragment f = new FavouritesListFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();

		args.putString("accountId", accountId);

		args.putString("username", user);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mAccountId = getArguments().getString("accountId");

		mUserName = getArguments().getString("username");
		
		if(mUserName == null) {
			mUserName = mApplication.getTwitterSession().getUsername(mAccountId);
		}
		
		mType = UpdaterService.DATATYPES.FAVOURITES;

		Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public synchronized void getCurrent() {
		Log.i(TAG, "Getting current");
		mCursor = mApplication.getStatusData().getFavourites(mAccountId, mUserName, null);
	
	}

	@Override
	public synchronized void getNewest() {
		Log.i(TAG, "Getting newest");
		
		mApplication.fetchFavourites(mAccountId, mUserName, 
				DataFetcher.FETCH_NEWEST);
		
		getCurrent();
	}
	
	@Override
	public synchronized void getOlder() {
		Log.i(TAG, "Getting oldest");
		
		mApplication.fetchFavourites(mAccountId, mUserName, 
				DataFetcher.FETCH_OLDER);
		
		getCurrent();
	}
}
