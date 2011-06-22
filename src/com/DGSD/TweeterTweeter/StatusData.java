package com.DGSD.TweeterTweeter;

import twitter4j.User;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StatusData { 
	private static final String TAG = StatusData.class.getSimpleName();

	private static final int VERSION = 1;

	private static final String DATABASE = "twitter.db";

	/*
	 * Various database tables
	 */
	public static final String TIMELINE_TABLE = "timeline_table";
	
	public static final String FAVOURITES_TABLE = "favourites_table";
	
	public static final String FOLLOWERS_TABLE = "followers_table";
	
	public static final String FOLLOWING_TABLE = "following_table";
	
	public static final String PROFILE_TABLE = "profile_table";
	
	/*
	 * Various database columns.
	 */
	
	public static final String C_ID = "_id";

	public static final String C_CREATED_AT = "created_at";

	public static final String C_TEXT = "txt";

	public static final String C_USER = "user_name";

	public static final String C_IMG = "imageurl";
	
	public static final String C_FAV = "isFavourite";
	
	public static final String C_SRC = "source";
	
	public static final String C_NAME = "user_name";
	
	public static final String C_SCREEN_NAME = "screen_name";
	
	public static final String C_DESC = "description";
	
	public static final String C_FOLLOWERS = "follower_count";
	
	public static final String C_FRIENDS = "friends_count";
	
	public static final String C_NUM_STAT = "num_of_status";
	
	/*
	 * Various database operations.
	 */
	private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";

	private static final String[] MAX_CREATED_AT_COLUMNS = { "max("
		+ StatusData.C_CREATED_AT + ")" };

	private static final String[] DB_TEXT_COLUMNS = { C_TEXT };

	
	// DbHelper implementations
	class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "Creating database: " + DATABASE);
			
			db.execSQL("create table " + TIMELINE_TABLE + " (" + C_ID + " text primary key, "
					+ C_CREATED_AT + " text, " + C_USER + " text, " + C_TEXT + " text, " 
					+ C_IMG + " text, " + C_FAV + " int, " + C_SRC + " text)");
			
			db.execSQL("create table " + FAVOURITES_TABLE + " (" + C_ID + " text primary key, "
					+ C_CREATED_AT + " text, " + C_USER + " text, " + C_TEXT + " text, " 
					+ C_IMG + " text, " + C_FAV + " int, " + C_SRC + " text)");
			
			db.execSQL("create table " + FOLLOWERS_TABLE + " (" + C_ID + " text primary key, "
					+ C_CREATED_AT + " text, " + C_NAME + " text, " + C_SCREEN_NAME + " text, "
					+ C_DESC + " text, " + C_FAV + " int, " + C_FOLLOWERS + " int, "
					+ C_FRIENDS + " int, " + C_NUM_STAT + " int, " + C_TEXT + " text, " 
					+ C_IMG + " text)");
			
			db.execSQL("create table " + FOLLOWING_TABLE + " (" + C_ID + " text primary key, "
					+ C_CREATED_AT + " text, " + C_NAME + " text, " + C_SCREEN_NAME + " text, "
					+ C_DESC + " text, " + C_FAV + " int, " + C_FOLLOWERS + " int, "
					+ C_FRIENDS + " int, " + C_NUM_STAT + " int, " + C_TEXT + " text, " 
					+ C_IMG + " text)");
			
			db.execSQL("create table " + PROFILE_TABLE + " (" + C_ID + " text primary key, "
					+ C_CREATED_AT + " text, " + C_NAME + " text, " + C_SCREEN_NAME + " text, "
					+ C_DESC + " text, " + C_FAV + " int, " + C_FOLLOWERS + " int, "
					+ C_FRIENDS + " int, " + C_NUM_STAT + " int, " + C_TEXT + " text, " 
					+ C_IMG + " text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + TIMELINE_TABLE);
			db.execSQL("drop table " + FAVOURITES_TABLE);
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

	public static synchronized ContentValues createTimelineContentValues(String id, 
			String createdAt, String user, String text, String imgUrl, 
			boolean isFav, String source) {
		ContentValues values = new ContentValues();

		values.put(C_ID, id);
		values.put(C_CREATED_AT, createdAt);
		values.put(C_TEXT, text);
		values.put(C_USER, user);
		values.put(C_IMG, imgUrl);
		values.put(C_FAV, isFav ? 1 : 0);
		values.put(C_SRC, source);
		
		return values;
	}

	public static synchronized ContentValues createUserContentValues(User u) {
		ContentValues values = new ContentValues();
		
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
			Log.w(TAG, "Null pointer getting tweet text", e);
			values.put(C_TEXT, "" );
		}
		try{
			values.put(C_IMG, u.getProfileImageURL().toString() );
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
		Log.d(TAG, "inserting values");
		
		
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();  
		try {
			db.insertWithOnConflict(table, null, values,
					SQLiteDatabase.CONFLICT_IGNORE);  
		} finally {
			db.close(); 
		}
	}

	/**
	 *
	 * @return Cursor where the columns are _id, created_at, user, txt, usrImgUrl
	 */
	public Cursor getStatusUpdates() {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(TIMELINE_TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public Cursor getFavourites() {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(FAVOURITES_TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public Cursor getFriends() {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(FOLLOWING_TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}
	
	public Cursor getFollowers() {   
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		return db.query(FOLLOWERS_TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}

	/**
	 *
	 * @return Timestamp of the latest status we ahve it the database
	 */
	public long getLatestStatusCreatedAtTime() {  
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TIMELINE_TABLE, MAX_CREATED_AT_COLUMNS, null, null, null,
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

	/**
	 *
	 * @param id of the status we are looking for
	 * @return Text of the status
	 */
	public String getStatusTextById(long id) {  // 
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TIMELINE_TABLE, DB_TEXT_COLUMNS, C_ID + "=" + id, null,
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


}