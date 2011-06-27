package com.DGSD.TweeterTweeter.Utils;
/*
 *Simple class to allow easily switching off of android's default log calls
 */
public class Log {
	public static final boolean LOG = true;

	/* Methods without exceptions */
	public static void i(String tag, String string) {
		if (LOG) android.util.Log.i(tag, string);
	}
	
	public static void e(String tag, String string) {
		if (LOG) android.util.Log.e(tag, string);
	}
	
	public static void d(String tag, String string) {
		if (LOG) android.util.Log.d(tag, string);
	}
	
	public static void v(String tag, String string) {
		if (LOG) android.util.Log.v(tag, string);
	}
	
	public static void w(String tag, String string) {
		if (LOG) android.util.Log.w(tag, string);
	}

	/* Methods with exceptions */
	public static void i(String tag, String string, Exception e) {
		if (LOG) android.util.Log.i(tag, string, e);
	}
	
	public static void e(String tag, String string, Exception e) {
		if (LOG) android.util.Log.e(tag, string, e);
	}
	
	public static void d(String tag, String string, Exception e) {
		if (LOG) android.util.Log.d(tag, string, e);
	}
	
	public static void v(String tag, String string, Exception e) {
		if (LOG) android.util.Log.v(tag, string, e);
	}
	
	public static void w(String tag, String string, Exception e) {
		if (LOG) android.util.Log.w(tag, string, e);
	}
	
}