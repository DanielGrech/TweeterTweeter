package com.DGSD.TweeterTweeter.Tasks;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class TwitterTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = AddFriendTask.class.getSimpleName();
	
	protected abstract void preExecute();
	
	protected abstract void doTask(Twitter twitter) throws TwitterException;
	
	protected abstract void onComplete();
	
	protected abstract void onError();

	protected String mCurrentAccount;

	protected TTApplication mApplication;

	private boolean hasError;

	public TwitterTask(TTApplication app) {	
		mApplication = app;

		mCurrentAccount = mApplication.getSelectedAccount();
		
		hasError = false;
	}

	@Override
	public void onPreExecute() {
		//TODO: Need to show some sort of progressbar here!
		preExecute();
	}

	@Override
	protected Void doInBackground(Void... params) {
		try{
			final Twitter twitter = mApplication.getTwitter(mCurrentAccount);
			doTask(twitter);
		} catch(TwitterException e) {
			Log.e(TAG, "Error in friend task", e);
			hasError = true;
		}


		return null;
	}

	@Override
	public void onPostExecute(Void arg) {
		if(hasError) {
			onError();
		} else {
			onComplete();
		}
	}
}
