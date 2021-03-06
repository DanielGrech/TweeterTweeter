package com.DGSD.TweeterTweeter.DataFetchers;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Fragments.BaseFragment;

public class FetchMentions extends DataFetcher {
	public FetchMentions(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String user, int type) throws TwitterException {
		Paging p = new Paging(1, BaseFragment.ELEMENTS_PER_PAGE);
		if(type == FETCH_NEWEST) {
			String latestTweet = 
				mApplication.getStatusData().getLatestTweetId(StatusData.MENTIONS_TABLE, account);

			if(latestTweet != null) {
				p.sinceId(Long.valueOf(latestTweet));
			}
		} else {
			String oldestTweet = 
				mApplication.getStatusData().getOldestTweetId(StatusData.MENTIONS_TABLE, account);
			
			if(oldestTweet != null) {
				p.maxId(Long.valueOf(oldestTweet));
			}
		}
		
		ResponseList<Status> timeline = mTwitter.getMentions(p);

		for (Status status : timeline) {
			//Returns true if new rows were added..
			if (mApplication.getStatusData().insert(StatusData.MENTIONS_TABLE, 
					StatusData.createTimelineContentValues(account, user, status))) {
				count++;
			}
		}

		return count;
	}
}
