package com.DGSD.TweeterTweeter.Tasks;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class AddFriendTask extends TwitterTask {
	private static final String TAG = AddFriendTask.class.getSimpleName();
	
	public static final int ADD_USER = 0;
	
	public static final int REMOVE_USER = 1;

	private String mScreenName;
	
	private int mFlag;
	
	public AddFriendTask(TTApplication app, String screenName, int flag) {
		super(app);
		
		mScreenName = screenName;
		
		mFlag = flag;
	}
		
	@Override
	protected void preExecute() {
		
	}

	@Override
	protected void doTask(Twitter twitter) throws TwitterException {
		if(mFlag == ADD_USER) {
			Log.i(TAG, "Creating friendship");
			twitter.createFriendship(mScreenName);
		} else {
			Log.i(TAG, "Detroying friendship");
			twitter.destroyFriendship(mScreenName);
		}
	}

	@Override
	protected void onComplete() {
		Toast.makeText(mApplication, mScreenName + (mFlag == ADD_USER ? " added" : " removed"), 
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onError() {
		Toast.makeText(mApplication, 
				"Error " + (mFlag == ADD_USER ? " adding " : " removing ") + mScreenName, 
				Toast.LENGTH_LONG).show();
	}
}
