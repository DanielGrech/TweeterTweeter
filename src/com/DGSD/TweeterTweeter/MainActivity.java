package com.DGSD.TweeterTweeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.DGSD.TweeterTweeter.Utils.Log;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TTApplication app = (TTApplication) getApplication();
		
		Intent startIntent = null;
		
		if(app.getTwitterSession().getAccountList() == null) {
			//Need to log in!
			Log.i(TAG, "Need to log in!");
			
			startIntent = new Intent(this, LoginActivity.class);
			
		} else {
			//Already logged in, lets restore the session!
			Log.i(TAG, "Already logged in!");
			
			startIntent = new Intent(this, TTActivity.class);
		}
		
		startActivity(startIntent);
		finish();
	}
	
}
