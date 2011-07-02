package com.DGSD.TweeterTweeter.Services;

import java.util.HashSet;

import twitter4j.TwitterException;
import android.app.IntentService;
import android.content.Intent;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class UpdaterService extends IntentService {
	private static final String TAG = UpdaterService.class.getSimpleName();

	public static final String DATA_TYPE = "data_type";

	public static final String ACCOUNT = "account";

	public static final String USER = "user";

	public static final String PAGE = "page";

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

		int page = inIntent.getIntExtra(PAGE, 1);

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
						updateHomeTimeline(account, page);
						updateMentions(account, page);
						updateFavourites(account, requestedUser, page);
						updateRetweetsBy(account, requestedUser, page);
						updateRetweetsOf(account, page);
						updateFollowers(account, requestedUser, page);
						updateFollowing(account, requestedUser, page);
						updateProfileInfo(account, requestedUser, page);
						break;
					case DATATYPES.HOME_TIMELINE: 
						updateHomeTimeline(account, page);
						break;
					case DATATYPES.MENTIONS:
						updateMentions(account, page);
						break;
					case DATATYPES.FAVOURITES:
						updateFavourites(account, requestedUser, page);
						break;
					case DATATYPES.RETWEETS_BY:
						updateRetweetsBy(account, requestedUser, page);
						break;
					case DATATYPES.RETWEETS_OF:
						updateRetweetsOf(account, page);
						break;
					case DATATYPES.FOLLOWERS:
						updateFollowers(account, requestedUser, page);
						break;
					case DATATYPES.FOLLOWING:
						updateFollowing(account, requestedUser, page);
						break;
					case DATATYPES.PROFILE_INFO:
						updateProfileInfo(account, requestedUser, page);
						break;
				}
			}
		}
	}

	protected void sendData(int type, String account, String user, int page) {
		Intent intent = new Intent(SEND_DATA);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		intent.putExtra(PAGE, page);
		sendBroadcast(intent);
	}

	protected void sendNoData(int type, String account, String user, int page) {
		Intent intent = new Intent(NO_DATA);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		intent.putExtra(PAGE, page);
		sendBroadcast(intent);
	}

	protected void sendError(int type, String account, String user, int page) {
		Intent intent = new Intent(ERROR);
		intent.putExtra(DATA_TYPE, type);
		intent.putExtra(ACCOUNT, account);
		intent.putExtra(USER, user);
		intent.putExtra(PAGE, page);
		sendBroadcast(intent);
	}

	private void updateHomeTimeline(String account, int page) {
		switch(mApplication.fetchStatusUpdates(account, null, page)) {
			case -1:
				sendError(DATATYPES.HOME_TIMELINE, account, null, page);
				break;
			case 0:
				sendNoData(DATATYPES.HOME_TIMELINE, account, null, page);
				break;
			default:
				sendData(DATATYPES.HOME_TIMELINE, account, null, page);
				break;
		}
	}

	private void updateMentions(String account, int page) {
		switch(mApplication.fetchMentions(account, null, page)) {
			case -1:
				sendError(DATATYPES.MENTIONS, account, null, page);
				break;
			case 0:
				sendNoData(DATATYPES.MENTIONS, account, null, page);
				break;
			default:
				sendData(DATATYPES.MENTIONS, account, null, page);
				break;
		}
	}

	private void updateFavourites(String account, String user, int page) {
		switch(mApplication.fetchFavourites(account, user, page)) {
			case -1:
				sendError(DATATYPES.FAVOURITES, account, user, page);
				break;
			case 0:
				sendNoData(DATATYPES.FAVOURITES, account, user, page);
				break;
			default:
				sendData(DATATYPES.FAVOURITES, account, user, page);
				break;
		}
	}

	private void updateRetweetsBy(String account, String user, int page) {
		switch(mApplication.fetchRetweetsBy(account, user, page)) {
			case -1:
				sendError(DATATYPES.RETWEETS_BY, account, user, page);
				break;
			case 0:
				sendNoData(DATATYPES.RETWEETS_BY, account, user, page);
				break;
			default:
				sendData(DATATYPES.RETWEETS_BY, account, user, page);
				break;
		}
	}

	private void updateRetweetsOf(String account, int page) {
		switch(mApplication.fetchRetweetsOf(account, null, page)) {
			case -1:
				sendError(DATATYPES.RETWEETS_OF, account, null, page);
				break;
			case 0:
				sendNoData(DATATYPES.RETWEETS_OF, account, null, page);
				break;
			default:
				sendData(DATATYPES.RETWEETS_OF, account, null, page);
				break;
		}
	}

	private void updateFollowers(String account, String user, int page) {
		switch(mApplication.fetchFollowers(account, user, page)) {
			case -1:
				sendError(DATATYPES.FOLLOWERS, account, user, page);
				break;
			case 0:
				sendNoData(DATATYPES.FOLLOWERS, account, user, page);
				break;
			default:
				sendData(DATATYPES.FOLLOWERS, account, user, page);
				break;
		}
	}

	private void updateFollowing(String account, String user, int page) {
		switch(mApplication.fetchFollowing(account, user, page)) {
			case -1:
				sendError(DATATYPES.FOLLOWING, account, user, page);
				break;
			case 0:
				sendNoData(DATATYPES.FOLLOWING, account, user, page);
				break;
			default:
				sendData(DATATYPES.FOLLOWING, account, user, page);
				break;
		}
	}

	private void updateProfileInfo(String account, String user, int page) {
		switch(mApplication.fetchProfileInfo(account, user, page)) {
			case -1:
				sendError(DATATYPES.PROFILE_INFO, account, user, page);
				break;
			case 0:
				sendNoData(DATATYPES.PROFILE_INFO, account, user, page);
				break;
			default:
				sendData(DATATYPES.PROFILE_INFO, account, user, page);
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
	}
}