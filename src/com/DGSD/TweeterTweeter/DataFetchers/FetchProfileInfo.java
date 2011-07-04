package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.TwitterException;
import twitter4j.User;
import android.content.ContentValues;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;

public class FetchProfileInfo extends DataFetcher {
	public FetchProfileInfo(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		User u = mTwitter.showUser(mTwitter.getId());

		if(u != null){
			ContentValues values = StatusData.createUserContentValues(account, u);

			mApplication.getStatusData().insertOrIgnore(StatusData.PROFILE_TABLE, values);
		}

		return count;
	}	
}