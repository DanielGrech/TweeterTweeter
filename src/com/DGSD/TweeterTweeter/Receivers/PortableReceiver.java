package com.DGSD.TweeterTweeter.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.DGSD.TweeterTweeter.Utils.Log;

public class PortableReceiver extends BroadcastReceiver {
	private static final String TAG = PortableReceiver.class.getSimpleName();

	private Receiver mReceiver;

	public void clearReceiver() {
		mReceiver = null;
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		public void onReceive(Context context, Intent intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (mReceiver != null) {
			mReceiver.onReceive(context, intent);
		} else {
			Log.w(TAG, "Dropping received Result");
		}
		
	}
}