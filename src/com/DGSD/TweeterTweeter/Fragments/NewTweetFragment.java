package com.DGSD.TweeterTweeter.Fragments;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploadFactory;
import twitter4j.media.MediaProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import com.DGSD.TweeterTweeter.Utils.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Services.NewStatusService;
import com.DGSD.TweeterTweeter.Utils.Tokenizer;
import com.github.droidfu.widgets.WebImageView;
import com.rosaloves.bitlyj.BitlyException;

/*
 * TODO: 
 * Ask for media description before upload..
 * If image upload fails, should alert user where the file is saved
 * Make media provider configurable (twitpic, yfrog etc).
 */
public class NewTweetFragment extends DialogFragment 
implements OnClickListener {

	public static final String TAG = NewTweetFragment.class.getSimpleName();

	private static final int GET_CAMERA_IMAGE = 0;

	private static final int GET_GALLERY_IMAGE = 1;

	private static final int GET_CAMERA_VIDEO = 2;

	public static final int MAX_TWEET_SIZE = 140;

	private static final int MAXIMUM_VIDEO_SIZE = 2; //2Mb

	private static final int LOW_QUALITY_VIDEO = 0;

	private TTApplication mApplication;

	private String mAccountId;

	private ViewGroup mAccountContainer;

	private MultiAutoCompleteTextView mTweetEditText;

	private TextView mCharacterCountView;

	private Button mShortenUrlButton;

	private Button mMediaButton;

	private Button mLocationButton;

	private Button mSubmitButton;
	
	private long mLatitude = -1;
	
	private long mLongitude = -1;

	protected static final String[] FROM = 
	{ StatusData.C_SCREEN_NAME, StatusData.C_IMG };

	protected static final int[] TO = {R.id.profile_name, R.id.profile_image};

	protected static final ViewBinder mViewBinder = new ViewBinder() { 
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			switch( view.getId() ) {
				case R.id.profile_image:
					String url = "";
					url = cursor.getString(columnIndex);

					WebImageView wiv = (WebImageView) view;

					wiv.setImageUrl(url);
					if(url != "") {
						wiv.loadImage();
					}

					return true;

				case R.id.profile_name:
					String name = "";
					name = cursor.getString(columnIndex);

					TextView tv = (TextView) view;
					tv.setText(name);

					return true;
			}


			return false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (TTApplication) getActivity().getApplication();

		mAccountId = "account1";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.new_tweet_layout, container, false);

		mAccountContainer = 
			(ViewGroup) root.findViewById(R.id.new_tweet_accounts_container);

		mTweetEditText = 
			(MultiAutoCompleteTextView) root.findViewById(R.id.new_tweet_text);

		mCharacterCountView = 
			(TextView) root.findViewById(R.id.new_tweet_character_count);

		mShortenUrlButton = 
			(Button) root.findViewById(R.id.new_tweet_url_shorten);

		mMediaButton = 
			(Button) root.findViewById(R.id.new_tweet_media);

		mLocationButton = 
			(Button) root.findViewById(R.id.new_tweet_location);

		mSubmitButton = 
			(Button) root.findViewById(R.id.new_tweet_submit);


		Log.i(TAG, "Returning root from onCreateView");

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mShortenUrlButton.setOnClickListener(this);

		mMediaButton.setOnClickListener(this);

		mLocationButton.setOnClickListener(this);

		mSubmitButton.setOnClickListener(this);

		setupCharacterCounter();

		setupMentionCompletion();

		//Listen for when the user presses 'enter' on the keyboard.
		mTweetEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				mSubmitButton.performClick();
				return false;
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		switch(requestCode) {
			case GET_CAMERA_IMAGE:
				if (resultCode == Activity.RESULT_OK) {
					final File file = getTempFile(getActivity());

					try{
						file.deleteOnExit();
					} catch(SecurityException e) {
						//Dont worry about it..
					}

					try {
						Bitmap captureBmp = Media.getBitmap(
								getActivity().getContentResolver(), Uri.fromFile(file) );
						new MediaUploadTask(MediaUploadTask.CAMERA_IMG, 
								captureBmp).execute();
					} catch (FileNotFoundException e) {
						Log.e(TAG, "File not found", e);
					} catch (IOException e) {
						Log.e(TAG, "IO Exception", e);
					}
				}
				else {
					Log.i(TAG, "Picture not taken!");
				}
				break;
			case GET_GALLERY_IMAGE: 
				if (resultCode == Activity.RESULT_OK) {
					Uri imageUri = intent.getData();

					new MediaUploadTask(MediaUploadTask.GALLERY_IMG,
							getPath(imageUri)).execute();
				}
				else {
					Log.i(TAG, "Picture not chosen!");
				}
				break;

			case GET_CAMERA_VIDEO:
				if (resultCode == Activity.RESULT_OK) {
					Uri imageUri = intent.getData();

					new MediaUploadTask(MediaUploadTask.CAMERA_VIDEO,
							getPath(imageUri)).execute();
				}
				else {
					Log.i(TAG, "Video not taken!");
				}
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.new_tweet_url_shorten:
				new UrlShortenTask().execute();
				break;

			case R.id.new_tweet_media:
				//TODO: Put support for video back in!
				/*final CharSequence[] choices = {"Camera Picture", "Gallery image", 
						"Camera Video", "Gallery Video"};*/
				final CharSequence[] choices = {"Camera Picture", "Gallery image"};

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Media");

				builder.setItems(choices, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item) {
							case 0: 
								//Camera Picture
								takePhoto();
								break;
							case 1:
								//Gallery Picture
								getGalleryPhoto();
								break;
							case 2:
								takeVideo();
								break;
						}
					}
				});

				builder.create().show();
				break;

			case R.id.new_tweet_location:
				Log.i(TAG, "Location Button!!");
				break;

			case R.id.new_tweet_submit:
				Log.i(TAG, "Submit Button!!");
				
				Intent intent = new Intent(getActivity(), NewStatusService.class);
				
				intent.putExtra(NewStatusService.TWEET_ACCOUNT, mAccountId);
				
				intent.putExtra(NewStatusService.TWEET_TEXT, mTweetEditText.getText());
				
				if(mLatitude != -1 && mLongitude != -1) {
					intent.putExtra(NewStatusService.TWEET_LAT, mLatitude);
					intent.putExtra(NewStatusService.TWEET_LONG, mLongitude);
				}
				
				getActivity().startService(intent);
				
				break;
		}
	}

	private void setupCharacterCounter() {
		mCharacterCountView.setText( Integer.toString( MAX_TWEET_SIZE - 
				mTweetEditText.getText().length() ) );

		if(mTweetEditText.getText().length()  > MAX_TWEET_SIZE){
			mCharacterCountView.setTextColor(Color.RED);
			mSubmitButton.setEnabled(false);
		}
		else{
			mCharacterCountView.setTextColor(Color.WHITE);
			mSubmitButton.setEnabled(true);
		}

		//Listen for changes in tweet text..
		mTweetEditText.addTextChangedListener(new MyTextWatcher());
	}

	private void setupMentionCompletion() {
		//Setup the completion for '@'
		Cursor cursor = mApplication.getStatusData().getFriends(mAccountId);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				R.layout.people_list_item,cursor, FROM, TO, 0);


		adapter.setCursorToStringConverter(new CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				Log.i(TAG, "Converting Cursor String");
				int desiredColumn = 4;//screen name in StatusData.FOLLOWERS_TABLE
				return cursor.getString(desiredColumn);
			}
		});

		//Filter as we type
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			@Override
			public Cursor runQuery(CharSequence constraint) {
				String where = StatusData.C_SCREEN_NAME + " LIKE '" + constraint + "%'";
				return mApplication.getStatusData().getFriends(mAccountId,where);
			}
		});


		adapter.setViewBinder(mViewBinder);

		mTweetEditText.setAdapter(adapter);

		mTweetEditText.setThreshold(1);

		mTweetEditText.setTokenizer(new Tokenizer());
	}

	private void takeVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, LOW_QUALITY_VIDEO);

		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, MAXIMUM_VIDEO_SIZE);  

		startActivityForResult(intent, GET_CAMERA_VIDEO) ;
	}

	private void getGalleryPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(Intent.createChooser(intent,"Select Picture"), 
				GET_GALLERY_IMAGE);
	}

	private void takePhoto(){
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(getActivity())) ); 

		startActivityForResult(intent, GET_CAMERA_IMAGE);
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
		int column_index = cursor
		.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private File getTempFile(Context context){
		//it will return /sdcard/image.tmp
		final File path = new File( Environment.getExternalStorageDirectory(), 
				context.getPackageName() );

		if(!path.exists()) {
			path.mkdir();
		}
		return new File(path, "image.tmp");
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

	private void addToTweet(String text) {
		String currentText = mTweetEditText.getText().toString();

		if(currentText.length() == 0) {
			mTweetEditText.append(text);
		}
		else if( currentText.charAt(currentText.length()-1) == ' ' ) {
			mTweetEditText.append(text);
		}
		else {
			mTweetEditText.append(" " + text);
		}
	}

	private class MyTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			int size = s.length();
			mCharacterCountView.setText(Integer.toString( MAX_TWEET_SIZE - size ) );

			if(size > MAX_TWEET_SIZE){
				mCharacterCountView.setTextColor(Color.RED);
				mSubmitButton.setEnabled(false);
			}
			else{
				mCharacterCountView.setTextColor(Color.WHITE);
				mSubmitButton.setEnabled(true);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	}

	private class UrlShortenTask extends AsyncTask<Void, Void, Void> {
		private String mText;

		private String mUserName;

		private String mKey;

		private boolean has_error = false;

		private ProgressDialog mProgressDialog;

		private Vector<Hyperlink> mLinkList;

		Pattern hyperLinksPattern = 
			Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
		
		public UrlShortenTask() {
			mLinkList = new Vector<Hyperlink>();
		}

		@Override
		protected void onPreExecute() {
			mUserName = getActivity().getResources().getString(R.string.bitlyName);

			mKey = getActivity().getResources().getString(R.string.bitlyKey);

			mProgressDialog = ProgressDialog.show(getActivity(), "", 
					"Shortening urls", true);
			
			mText = mTweetEditText.getText().toString();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try{
				SpannableString linkableText = new SpannableString(mText);

				gatherLinks(linkableText, hyperLinksPattern);

				for(Hyperlink link : mLinkList) {
					String url = link.foundUrl.toString();
					if(!url.startsWith("http://")) {
						url = "http://".concat(url);
					}
					
					link.newUrl = as(mUserName, mKey)
							.call(shorten(url)).getShortUrl();
				}
			}catch(BitlyException e) {
				Log.e(TAG, "Error shortening URL", e);
				has_error = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			mProgressDialog.dismiss();

			if(has_error) {
				Toast.makeText(getActivity(), 
						"Error shortening url", Toast.LENGTH_SHORT).show();
			}
			else {
				for(Hyperlink link : mLinkList) {
					System.err.println("NEW LINK: " + link.newUrl);
					
					if( link.newUrl.startsWith("http://")) {
						link.newUrl = link.newUrl.substring(7);
					}
					
					if( link.newUrl.startsWith("www.")) {
						link.newUrl = link.newUrl.substring(4);
					}
						
					
					mText = mText.replace(link.foundUrl, link.newUrl);
				}
				
				mTweetEditText.setText("");
				
				mTweetEditText.append(mText);
			}
		}

		private final void gatherLinks(Spannable s, Pattern pattern){
			// Matcher matching the pattern
			Matcher m = pattern.matcher(s);

			while (m.find()){
				int start = m.start();
				int end = m.end();

				Hyperlink spec = new Hyperlink();

				spec.foundUrl = s.subSequence(start, end);

				mLinkList.add(spec);
			}
		}

		private class Hyperlink{
			CharSequence foundUrl;
			String newUrl;
		}
	}

	private class MediaUploadTask extends AsyncTask<Void, Void, Void> {

		private File mFile;

		private File mImageFile;

		private Bitmap mBitmap;

		private ProgressDialog mProgressDialog;

		private String mUrl;

		private boolean has_error;

		private int mType;

		public static final int CAMERA_IMG = 0;

		public static final int GALLERY_IMG = 1;

		public static final int CAMERA_VIDEO = 2;

		public MediaUploadTask(int type, Bitmap b) {
			mType = type;
			if(mType == CAMERA_IMG) {
				mBitmap = b;
			}
		}

		public MediaUploadTask(int type, String filepath) {
			mType = type;
			if(mType == GALLERY_IMG) {
				mImageFile = new File(filepath);
			}
			else if(mType == CAMERA_VIDEO) {
				mFile = new File(filepath);

			}
		}


		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(getActivity(), "", 
					"Uploading image. Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			String twitpicKey = getActivity().getResources().getString(R.string.twitpic_key);

			Properties props = new Properties();

			props.put(PropertyConfiguration.MEDIA_PROVIDER,MediaProvider.TWITPIC);
			props.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN,mApplication.getAccessToken("account1").getToken());
			props.put(PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET,mApplication.getAccessToken("account1").getTokenSecret());
			props.put(PropertyConfiguration.OAUTH_CONSUMER_KEY,TTApplication.CONSUMER_KEY);
			props.put(PropertyConfiguration.OAUTH_CONSUMER_SECRET,TTApplication.CONSUMER_SECRET);
			//props.put(PropertyConfiguration.MEDIA_PROVIDER_API_KEY,twitpicKey);

			Configuration conf = new PropertyConfiguration(props);

			ImageUpload iUpload = new ImageUploadFactory(conf).getInstance(MediaProvider.TWITPIC);

			ImageUpload vUpload = new ImageUploadFactory(conf).getInstance(MediaProvider.YFROG);

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
				case CAMERA_VIDEO:
					//Nothing to do, we already have the file..
					break;
			}

			try {
				//mUrl = iUpload.upload(mFile);
				mUrl = vUpload.upload(mFile);

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
				Toast.makeText(getActivity(), "Error occured!", Toast.LENGTH_LONG).show();
				Log.i(TAG, "Error occured!");
			}
			else {
				Log.i(TAG, "Photo at " + mUrl);

				addToTweet(mUrl);

			}
		}
	}

}
