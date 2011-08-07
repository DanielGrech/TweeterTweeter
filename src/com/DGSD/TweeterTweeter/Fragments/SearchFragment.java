package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.database.Cursor;
import android.os.Bundle;

import com.DGSD.TweeterTweeter.DataFetchers.DataFetcher;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

public class SearchFragment extends BaseStatusFragment {

	private static final String TAG = SearchFragment.class.getSimpleName();
	
	private String mQuery;
	
	public static SearchFragment newInstance(String accountId, String query){
		SearchFragment f = new SearchFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putString("account", accountId);
		args.putString("query", query);

		f.setArguments(args);

		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mQuery = getArguments().getString("query");
		
		mAccountId = getArguments().getString("account");
	}
	
	@Override
	public boolean getNewest() throws TwitterException {
		Log.i(TAG, "Getting newest search results");
		
		if(mApplication.fetchSearchResults(mAccountId, mQuery, 
				DataFetcher.FETCH_NEWEST) > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cursor getCurrent() throws TwitterException {
		Log.i(TAG, "Getting current search results");
		return mApplication.getStatusData().getSearchResults(mAccountId, null);
	}

	@Override
	public boolean getOlder() throws TwitterException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int getType() {
		return UpdaterService.DATATYPES.SEARCH;
	}

}
