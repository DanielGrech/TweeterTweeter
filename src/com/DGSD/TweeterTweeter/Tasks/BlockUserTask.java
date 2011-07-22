package com.DGSD.TweeterTweeter.Tasks;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class BlockUserTask extends TwitterTask {
	private static final String TAG = BlockUserTask.class.getSimpleName();
	
	public static final int BLOCK_USER = 0;
	
	public static final int UNBLOCK_USER = 1;

	private String mScreenName;
	
	private int mFlag;
	
	public BlockUserTask(TTApplication app, String screenName, int flag) {
		super(app);
		
		mScreenName = screenName;
		
		mFlag = flag;
	}
		
	@Override
	protected void preExecute() {
		
	}

	@Override
	protected void doTask(Twitter twitter) throws TwitterException {
		if(mFlag == BLOCK_USER) {
			Log.i(TAG, "Creating friendship");
			twitter.createBlock(mScreenName);
		} else {
			Log.i(TAG, "Detroying friendship");
			twitter.destroyBlock(mScreenName);
		}
	}

	@Override
	protected void onComplete() {
		Toast.makeText(mApplication, mScreenName + (mFlag == BLOCK_USER ? " blocked" : " unblocked"), 
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onError() {
		Toast.makeText(mApplication, "Error " +
				(mFlag == BLOCK_USER ? " blocking " : " unblocking ") + mScreenName, 
				Toast.LENGTH_LONG).show();
	}
}
