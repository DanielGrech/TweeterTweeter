package com.DGSD.TweeterTweeter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.DGSD.TweeterTweeter.Utils.Log;
import android.view.Window;

public class TwitterConnection {
	private Twitter mTwitter;
	
	private Context mContext;
	
	private TTApplication mApplication;
	
	private TwitterSession mSession;
	
	private AccessToken mAccessToken;
	
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	
	private OAuthProvider mHttpOauthprovider;
	
	private ProgressDialog mProgressDlg;
	
	private TwDialogListener mListener;

	public static final String CALLBACK_URL = "twitterapp://connect";
	
	private static final String TAG = "TwitterApp";

	private String mAccountId;
	
	public TwitterConnection(TTApplication app, Context context) {

		mApplication = app;
		
		mContext = context;
		
		mTwitter = new TwitterFactory().getInstance(); 
		
		mSession = mApplication.getTwitterSession();
		
		mProgressDlg = new ProgressDialog(mContext);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(TTApplication.CONSUMER_KEY, 
				TTApplication.CONSUMER_SECRET);
		
		mHttpOauthprovider = new DefaultOAuthProvider("http://twitter.com/oauth/request_token",
				"http://twitter.com/oauth/access_token",
				"http://twitter.com/oauth/authorize");

		Set<String> mAccountList = mSession.getAccountList();
		
		mAccountId = "account" + (mAccountList == null ? 1 : mAccountList.size() + 1);
		
		mAccessToken = mSession.getAccessToken(mAccountId);

		configureToken();
	}

	public void setListener(TwDialogListener listener) {
		mListener = listener;
	}

	private void configureToken() {
		if (mAccessToken != null) {
			try{
				mTwitter.setOAuthConsumer(TTApplication.CONSUMER_KEY, TTApplication.CONSUMER_SECRET);
			}catch(IllegalStateException e){
				e.printStackTrace();
			}
			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken(mAccountId);

			mAccessToken = null;
		}
	}

//	public String getAccount(String account){
//		return mSession.getAccount();
//	}

	public String getUsername(String account) {
		return mSession.getUsername(account);
	}

	public Twitter getObject(){
		return mTwitter;
	}

	public void updateStatus(String status) throws Exception {
		try {
			mTwitter.updateStatus(status);
		} catch (TwitterException e) {
			throw e;
		}
	}

	public void authorize() {
		mProgressDlg.setMessage("Initializing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				int what = 1;

				try {
					authUrl = mHttpOauthprovider.retrieveRequestToken(mHttpOauthConsumer, CALLBACK_URL);	

					what = 0;

					Log.d(TAG, "Request token url " + authUrl);
				} catch (Exception e) {
					Log.d(TAG, "Failed to get request token");

					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0, authUrl));
			}
		}.start();
	}

	public void processToken(String callbackUrl)  {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();

		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				int what = 1;

				try {
					mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer, verifier);

					mAccessToken = new AccessToken(mHttpOauthConsumer.getToken(), mHttpOauthConsumer.getTokenSecret());

					configureToken();

					User user = mTwitter.verifyCredentials();

					mSession.storeAccessToken(mAccountId, mAccessToken, user.getScreenName());

					what = 0;
				} catch (Exception e){
					Log.d(TAG, "Error getting access token");

					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}

	private String getVerifier(String callbackUrl) {
		String verifier	 = "";

		try {
			callbackUrl = callbackUrl.replace("twitterapp", "http");

			URL url 		= new URL(callbackUrl);
			String query 	= url.getQuery();

			String array[]	= query.split("&");

			for (String parameter : array) {
				String v[] = parameter.split("=");

				if (URLDecoder.decode(v[0]).equals(oauth.signpost.OAuth.OAUTH_VERIFIER)) {
					verifier = URLDecoder.decode(v[1]);
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return verifier;
	}

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {
			@Override
			public void onComplete(String value) {
				processToken(value);
			}

			@Override
			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(mContext, url, listener).show();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDlg.dismiss();

			if (msg.what == 1) {
				if (msg.arg1 == 1)
					mListener.onError("Error getting request token");
				else
					mListener.onError("Error getting access token");
			} else {
				if (msg.arg1 == 1)
					showLoginDialog((String) msg.obj);
				else
					mListener.onComplete("");
			}
		}
	};

	public interface TwDialogListener {
		public void onComplete(String value);		

		public void onError(String value);
	}
}