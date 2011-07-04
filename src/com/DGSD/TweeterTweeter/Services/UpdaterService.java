package com.DGSD.TweeterTweeter.Services;

import java.util.HashSet;

import twitter4j.TwitterException;
import android.app.IntentService;
import android.content.Intent;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.DataFetchers.DataFetcher;
import com.DGSD.TweeterTweeter.Utils.Log;

public class UpdaterService extends IntentService {
	private static final String TAG = UpdaterService.class.getSimpleName();

	public static final String DATA_TYPE = "data_type";

	public static final String ACCOUNT = "account";

	public static final String USER = "user";

	public static final String PAGE = "page";

	public static final String TWEETID = "tweet_id";
	
	public static final String SEND_DATA = 
		"com.DGSD.TweeterTweeter.SEND_DATA";

	public static final String NO_DATA = 
		"com.DGSD.TweeterTweeter.NO_DATA";

	public static final String ERROR = 
		"com.DGSD.TweeterTweeter.ERROR";

	private TTApplication mApplication;

	public UpdaterService() {
		super(TAG);
		Log.d(TAG, "UpdaterService constructed");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.mApplication = (TTApplication) getApplication(); 
		Log.d(TAG, "onCreated");
	}

	@Override
	protected void onHandleIntent(Intent inIntent) {
		int dataType = inIntent.getIntExtra(DATA_TYPE, DATATYPES.ALL_DATA);

		String requestedAccount = inIntent.getStringExtra(ACCOUNT);

		String requestedUser = inIntent.getStringExtra(USER);

		String tweetId = inIntent.getStringExtra(TWEETID);

		boolean hasRequestedUser = true;
		if(requestedUser == null) {
			hasRequestedUser = false;
		}

		Log.i(TAG, "REQUESTED USER: " + requestedUser);

		HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();

		if(accounts != null) {
			for(String account : accounts) {
				if(requestedAccount != null) { 
					//Update only the requested account (or all if none requested)
					if(!requestedAccount.equals(account))
						break;
				}

				if(!hasRequestedUser) {
					//If no other user specified, get data for our own accounts
					try{
						requestedUser = 
							mApplication.getTwitter(account).getScreenName();
					} catch(TwitterException e) {
						requestedUser = null;
					}
				}

				switch(dataType) {
					case DATATYPES.ALL_DATA : //Update all data.
						updateHomeTimeline(account);
						updateMentions(account);
						updateFavourites(account, requestedUser);
						updateRetweetsBy(account, requestedUser);
						updateRetweetsOf(account);
						updateFollowers(account, requestedUser);
						updateFollowing(account, requestedUser);
						updateProfileInfo(account, requestedUser);
						try{
							mApplication.updateCurrentFavourites(account);
						}catch(TwitterException e) {
							Log.e(TAG, "O NO!", e);
						}
						break;
					case DATATYPES.HOME_TIMELINE: 
						updateHomeTimeline(account);
						break;
					case DATATYPES.MENTIONS:
						updateMentions(account);
						break;
					case DATATYPES.FAVOURITES:
						updateFavourites(account, requestedUser);
						break;
					case DATATYPES.RETWEETS_BY:
						updateRetweetsBy(account, requestedUser);
						break;
					case DATATYPES.RETWEETS_OF:
						updateRetweetsOf(account);
						break;
					case DATATYPES.FOLLOWERS:
						updateFollowers(account, requestedUser);
						break;
					case DATATYPES.FOLLOWING:
						updateFollowing(account, requestedUser);
						break;
					case DATATYPES.PROFILE_INFO:
						updateProfileInfo(account, requestedUser);
						break;
					case DATATYPES.NEW_FAVOURITE:
						try{
    						mApplication.addNewFavourite(account, tweetId);
    						mApplication.updateCurrentFavourites(account);
						} catch(TwitterException e) {
							Log.e(TAG, "O NO!", e);
						}
						break;
				}
			}
		}
	}

	protected void sendData(int type, String account, String user) {
		Intent intent = new Intent(SEND_DATA);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		sendBroadcast(intent);
	}

	protected void sendNoData(int type, String account, String user) {
		Intent intent = new Intent(NO_DATA);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		sendBroadcast(intent);
	}

	protected void sendError(int type, String account, String user) {
		Intent intent = new Intent(ERROR);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		sendBroadcast(intent);
	}

	private void updateHomeTimeline(String account) {
		switch(mApplication.fetchStatusUpdates(account, null, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.HOME_TIMELINE, account, null);
				break;
			case 0:
				sendNoData(DATATYPES.HOME_TIMELINE, account, null);
				break;
			default:
				sendData(DATATYPES.HOME_TIMELINE, account, null);
				break;
		}
	}

	private void updateMentions(String account) {
		switch(mApplication.fetchMentions(account, null, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.MENTIONS, account, null);
				break;
			case 0:
				sendNoData(DATATYPES.MENTIONS, account, null);
				break;
			default:
				sendData(DATATYPES.MENTIONS, account, null);
				break;
		}
	}

	private void updateFavourites(String account, String user) {
		switch(mApplication.fetchFavourites(account, user, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.FAVOURITES, account, user);
				break;
			case 0:
				sendNoData(DATATYPES.FAVOURITES, account, user);
				break;
			default:
				sendData(DATATYPES.FAVOURITES, account, user);
				break;
		}
	}

	private void updateRetweetsBy(String account, String user) {
		switch(mApplication.fetchRetweetsBy(account, user, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.RETWEETS_BY, account, user);
				break;
			case 0:
				sendNoData(DATATYPES.RETWEETS_BY, account, user);
				break;
			default:
				sendData(DATATYPES.RETWEETS_BY, account, user);
				break;
		}
	}

	private void updateRetweetsOf(String account) {
		switch(mApplication.fetchRetweetsOf(account, null, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.RETWEETS_OF, account, null);
				break;
			case 0:
				sendNoData(DATATYPES.RETWEETS_OF, account, null);
				break;
			default:
				sendData(DATATYPES.RETWEETS_OF, account, null);
				break;
		}
	}

	private void updateFollowers(String account, String user) {
		switch(mApplication.fetchFollowers(account, user, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.FOLLOWERS, account, user);
				break;
			case 0:
				sendNoData(DATATYPES.FOLLOWERS, account, user);
				break;
			default:
				sendData(DATATYPES.FOLLOWERS, account, user);
				break;
		}
	}

	private void updateFollowing(String account, String user) {
		switch(mApplication.fetchFollowing(account, user, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.FOLLOWING, account, user);
				break;
			case 0:
				sendNoData(DATATYPES.FOLLOWING, account, user);
				break;
			default:
				sendData(DATATYPES.FOLLOWING, account, user);
				break;
		}
	}

	private void updateProfileInfo(String account, String user) {
		switch(mApplication.fetchProfileInfo(account, user, 
				DataFetcher.FETCH_NEWEST)) {
			case -1:
				sendError(DATATYPES.PROFILE_INFO, account, user);
				break;
			case 0:
				sendNoData(DATATYPES.PROFILE_INFO, account, user);
				break;
			default:
				sendData(DATATYPES.PROFILE_INFO, account, user);
				break;
		}
	}

	public static final class DATATYPES {
		public static final int ALL_DATA = -1;
		public static final int HOME_TIMELINE = 0;
		public static final int FAVOURITES = 1;
		public static final int MENTIONS = 2;
		public static final int RETWEETS_BY = 3;
		public static final int RETWEETS_OF = 4;
		public static final int TIMELINE = 5;
		public static final int FOLLOWERS = 6;
		public static final int FOLLOWING = 7;
		public static final int PROFILE_INFO = 8;
		public static final int NEW_FAVOURITE = 9;
	}
}