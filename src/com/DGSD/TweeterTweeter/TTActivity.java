package com.DGSD.TweeterTweeter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.DGSD.TweeterTweeter.Utils.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.DGSD.TweeterTweeter.Fragments.HomeTimelineFragment;
import com.DGSD.TweeterTweeter.Fragments.LoginFragment;
import com.DGSD.TweeterTweeter.Services.UpdaterService;

public class TTActivity extends FragmentActivity {
	
	private static final String TAG = TTActivity.class.getSimpleName();
	
	private TTApplication mApplication;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//TODO: This should be part of the theme!
		// Hide the Title Bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);
		
		mApplication = (TTApplication)getApplication();
		
		if(mApplication.getTwitterSession().getAccountList() == null) {
			//Need to log in!
			Log.i(TAG, "Need to log in..");
			
			getSupportFragmentManager().beginTransaction()
                        		       .replace(R.id.container, new LoginFragment())
                        		       .commit();
		}
		else {
			Log.i(TAG, "Already logged in!");
			
			if( mApplication.isHoneycombTablet() ) {
				setupTablet();
			}
			else{
				setupPhone();
			}
		}
	}
	
	public void setupTablet() {
		
	}
	
	public void setupPhone() {
		final Button b = new Button(this);
		if(mApplication.isServiceRunning())
			b.setText("Stop");
		else
			b.setText("Start");
		
		b.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mApplication.isServiceRunning()) {
					stopService(new Intent(TTActivity.this, UpdaterService.class));
					b.setText("Start");
				}else {
					startService(new Intent(TTActivity.this, UpdaterService.class));
					b.setText("Stop");
					
				}
				
				
			}
		});
		
		getSupportFragmentManager().beginTransaction().replace(R.id.container, 
				HomeTimelineFragment.newInstance("account2")).commit();
		((ViewGroup)findViewById(R.id.container)).addView(b);
	}
	
}
