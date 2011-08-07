package com.DGSD.TweeterTweeter.DataFetchers;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;

public class FetchSearch extends DataFetcher {
	public FetchSearch(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String query, int type) throws TwitterException {
		/*Paging p = new Paging(1, BaseFragment.ELEMENTS_PER_PAGE);
		if(type == FETCH_NEWEST) {
			String latestTweet = 
				mApplication.getStatusData().getLatestTweetId(StatusData.HOME_TIMELINE_TABLE, account);

			if(latestTweet != null) {
				p.sinceId(Long.valueOf(latestTweet));
			}
		} else {
			String oldestTweet = 
				mApplication.getStatusData().getOldestTweetId(StatusData.HOME_TIMELINE_TABLE, account);
			
			if(oldestTweet != null) {
				p.maxId(Long.valueOf(oldestTweet));
			}
		}*/

		Query mQuery = new Query(query);
		QueryResult results= mTwitter.search(mQuery);
		
		List<Tweet> tweets = results.getTweets();

		for (Tweet t : tweets) {
			
			if (mApplication.getStatusData().insert(StatusData.TEMP_SEARCH_TABLE, 
					StatusData.createSearchContentValues(account, t))) {
				count++;
			}
		}

		return count;
	}
}