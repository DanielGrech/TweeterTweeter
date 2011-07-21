package com.DGSD.TweeterTweeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.TwitterConnection.TwDialogListener;
import com.DGSD.TweeterTweeter.Utils.Log;

public class LoginActivity extends Activity{
	
	private static final String TAG = LoginActivity.class.getSimpleName();

	private Button mLoginButton;

	private TwitterConnection mTwitter;
	
	private TTApplication mApplication;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		mLoginButton = (Button) findViewById(R.id.loginButton);

		mApplication = (TTApplication) getApplication();
		
		mLoginButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mTwitter.authorize();
			}
		});
		
		mTwitter = new TwitterConnection(mApplication, this);
		
		mTwitter.setListener(mTwLoginDialogListener);
	}

	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		@Override
		public void onComplete(String value) {
			Log.i(TAG, "IVE COMPLETED!!!!! " + value);

			Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();

			startActivity(new Intent(LoginActivity.this, TTActivity.class));
			finish();
		}

		@Override
		public void onError(String value) {
			Log.i(TAG, "ERROR LOGGING IN! " + value);
			
			Toast.makeText(LoginActivity.this, "Twitter connection failed: " + value, Toast.LENGTH_LONG).show();
		}
	};

	public void clickLoginButton(){
		if(mLoginButton != null)
			mLoginButton.performClick();
	}
}
