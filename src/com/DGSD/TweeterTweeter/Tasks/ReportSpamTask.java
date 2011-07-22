package com.DGSD.TweeterTweeter.Tasks;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class ReportSpamTask extends TwitterTask {
	private static final String TAG = ReportSpamTask.class.getSimpleName();

	private String mScreenName;
	
	public ReportSpamTask(TTApplication app, String screenName) {
		super(app);
		
		mScreenName = screenName;
	}
		
	@Override
	protected void preExecute() {
		
	}

	@Override
	protected void doTask(Twitter twitter) throws TwitterException {
		Log.i(TAG, "Reporting user: " + mScreenName);
		twitter.reportSpam(mScreenName);
	}

	@Override
	protected void onComplete() {
		Toast.makeText(mApplication, mScreenName + " blocked & reported for spam", 
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onError() {
		Toast.makeText(mApplication, "Error reporting " + mScreenName, 
				Toast.LENGTH_LONG).show();
	}
}
