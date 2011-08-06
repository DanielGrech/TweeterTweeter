package com.DGSD.TweeterTweeter.DataFetchers;

import java.util.Stack;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.ContentValues;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;

public class FetchFollowing extends DataFetcher {
	public FetchFollowing(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		Stack<Long> mIds = new Stack<Long>();
		long cursor = -1;

		//Get the ids of all friends..
		IDs ids;
		do{
			ids =  mTwitter.getFriendsIDs(user, cursor);

			long[] idArray = ids.getIDs();

			for(int i = 0, size=idArray.length; i<size ;i++)
				mIds.add(idArray[i]);
		}while( (cursor = ids.getNextCursor()) != 0);

		long tempIds[] = new long[mIds.size() > 100 ? 100 : mIds.size()];
		
		ResponseList<User> users = null;
		
		int currentElement = 0;
		for(int i = 0, size = mIds.size(); i < size; i++) {
			tempIds[currentElement] = mIds.get(i);

			//This is the most the twitter API allows for the lookupUsers call
			if(i == 99) {
				currentElement = 0;
				if(users == null) {
					users = mTwitter.lookupUsers(tempIds);
				} else {
					users.addAll(mTwitter.lookupUsers(tempIds));
				}
			} else {
				currentElement++;
			}
		}
		
		ContentValues values;
		for (User u : users) {
			values = StatusData.createUserContentValues(account, user, u);

			if(mApplication.getStatusData()
					.insert(StatusData.FOLLOWING_TABLE, values)) {
				count++;
			}
		}

		return count;
	}
}