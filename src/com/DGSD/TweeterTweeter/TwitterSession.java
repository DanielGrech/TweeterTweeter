package com.DGSD.TweeterTweeter;

import java.util.HashSet;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TwitterSession {
	private SharedPreferences sharedPref;
	private Editor editor;

	private static final String TWEET_AUTH_KEY = "auth_key";
	private static final String TWEET_AUTH_SECRET_KEY = "auth_secret_key";
	private static final String TWEET_USER_NAME = "user_name";
	private static final String ACCOUNT_NAME = "account";
	private static final String SHARED = "Twitter_Preferences";

	public TwitterSession(Context context) {
		sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);

		editor = sharedPref.edit();
	}

	public HashSet<String> getAccountList(){
		HashSet<String> retval = new HashSet<String>();

		int accountNum = 1;

		String val;

		do {
			val = sharedPref.getString(ACCOUNT_NAME + Integer.toString(accountNum), null);
			if(val != null) {
				retval.add(val);
				accountNum++;
			}
		}while(val != null);

		return retval.isEmpty() ? null : retval;
	}

	public void storeAccessToken(String accountName, AccessToken accessToken, String username) {
		editor.putString(accountName + TWEET_AUTH_KEY, accessToken.getToken());
		editor.putString(accountName + TWEET_AUTH_SECRET_KEY, accessToken.getTokenSecret());
		editor.putString(accountName + TWEET_USER_NAME, username);

		editor.putString(accountName, accountName);
		
		editor.commit();
	}

	public void resetAccessToken(String accountName) {
		editor.putString(accountName + TWEET_AUTH_KEY, null);
		editor.putString(accountName + TWEET_AUTH_SECRET_KEY, null);
		editor.putString(accountName + TWEET_USER_NAME, null);

		editor.putString(accountName, null);
		
		editor.commit();
	}

	public String getUsername(String accountName) {
		return sharedPref.getString(accountName + TWEET_USER_NAME, "");
	}

	public AccessToken getAccessToken(String accountName) {
		String token 		= sharedPref.getString(accountName + TWEET_AUTH_KEY, null);
		String tokenSecret 	= sharedPref.getString(accountName + TWEET_AUTH_SECRET_KEY, null);

		if (token != null && tokenSecret != null) 
			return new AccessToken(token, tokenSecret);
		else
			return null;
	}
}