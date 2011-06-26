package com.DGSD.TweeterTweeter.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.DGSD.TweeterTweeter.Services.UpdaterService;

/*
 * Used to start various services when the phone boots.
 */
public class BootReceiver extends BroadcastReceiver { 

	@Override
	public void onReceive(Context context, Intent intent) { 
		context.startService(new Intent(context, UpdaterService.class)); 
		Log.d("BootReceiver", "onReceived");
	}

}