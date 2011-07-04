package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Fragments.BaseFragment;

public class FetchTimeline extends DataFetcher {
	public FetchTimeline(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		Paging p = new Paging(1, BaseFragment.ELEMENTS_PER_PAGE);
		if(type == FETCH_NEWEST) {
			String latestTweet = 
				mApplication.getStatusData().getLatestTweetId(StatusData.TIMELINE_TABLE, account);

			if(latestTweet != null) {
				p.sinceId(Long.valueOf(latestTweet));
			}
		} else {
			String oldestTweet = 
				mApplication.getStatusData().getOldestTweetId(StatusData.TIMELINE_TABLE, account);
			
			if(oldestTweet != null) {
				p.maxId(Long.valueOf(oldestTweet));
			}
		}
		
		ResponseList<Status> timeline = mTwitter.getUserTimeline(user, p);

		for (Status status : timeline) {
			if (mApplication.getStatusData().insert(StatusData.TIMELINE_TABLE, 
					StatusData.createTimelineContentValues(account, user, status))) {
				count++;
			}
		}

		return count;
	}
}