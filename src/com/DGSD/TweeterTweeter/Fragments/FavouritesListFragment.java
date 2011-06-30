package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

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
		
		mType = UpdaterService.DATATYPES.FAVOURITES;

		Log.i(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public synchronized void setupList() throws TwitterException {
		Log.i(TAG, "Setting up list");

		//We have this info in the db already..
		if(mUserName != null) {
			mCursor = mApplication.getStatusData().getFavourites(mAccountId, mUserName);
		} else {
			mCursor = mApplication.getStatusData().getFavourites(mAccountId, 
					mApplication.getTwitter(mAccountId).getScreenName());
		}
		
		if(mCursor.getCount() == 0) {
			mListView.refresh();
		}
		
		if(mAdapter == null) {
			mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
					mCursor, FROM, TO, 0);
		}

		((SimpleCursorAdapter)mAdapter).setViewBinder(mViewBinder);
	}
}
