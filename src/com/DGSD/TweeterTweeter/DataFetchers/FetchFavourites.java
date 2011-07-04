package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class FetchFavourites extends DataFetcher {
	public FetchFavourites(TTApplication app) {
		super(app);
		// TODO Auto-generated constructor stub
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		
		ResponseList<Status> timeline;

		if(user == null) {
			timeline = mTwitter.getFavorites();
		} else {
			timeline = mTwitter.getFavorites(user);
		}

		for (Status status : timeline) {
			//Returns true if new rows were added..
			if (mApplication.getStatusData().insert(StatusData.FAVOURITES_TABLE, 
					StatusData.createTimelineContentValues(account, user, status))) {
				Log.i(TAG, "INSERTING NEW FAVOURITE!");
				count++;
			}
		}

		return count;
	}
}