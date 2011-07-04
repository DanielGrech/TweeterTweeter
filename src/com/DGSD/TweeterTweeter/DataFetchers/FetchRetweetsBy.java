package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Fragments.BaseFragment;

public class FetchRetweetsBy extends DataFetcher {
	public FetchRetweetsBy(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		Paging p = new Paging(1, BaseFragment.ELEMENTS_PER_PAGE);
		if(type == FETCH_NEWEST) {
			String latestTweet = 
				mApplication.getStatusData().getLatestTweetId(StatusData.RT_BY_TABLE, account);

			if(latestTweet != null) {
				p.sinceId(Long.valueOf(latestTweet));
			}
		} else {
			String oldestTweet = 
				mApplication.getStatusData().getOldestTweetId(StatusData.RT_BY_TABLE, account);
			
			if(oldestTweet != null) {
				p.maxId(Long.valueOf(oldestTweet));
			}
		}
		
		ResponseList<Status> timeline;

		if(user == null) {
			timeline = mTwitter.getRetweetedByMe(p);
		} else {
			timeline = mTwitter.getRetweetedByUser(user, p);
		}

		for (Status status : timeline) {
			//Returns true if new rows were added..
			if (mApplication.getStatusData().insert(StatusData.RT_BY_TABLE, 
					StatusData.createTimelineContentValues(account, user, status))) {
				count++;
			}
		}

		return count;
	}
}