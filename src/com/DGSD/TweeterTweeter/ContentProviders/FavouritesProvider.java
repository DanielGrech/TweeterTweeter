package com.DGSD.TweeterTweeter.ContentProviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.LiveFolders;

import com.DGSD.TweeterTweeter.StatusData;

public class FavouritesProvider extends ContentProvider{

	public static final String TAG = FavouritesProvider.class.getSimpleName();

	public static final Uri CONTENT_URI = 
		Uri.parse("content://com.DGSD.TweeterTweeter.ContentProvider.Favourites");

	public static final String SINGLE_RECORD_MIME_TYPE =
		"vnd.android.cursor.item/vnd.com.DGSD.TweeterTweeter.favourite";

	public static final String MULTIPLE_RECORDS_MIME_TYPE =
		"vnd.android.cursor.dir/vnd.com.DGSD.TweeterTweeter.mfavourite";

	private StatusData mStatusData;


	private static final String[] mProjection = {
		StatusData.C_ID + " AS " + LiveFolders._ID,
		StatusData.C_TEXT + " AS " + LiveFolders.NAME,
		StatusData.C_USER_NAME + " AS " + LiveFolders.DESCRIPTION
	};

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		//This content provider will be read-only
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return this.getId(uri) < 0 ?
				MULTIPLE_RECORDS_MIME_TYPE : SINGLE_RECORD_MIME_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//This content provider will be read-only
		return null;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if(mStatusData == null) {
			mStatusData = new StatusData(getContext());
		}


		long id = this.getId(uri);
		SQLiteDatabase db = mStatusData.getDbHelper().getReadableDatabase(); 
		if (id < 0) {
			return db.query(StatusData.FAVOURITES_TABLE, mProjection,
					selection, selectionArgs, null, null, StatusData.C_CREATED_AT); 
		} else {
			return db.query(StatusData.FAVOURITES_TABLE, mProjection, 
					StatusData.C_ID + "=" + id, null, null, null, StatusData.C_CREATED_AT); 
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		//This content provider will be read-only
		return 0;
	}

	private long getId(Uri uri) {
		String lastPathSegment = uri.getLastPathSegment(); 
		if (lastPathSegment != null) {
			try {
				return Long.parseLong(lastPathSegment); 
			} catch (NumberFormatException e) { 
				// at least we tried
			}
		}
		return -1;   
	}

}
