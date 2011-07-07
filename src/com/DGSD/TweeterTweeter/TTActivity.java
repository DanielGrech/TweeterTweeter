package com.DGSD.TweeterTweeter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.DGSD.TweeterTweeter.Fragments.FollowersFragment;
import com.DGSD.TweeterTweeter.Fragments.FollowingFragment;
import com.DGSD.TweeterTweeter.Fragments.HomeTimelineFragment;
import com.DGSD.TweeterTweeter.Fragments.LoginFragment;
import com.DGSD.TweeterTweeter.Fragments.MentionsListFragment;
import com.DGSD.TweeterTweeter.Fragments.RetweetsOfFragment;
import com.DGSD.TweeterTweeter.Fragments.TimelineFragment;
import com.DGSD.TweeterTweeter.Utils.Log;

public class TTActivity extends FragmentActivity {
	
	private static final String TAG = TTActivity.class.getSimpleName();
	
	private TTApplication mApplication;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		getSupportFragmentManager().beginTransaction().add(R.id.container, 
				HomeTimelineFragment.newInstance("account2")).commit();
		
		getSupportFragmentManager().beginTransaction().add(R.id.container, 
				FollowingFragment.newInstance("account2", "DanielGrech")).commit();
		
		getSupportFragmentManager().beginTransaction().add(R.id.container, 
				FollowersFragment.newInstance("account2", "DanielGrech")).commit();
	}
	
}
