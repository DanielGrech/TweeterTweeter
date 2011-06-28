package com.DGSD.TweeterTweeter.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Utils.Log;

/*
 * Used to start various services when the phone boots.
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = BootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent callingIntent) {

		// Check if we should do anything at boot at all
		long interval = ((TTApplication) context.getApplicationContext()).getInterval(); 
		
		if (interval == TTApplication.INTERVAL_NEVER) { 
			return;
		}

		// Create the pending intent
		Intent intent = new Intent(context, UpdaterService.class);
		intent.putExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.ALL_DATA);
		
		PendingIntent pendingIntent = PendingIntent.getService(context, -1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT); 

		// Setup alarm service to wake up and start service periodically
		AlarmManager alarmManager = (AlarmManager) context
			.getSystemService(Context.ALARM_SERVICE); 
		
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System
				.currentTimeMillis(), interval, pendingIntent);

		Log.d(TAG, "onReceived");
	}

}