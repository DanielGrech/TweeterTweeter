package com.DGSD.TweeterTweeter.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Fragments.NewTweetFragment;

public class MediaUploadTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = MediaUploadTask.class.getSimpleName();

	private TTApplication mApplication;

	private Activity mActivity;

	private TextView mTextView;
	
	private File mFile;

	private File mImageFile;

	private Bitmap mBitmap;

	private ProgressDialog mProgressDialog;

	private String mUrl;

	private boolean has_error;

	private int mType;

	public static final int CAMERA_IMG = 0;

	public static final int GALLERY_IMG = 1;

	public MediaUploadTask(Activity a, TextView tv, int type, Bitmap b) {
		mActivity = a;
		
		mTextView = tv;

		mApplication = (TTApplication) mActivity.getApplication();

		mType = type;
		if(mType == CAMERA_IMG) {
			mBitmap = b;
		}
	}

	public MediaUploadTask(Activity a, TextView tv, int type, String filepath) {
		mActivity = a;

		mApplication = (TTApplication) mActivity.getApplication();
		
		mTextView = tv;

		mType = type;
		if(mType == GALLERY_IMG) {
			mImageFile = new File(filepath);
		}
	}


	@Override
	protected void onPreExecute() {
		mProgressDialog = ProgressDialog.show(mActivity , "", 
				"Uploading image. Please wait...", true);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		String twitpicKey = mActivity .getResources().getString(R.string.twitpic_key);

		Properties props = new Properties();

		props.put(PropertyConfiguration.MEDIA_PROVIDER,MediaProvider.TWITPIC);
		props.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN,mApplication.getAccessToken("account1").getToken());
		props.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET,mApplication.getAccessToken("account1").getTokenSecret());
		props.put(PropertyConfiguration.OAUTH_CONSUMER_KEY,TTApplication.CONSUMER_KEY);
		props.put(PropertyConfiguration.OAUTH_CONSUMER_SECRET,TTApplication.CONSUMER_SECRET);
		props.put(PropertyConfiguration.MEDIA_PROVIDER_API_KEY,twitpicKey);

		Configuration conf = new PropertyConfiguration(props);

		ImageUpload iUpload = new ImageUploadFactory(conf).getInstance(MediaProvider.TWITPIC);

		switch(mType) {
			case CAMERA_IMG:
				try {
					mFile = File.createTempFile(Integer.toString(new Random().nextInt()), ".jpg");
					writeBitmapToFile(mBitmap, mFile);
				} catch(Exception e){
					Log.e(TAG, "Error saving image", e);
					has_error = true;
					return null;
				}
				break;
			case GALLERY_IMG:
				try {
					//We need to make the image smaller!
					Bitmap b = decodeFile(mImageFile);
					mFile = File.createTempFile(Integer.toString(new Random().nextInt()), ".jpg");
					writeBitmapToFile(b, mFile);
				} catch (IOException e) {
					Log.e(TAG, "Error saving image", e);
					has_error = true;
					return null;
				}
				break;
		}

		try {
			mUrl = iUpload.upload(mFile);

			if(mType == CAMERA_IMG) {
				//Only if we started with a Bitmap (not an actual file) will 
				//we delete this..
				if (!mFile.delete()) {
					Log.i(TAG, "Failed to delete " + mFile.getAbsolutePath());
				}
			}
		} catch (TwitterException e) {
			Log.e(TAG, "Error uploading image", e);
			has_error = true;
		} catch(RuntimeException e) {
			Log.e(TAG, "Error uploading image", e);
			has_error = true;
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void arg) {
		mProgressDialog.dismiss();
		if(has_error) {
			Toast.makeText(mActivity, "Error occured!", Toast.LENGTH_LONG).show();
			Log.i(TAG, "Error occured!");
		}
		else {
			Log.i(TAG, "Photo at " + mUrl);

			NewTweetFragment.addToTweet(mTextView, mUrl);

		}
	}

	private void writeBitmapToFile(Bitmap b, File f) throws IOException {
		final FileOutputStream out = new FileOutputStream(f);

		b.compress(Bitmap.CompressFormat.JPEG, 90, out);

		out.close();	
	}

	private Bitmap decodeFile(File f){
		try {
			//decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f),null,o);

			//Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE=70;
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true){
				if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale++;
			}

			//decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Error decoding image", e);
		}
		return null;
	}
}
