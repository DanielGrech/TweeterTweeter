package com.DGSD.TweeterTweeter.DataFetchers;

import java.util.ArrayList;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.ContentValues;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;

public class FetchFollowers extends DataFetcher {
	public FetchFollowers(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		ArrayList<Long> mIds = new ArrayList<Long>();
		long cursor = -1;

		//Get the ids of all followers..
		IDs ids;
		do{
			ids =  mTwitter.getFollowersIDs(user, cursor);

			long[] idArray = ids.getIDs();

			for(int i = 0, size=idArray.length; i<size ;i++)
				mIds.add(idArray[i]);
		}while( (cursor = ids.getNextCursor()) != 0);

		long tempIds[] = new long[mIds.size()];
		for(int i = 0, size = mIds.size(); i < size; i++)
			tempIds[i] = mIds.get(i);

		ResponseList<User> users = mTwitter.lookupUsers(tempIds);

		ContentValues values;
		for (User u : users) {
			values = StatusData.createUserContentValues(account, user, u);

			if(mApplication.getStatusData()
					.insert(StatusData.FOLLOWERS_TABLE, values)) {
				count++;
			}
		}

		return count;
	}
}