package com.DGSD.TweeterTweeter;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorJoiner;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.DGSD.TweeterTweeter.Utils.Log;

public class StatusData { 
	private static final String TAG = StatusData.class.getSimpleName();

	private static final int VERSION = 1;

	private static final String DATABASE = "twitter.db";

	/*
	 * Various database tables
	 */
	public static final String TABLE_NAME_TEMPLATE = "<!@#TABLENAME!@#>";
	public static final String HOME_TIMELINE_TABLE = "home_timeline_table";
	public static final String TIMELINE_TABLE = "timeline_table";
	public static final String FAVOURITES_TABLE = "favourites_table";
	public static final String MENTIONS_TABLE = "mentions_table";
	public static final String FOLLOWERS_TABLE = "followers_table";
	public static final String FOLLOWING_TABLE = "following_table";
	public static final String PROFILE_TABLE = "profile_table";
	public static final String RT_OF_TABLE = "rt_of_me_table";
	public static final String RT_BY_TABLE = "rt_by_me_table";
	public static final String FAVOURITES_PENDING_TABLE = "favourites_pending_table";
	public static final String UNFAVOURITES_PENDING_TABLE = "unfavourites_pending_table";


	/*
	 * Various database columns.
	 */

	public static final String C_ACCOUNT = "account";
	public static final String C_ID = "_id";
	public static final String C_CREATED_AT = "created_at";
	public static final String C_TEXT = "txt";
	public static final String C_USER = "user";
	public static final String C_USER_NAME = "user_name";
	public static final String C_IMG = "imageurl";
	public static final String C_FAV = "isFavourite";
	public static final String C_SRC = "source";
	public static final String C_NAME = "user_name";
	public static final String C_SCREEN_NAME = "screen_name";
	public static final String C_DESC = "description";
	public static final String C_FOLLOWERS = "follower_count";
	public static final String C_FRIENDS = "friends_count";
	public static final String C_NUM_STAT = "num_of_status";
	public static final String C_IN_REPLY = "in_reply_to_screenname";
	public static final String C_ORIG_TWEET = "orig_tweeter_name";
	public static final String C_RETWEET_COUNT = "retweet_count";
	public static final String C_PLACE_NAME = "place_name";
	public static final String C_LAT = "latitude";
	public static final String C_LONG = "longitude";
	public static final String C_MEDIA_ENT = "media_entities";
	public static final String C_HASH_ENT = "hastag_entities";
	public static final String C_URL_ENT = "url_entities";
	public static final String C_USER_ENT = "user_entities";

	/*
	 * Various database operations.
	 */
	private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";

	private static final String GET_ALL_ORDER_BY_ALPHA = C_SCREEN_NAME + " COLLATE NOCASE";

	private static final String[] MAX_CREATED_AT_COLUMNS = { "max("
			+ C_CREATED_AT + ")" };

	private static final String[] MAX_ID_COLUMNS = { "max("
			+ C_ID + ")" };

	private static final String[] MIN_ID_COLUMNS = { "min("
			+ C_ID + ")" };


	private static final String[] DB_TEXT_COLUMNS = { C_TEXT };


	// DbHelper implementations
	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DATABASE);

			String statusTemp = "create table " + TABLE_NAME_TEMPLATE + " (" + 
					C_ACCOUNT + " text, " + 
					C_USER + " text, " + 
					C_ID + " text primary key, " + 
					C_CREATED_AT + " text, " + 
					C_TEXT + " text, " + 
					C_USER_NAME + " text, " + 
					C_SCREEN_NAME + " text, "+ 
					C_IMG + " text, " + 
					C_FAV + " int, " + 
					C_SRC + " text, " + 
					C_IN_REPLY + " text, " + 
					C_ORIG_TWEET + " text, " + 
					C_RETWEET_COUNT + " int, " + 
					C_PLACE_NAME + " text, " + 
					C_LAT + " text, " + 
					C_LONG + " text, " + 
					C_MEDIA_ENT + " text, " + 
					C_HASH_ENT + " text, " + 
					C_URL_ENT + " text, " + 
					C_USER_ENT + " text)";

			String userTemp = "create table " + TABLE_NAME_TEMPLATE + " (" +
					C_ACCOUNT + " text, " +
					C_NAME + " text, " +
					C_ID + " text primary key, " +
					C_USER + " text, " +
					C_SCREEN_NAME + " text, " +
					C_CREATED_AT + " text, " + 
					C_DESC + " text, " +
					C_FAV + " int, " + 
					C_FOLLOWERS + " int, " +
					C_FRIENDS + " int, " + 
					C_NUM_STAT + " int, " + 
					C_TEXT + " text, " +
					C_IMG + " text)";

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, HOME_TIMELINE_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, TIMELINE_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, FAVOURITES_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, FAVOURITES_PENDING_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, UNFAVOURITES_PENDING_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, MENTIONS_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, RT_BY_TABLE));

			db.execSQL(statusTemp.replace(TABLE_NAME_TEMPLATE, RT_OF_TABLE));


			db.execSQL(userTemp.replace(TABLE_NAME_TEMPLATE, FOLLOWERS_TABLE));

			db.execSQL(userTemp.replace(TABLE_NAME_TEMPLATE, FOLLOWING_TABLE));

			db.execSQL(userTemp.replace(TABLE_NAME_TEMPLATE, PROFILE_TABLE));

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + HOME_TIMELINE_TABLE);
			db.execSQL("drop table " + TIMELINE_TABLE);
			db.execSQL("drop table " + FAVOURITES_TABLE);
			db.execSQL("drop table " + FAVOURITES_PENDING_TABLE);
			db.execSQL("drop table " + UNFAVOURITES_PENDING_TABLE);
			db.execSQL("drop table " + MENTIONS_TABLE);
			db.execSQL("drop table " + RT_BY_TABLE);
			db.execSQL("drop table " + RT_OF_TABLE);
			db.execSQL("drop table " + FOLLOWERS_TABLE);
			db.execSQL("drop table " + FOLLOWING_TABLE);
			db.execSQL("drop table " + PROFILE_TABLE);

			this.onCreate(db);
		}
	}

	private final DbHelper dbHelper; 

	public StatusData(Context context) { 
		this.dbHelper = new DbHelper(context);
		Log.i(TAG, "Initialized data");
	}

	public DbHelper getDbHelper() {
		return dbHelper;
	}

	public static synchronized ContentValues 
	createTimelineContentValues(String account, String user, Status status) {
		ContentValues values = new ContentValues();


		String mediaEntities = "";
		String hashtagEntities = "";
		String urlEntities = "";
		String userEntities = "";
		String placeName = "";
		String latitude = "";
		String longitude = "";
		String retweetedScreenName = "";

		if(status.getMediaEntities() != null) {
			for(MediaEntity me: status.getMediaEntities()) {
				mediaEntities += me.getMediaURL().toString() + ",";
			}
		}

		if(status.getHashtagEntities() != null) {
			for(HashtagEntity ht: status.getHashtagEntities()) {
				hashtagEntities += ht.getText()+ ",";
			}
		}

		if(status.getURLEntities() != null) {
			for(URLEntity url: status.getURLEntities()) {
				urlEntities += url.getExpandedURL() + ",";
			}
		}

		if(status.getUserMentionEntities() != null) {
			for(UserMentionEntity um : status.getUserMentionEntities()) {
				userEntities += um.getScreenName()+ ",";
			}
		}

		if( status.getPlace() != null ) {
			placeName = status.getPlace().getName();
		}

		if(status.getGeoLocation() != null) {
			latitude = Double.toString(status.getGeoLocation().getLatitude());
			longitude = Double.toString(status.getGeoLocation().getLongitude());
		}

		if(status.getRetweetedStatus() != null) {
			retweetedScreenName = 
					status.getRetweetedStatus().getUser().getScreenName();
		}

		values.put(C_ACCOUNT, account);
		values.put(C_USER, user);
		values.put(C_ID, Long.toString(status.getId()));
		values.put(C_CREATED_AT, Long.toString(status.getCreatedAt().getTime()));
		values.put(C_TEXT, status.getText());
		values.put(C_USER_NAME, status.getUser().getName());
		values.put(C_SCREEN_NAME, status.getUser().getScreenName());
		values.put(C_IMG, status.getUser().getProfileImageURL().toString().replace("_normal.", "_bigger."));
		values.put(C_FAV, status.isFavorited() ? 1 : 0);
		values.put(C_SRC, status.getSource());
		values.put(C_IN_REPLY, status.getInReplyToScreenName());
		values.put(C_ORIG_TWEET, retweetedScreenName);
		values.put(C_RETWEET_COUNT, status.getRetweetCount());
		values.put(C_PLACE_NAME, placeName);
		values.put(C_LAT, latitude);
		values.put(C_LONG, longitude);
		values.put(C_MEDIA_ENT, mediaEntities);
		values.put(C_HASH_ENT, hashtagEntities);
		values.put(C_URL_ENT, urlEntities);
		values.put(C_USER_ENT, userEntities);


		return values;
	}

	public static synchronized ContentValues createUserContentValues(String account, String user, User u) {
		ContentValues values = new ContentValues();

		values.put(C_ACCOUNT, account);
		values.put(C_USER, user);
		values.put(C_ID, Long.toString(u.getId()) );
		values.put(C_CREATED_AT, Long.toString(u.getCreatedAt().getTime()) );
		values.put(C_NAME, u.getName() );
		values.put(C_SCREEN_NAME, u.getScreenName() );
		values.put(C_DESC, u.getDescription() );
		values.put(C_FAV, u.getFavouritesCount() );
		values.put(C_FOLLOWERS, u.getFollowersCount() );
		values.put(C_FRIENDS, u.getFriendsCount() );
		values.put(C_NUM_STAT, u.getStatusesCount() );

		try{
			values.put(C_TEXT, u.getStatus().getText() );
		}catch(NullPointerException e) {
			Log.w(TAG, "Null pointer getting tweet text");
			values.put(C_TEXT, "" );
		}
		try{
			values.put(C_IMG, u.getProfileImageURL().toString().replace("_normal.", "_bigger.") );
		}catch(NullPointerException e) {
			Log.w(TAG, "Null pointer getting profile image", e);
			values.put(C_TEXT, "" );
		}

		return values;
	}

	public void close() {  
		this.dbHelper.close();
	}

	public void insertOrIgnore(String table, ContentValues values) {  
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();  
		try {
			db.insertWithOnConflict(table, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);  
		} finally {
			db.close(); 
		}
	}

	public boolean insert(String table, ContentValues values) {  
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();  
		try {
			db.insertOrThrow(table, null, values);  
			return true;
		} catch(SQLException e) { 
			return false;
		} finally {
			db.close(); 
		}
	}

	public boolean contains(String table, String accountId, String column, String val) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		Cursor c = db.query(table, new String[]{column}, 
				C_ACCOUNT + " IN (\"" + accountId + "\") AND " + column + "=\"" + val + "\"", 
				null, null, null, null);

		return !(c == null || c.getCount() == 0);
	}


	public Cursor getStatusUpdates(String accountId, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(HOME_TIMELINE_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
				null, null, null, GET_ALL_ORDER_BY);
	}

	public Cursor getPendingFavourites(String accountId, String[] columns) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		return db.query(FAVOURITES_PENDING_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
				null, null, null, GET_ALL_ORDER_BY);
	}

	public void removeFavourite(String account, String tweetid) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		if( db.delete(FAVOURITES_TABLE, 
				C_ACCOUNT + " IN (\"" + account + "\") AND " + 
						C_ID + " IN (\"" + tweetid + "\")", null) <= 0 ) {
			Log.i(TAG, "Couldnt find favourite pending to delete");
		} else {
			Log.i(TAG, "Favourite pending deleted!");
		}
	}

	public void removePendingFavourite(String account, String tweetid) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		if( db.delete(FAVOURITES_PENDING_TABLE, 
				C_ACCOUNT + " IN (\"" + account + "\") AND " + 
						C_ID + " IN (\"" + tweetid + "\")", null) <= 0 ) {
			Log.i(TAG, "Couldnt find favourite pending to delete");
		} else {
			Log.i(TAG, "Favourite pending deleted!");
		}
	}

	public Cursor getPendingUnfavourites(String accountId, String[] columns) {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		return db.query(UNFAVOURITES_PENDING_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
				null, null, null, GET_ALL_ORDER_BY);
	}

	public void removePendingUnfavourite(String account, String tweetid) {
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		if( db.delete(UNFAVOURITES_PENDING_TABLE, 
				C_ACCOUNT + " IN (\"" + account + "\") AND " + 
						C_ID + " IN (\"" + tweetid + "\")", null) <= 0 ) {
			Log.i(TAG, "Couldnt find favourite pending to delete");
		} else {
			Log.i(TAG, "Favourite pending deleted!");
		}
	}

	public Cursor getFavourites(String accountId, String screenName, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(FAVOURITES_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
					null, null, null, GET_ALL_ORDER_BY);
		} else {
			return db.query(FAVOURITES_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + C_USER + "=\"" + screenName + "\"", 
					null, null, null, GET_ALL_ORDER_BY);
		}
	}

	public Cursor getMentions(String accountId, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(MENTIONS_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
				null, null, null, GET_ALL_ORDER_BY);
	}

	public Cursor getRetweetsOf(String accountId, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(RT_OF_TABLE, columns, C_ACCOUNT + "=\"" + accountId + "\"", 
				null, null, null, GET_ALL_ORDER_BY);
	}

	public Cursor getRetweetsBy(String accountId, String screenName, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(RT_BY_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
					null, null, null, GET_ALL_ORDER_BY);
		} else {
			return db.query(RT_BY_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + C_USER + "=\"" + screenName + "\"", 
					null, null, null, GET_ALL_ORDER_BY);
		}
	}

	public Cursor getTimeline(String accountId, String screenName, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(TIMELINE_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
					null, null, null, GET_ALL_ORDER_BY);
		} else {
			return db.query(TIMELINE_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + C_USER + "=\"" + screenName + "\"", 
					null, null, null, GET_ALL_ORDER_BY);
		}
	}

	public Cursor getFollowing(String accountId, String screenName, String whereClause, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(FOLLOWING_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + whereClause, 
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		} else {
			return db.query(FOLLOWING_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + 
							C_USER + "=\"" + screenName + "\" "+ whereClause, 
							null, null, null, GET_ALL_ORDER_BY_ALPHA);
		}
	}	

	public Cursor getFollowing(String accountId, String screenName, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		if(screenName == null) {
			return db.query(FOLLOWING_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		} else {
			return db.query(FOLLOWING_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + C_USER + "=\"" + screenName + "\"", 
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		}

	}

	public Cursor getFollowers(String accountId, String screenName, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(FOLLOWERS_TABLE, columns, C_ACCOUNT + " IN (\"" + accountId + "\")", 
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		} else {
			return db.query(FOLLOWERS_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + C_USER + "=\"" + screenName + "\"", 
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		}
	}

	public Cursor getFollowers(String accountId, String screenName, String whereClause, String[] columns) {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		if(screenName == null) {
			return db.query(FOLLOWERS_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\") AND " + whereClause,
					null, null, null, GET_ALL_ORDER_BY_ALPHA);
		} else {
			return db.query(FOLLOWERS_TABLE, columns, 
					C_ACCOUNT + " IN (\"" + accountId + "\")  AND " + 
							C_USER + "=\"" + screenName + "\" "+ whereClause, 
							null, null, null, GET_ALL_ORDER_BY_ALPHA);
		}
	}

	/**
	 * @return The screenname and image url of any person we have cached
	 */
	public Cursor getPeople(String accountId, String screenName, String whereClause, String[] columns) {   
		Cursor following = getFollowing(accountId, null, whereClause, columns);
		Cursor followers = getFollowers(accountId, null, whereClause, columns);

		CursorJoiner joiner = new CursorJoiner(following, new String[]{C_ID} , followers, new String[]{C_ID});

		MatrixCursor retval = new MatrixCursor(new String[] {C_SCREEN_NAME, C_IMG, C_ID});

		String name;
		String image;
		String id;

		for(CursorJoiner.Result result : joiner) { 
			name = null;
			image = null;
			id = null;
			try {
				switch(result) {

					case BOTH:
						name = following.getString(following.getColumnIndex(C_SCREEN_NAME));
						image = following.getString(following.getColumnIndex(C_IMG));
						id = following.getString(following.getColumnIndex(C_ID));
					case LEFT:
						name = following.getString(following.getColumnIndex(C_SCREEN_NAME));
						image = following.getString(following.getColumnIndex(C_IMG));
						id = following.getString(following.getColumnIndex(C_ID));
					case RIGHT:
						name = followers.getString(followers.getColumnIndex(C_SCREEN_NAME));
						image = followers.getString(followers.getColumnIndex(C_IMG));
						id = followers.getString(followers.getColumnIndex(C_ID));
				}
			} catch(CursorIndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			retval.addRow(new String[]{name, image, id});
		}

		return retval;
	}


	/**
	 *
	 * @return Timestamp of the latest status we have it the database
	 */
	public long getLatestCreatedAtTime(String table, String accountId) {  
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(table, MAX_CREATED_AT_COLUMNS, 
					C_ACCOUNT + " IN (\"" + accountId + "\")", null, null,
					null, null);
			try {
				return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public String getLatestTweetId(String table, String accountId) {  
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(table, MAX_ID_COLUMNS, 
					C_ACCOUNT + " IN (\"" + accountId + "\")", null, null,
					null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public String getOldestTweetId(String table, String accountId) {  
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(table, MIN_ID_COLUMNS, 
					C_ACCOUNT + " IN (\"" + accountId + "\")", null, null,
					null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public String getStatusTextById(String accountId, long id) {  // 
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(HOME_TIMELINE_TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null,
					null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public static ContentValues getStatus(Cursor c) {
		if(c == null) {
			return null;
		}
		
		ContentValues values = new ContentValues();

		values.put(C_ACCOUNT, c.getString(c.getColumnIndex(C_ACCOUNT)));
		values.put(C_USER, c.getString(c.getColumnIndex(C_USER)));
		values.put(C_ID, c.getString(c.getColumnIndex(C_ID)));
		values.put(C_CREATED_AT, c.getString(c.getColumnIndex(C_CREATED_AT)));
		values.put(C_TEXT, c.getString(c.getColumnIndex(C_TEXT)));
		values.put(C_USER_NAME, c.getString(c.getColumnIndex(C_USER_NAME)));
		values.put(C_SCREEN_NAME, c.getString(c.getColumnIndex(C_SCREEN_NAME)));
		values.put(C_IMG, c.getString(c.getColumnIndex(C_IMG)));
		values.put(C_FAV, c.getString(c.getColumnIndex(C_FAV)));
		values.put(C_SRC, c.getString(c.getColumnIndex(C_SRC)));
		values.put(C_IN_REPLY, c.getString(c.getColumnIndex(C_IN_REPLY)));
		values.put(C_ORIG_TWEET, c.getString(c.getColumnIndex(C_ORIG_TWEET)));
		values.put(C_RETWEET_COUNT, c.getString(c.getColumnIndex(C_RETWEET_COUNT)));
		values.put(C_PLACE_NAME, c.getString(c.getColumnIndex(C_PLACE_NAME)));
		values.put(C_LAT, c.getString(c.getColumnIndex(C_LAT)));
		values.put(C_LONG, c.getString(c.getColumnIndex(C_LONG)));
		values.put(C_MEDIA_ENT, c.getString(c.getColumnIndex(C_MEDIA_ENT)));
		values.put(C_HASH_ENT, c.getString(c.getColumnIndex(C_HASH_ENT)));
		values.put(C_URL_ENT, c.getString(c.getColumnIndex(C_URL_ENT)));
		values.put(C_USER_ENT, c.getString(c.getColumnIndex(C_USER_ENT)));

		return values;
	}

}