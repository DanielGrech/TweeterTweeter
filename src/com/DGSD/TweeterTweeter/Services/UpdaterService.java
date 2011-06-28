package com.DGSD.TweeterTweeter.Services;

import java.util.HashSet;

import android.app.IntentService;
import android.content.Intent;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class UpdaterService extends IntentService {
	private static final String TAG = UpdaterService.class.getSimpleName();

	public static final int ALL_DATA = -1;

	public static final String DATA_TYPE = "data_type";
	
	public static final String ACCOUNT = "account";

	public static final String SEND_DATA = 
		"com.DGSD.TweeterTweeter.SEND_DATA";

	public static final String NO_DATA = 
		"com.DGSD.TweeterTweeter.NO_DATA";

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
		int dataType = inIntent.getIntExtra(DATA_TYPE, ALL_DATA);
		
		String requestedAccount = inIntent.getStringExtra(ACCOUNT);

		HashSet<String> accounts = mApplication.getTwitterSession().getAccountList();

		if(accounts != null) {
			for(String account : accounts) {
				if(requestedAccount != null) { 
					//Update only the requested account (or all if none requested)
					if(!requestedAccount.equals(account))
						break;
				}
				switch(dataType) {
					case ALL_DATA : //Update all data.
						updateHomeTimeline(account);
						updateMentions(account);
						updateFavourites(account);
						updateRetweetsByMe(account);
						updateRetweetsOfMe(account);
						updateFollowers(account);
						updateFollowing(account);
						updateProfileInfo(account);
						break;
					case DATATYPES.HOME_TIMELINE: 
						updateHomeTimeline(account);
						break;
					case DATATYPES.MENTIONS:
						updateMentions(account);
						break;
					case DATATYPES.FAVOURITES:
						updateFavourites(account);
						break;
					case DATATYPES.RETWEETS_BY:
						updateRetweetsByMe(account);
						break;
					case DATATYPES.RETWEETS_OF:
						updateRetweetsOfMe(account);
						break;
					case DATATYPES.FOLLOWERS:
						updateFollowers(account);
						break;
					case DATATYPES.FOLLOWING:
						updateFollowing(account);
						break;
					case DATATYPES.PROFILE_INFO:
						updateProfileInfo(account);
						break;
				}
			}
		}
	}

	protected void sendData(int type) {
		Intent intent = new Intent(SEND_DATA);
		intent.putExtra(DATA_TYPE, type);
		sendBroadcast(intent);
	}

	protected void sendNoData(int type) {
		Intent intent = new Intent(NO_DATA);
		intent.putExtra(DATA_TYPE, type);
		sendBroadcast(intent);
	}

	private void updateHomeTimeline(String account) {
		if(mApplication.fetchStatusUpdates(account) > 0) {
			sendData(DATATYPES.HOME_TIMELINE);
		} else {
			sendNoData(DATATYPES.HOME_TIMELINE);
		}
	}

	private void updateMentions(String account) {
		if(mApplication.fetchMentions(account) > 0 ) {
			sendData(DATATYPES.MENTIONS);
		} else {
			sendNoData(DATATYPES.MENTIONS);
		}
	}

	private void updateFavourites(String account) {
		if(mApplication.fetchFavourites(account) > 0) {
			sendData(DATATYPES.FAVOURITES);
		} else {
			sendNoData(DATATYPES.FAVOURITES);
		}
	}

	private void updateRetweetsByMe(String account) {
		if(mApplication.fetchRetweetsByMe(account) > 0){
			sendData(DATATYPES.RETWEETS_BY);
		} else {
			sendNoData(DATATYPES.RETWEETS_BY);
		}
	}

	private void updateRetweetsOfMe(String account) {
		if(mApplication.fetchRetweetsOfMe(account) > 0){
			sendData(DATATYPES.RETWEETS_OF);
		} else {
			sendNoData(DATATYPES.RETWEETS_OF);
		}
	}

	private void updateFollowers(String account) {
		if(mApplication.fetchFollowers(account) > 0) {
			sendData(DATATYPES.FOLLOWERS);
		} else {
			sendNoData(DATATYPES.FOLLOWERS);
		}
	}

	private void updateFollowing(String account) {
		if(mApplication.fetchFollowing(account) > 0) {
			sendData(DATATYPES.FOLLOWING);
		} else {
			sendNoData(DATATYPES.FOLLOWING);
		}
	}

	private void updateProfileInfo(String account) {
		if(mApplication.fetchProfileInfo(account) > 0) {
			sendData(DATATYPES.PROFILE_INFO);
		} else {
			sendNoData(DATATYPES.PROFILE_INFO);
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