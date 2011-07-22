package com.DGSD.TweeterTweeter.Services;

import twitter4j.GeoLocation;
import twitter4j.TwitterException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTActivity;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class NewStatusService extends Service{
	
	private static final String TAG = NewStatusService.class.getSimpleName();
	
	public static final String TWEET_ACCOUNT = "account";
	
	public static final String TWEET_TEXT = "text";
	
	public static final String TWEET_LAT = "latitude";
	
	public static final String TWEET_LONG = "longitude";
	
	private static final int TICKER_NOTIFICATION = 0;
	
	private static final int RETRY_NOTIFICATION = 1;
	
	private static final  String ns = Context.NOTIFICATION_SERVICE;
	
	private TTApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private String mAccountId = null;
	
	private String mTweetText = null;
	
	private long mLatitude = -1;
	
	private long mLongitude = -1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		mApplication = (TTApplication) getApplication();
		
		Log.d(TAG, "onCreated");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");
		
		mAccountId = intent.getCharSequenceExtra(TWEET_ACCOUNT).toString();
		
		mTweetText = intent.getCharSequenceExtra(TWEET_TEXT).toString();
		
		mLatitude  = intent.getLongExtra(TWEET_LAT, -1);
		
		mLongitude = intent.getLongExtra(TWEET_LONG, -1);
		
		//Make sure any previous warnings are gone
		mNotificationManager.cancel(RETRY_NOTIFICATION);
		
		showTickerText("Sending tweet", "Contacting twitter service");
		
		new StatusUpdater().start();
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "onDestroyed");
	}
	
	private class StatusUpdater extends Thread {
		public StatusUpdater() {
			super("UpdaterService-StatusUpdater");
		}

		@Override
		public void run() {
			GeoLocation location = null;
			if(mLatitude != -1 && mLongitude != -1) {
				location = new GeoLocation(mLatitude, mLatitude);
			}
			
			try {
				mApplication.updateStatus(mAccountId, mTweetText, location);
				
				showTickerText("Tweet Sent!", "Your tweet has been posted");
			} catch (TwitterException e) {
				Log.e(TAG, "Error updating status", e);
				
				/*
				 * We should retry if the user requests..
				 */
				Notification notification = 
					getCustomNotification("Error updating status", 
						"Tap to retry");
				
				Intent intent = new Intent(mApplication, NewStatusService.class);
				
				intent.putExtra(NewStatusService.TWEET_ACCOUNT, mAccountId);
				
				intent.putExtra(NewStatusService.TWEET_TEXT, mTweetText);
				
				if(mLatitude != -1 && mLongitude != -1) {
					intent.putExtra(NewStatusService.TWEET_LAT, mLatitude);
					intent.putExtra(NewStatusService.TWEET_LONG, mLongitude);
				}
				
				
				PendingIntent contentIntent = 
					PendingIntent.getActivity(NewStatusService.this, 0, intent, 0);
				
				notification.contentIntent = contentIntent;
				
				//Ask for a retry..
				mNotificationManager.notify(RETRY_NOTIFICATION, notification);
				
				return;
			}
		}
	}
	
	/*
	 * Shows the given text in the notification bar, then removes itself
	 */
	private void showTickerText(String title, String text) {
		/*
		 * Even though we only want the ticker text, 
		 * we need to build the whole notification
		 */
		Notification notification = 
			new Notification(R.drawable.twitter_notification_icon, 
					text, System.currentTimeMillis());
		
		Context context = getApplicationContext();
		CharSequence contentTitle = title;
		CharSequence contentText = text;
		Intent notificationIntent = new Intent(this, TTActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(TICKER_NOTIFICATION, notification);
	}
	
	/*
	 * Builds a notification with an icon and a message
	 */
	private Notification getCustomNotification(String tickerText, String message) {
		long when = System.currentTimeMillis();

		Notification notification = new Notification(R.drawable.twitter_notification_icon, 
				tickerText, when);
		
		Intent notificationIntent = new Intent(this, TTActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.contentIntent = contentIntent;
		
		RemoteViews contentView = 
			new RemoteViews(getPackageName(), R.layout.status_notification_layout);
		contentView.setImageViewResource(R.id.image, R.drawable.icon);
		contentView.setTextViewText(R.id.text, message);
		notification.contentView = contentView;
		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		return notification;
	}
}
