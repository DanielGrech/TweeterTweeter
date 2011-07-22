package com.DGSD.TweeterTweeter.Utils;

import android.database.Cursor;

public class ListUtils {
	private static final String TAG = ListUtils.class.getSimpleName();
	
	public static String getTweetProperty(Cursor mCursor, String column, int pos) {
		String retval = "";

		try{
			if(mCursor != null && mCursor.moveToPosition(pos)) {
				retval = mCursor.getString(mCursor.getColumnIndex(column));
			} else {
				Log.d(TAG,"No Tweet at position: " + pos);
			}
		}catch(RuntimeException e) {
			Log.e(TAG, "Error getting tweet id", e);
		}

		return retval;
	}
}
