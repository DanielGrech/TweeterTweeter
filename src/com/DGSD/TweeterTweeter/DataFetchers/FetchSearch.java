package com.DGSD.TweeterTweeter.DataFetchers;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.TwitterException;

import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;

public class FetchSearch extends DataFetcher {
	private static String mLastQuery = null;
	
	private static int mPageCounter = 1;
	
	public FetchSearch(TTApplication app) {
		super(app);
	}

	public int fetchData(String account, String query, int type) throws TwitterException {
		int pageToUse;

		if(mLastQuery == null) {
			mLastQuery = query;
		}
		
		if(mLastQuery != query) {
			//We have a new search; reset all counters;
			mLastQuery = query;
			mPageCounter = 1;
			pageToUse = 1;
		} else {
			pageToUse = (type == FETCH_NEWEST) ? 1 : ++mPageCounter; 
		}
		
		System.err.println("MPAGECOUNTER = " + mPageCounter + " mLASTQUERY = " + mLastQuery + " TO USE: " + pageToUse);
		
		Query mQuery = new Query(query);
		mQuery.setPage(pageToUse);
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