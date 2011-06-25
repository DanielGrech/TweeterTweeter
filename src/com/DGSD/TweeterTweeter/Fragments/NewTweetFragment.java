package com.DGSD.TweeterTweeter.Fragments;

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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.github.droidfu.widgets.WebImageView;

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

	public static final int MAX_TWEET_SIZE = 140;

	private TTApplication mApplication;

	private String mAccountId;

	private ViewGroup mAccountContainer;

	private MultiAutoCompleteTextView mTweetEditText;

	private TextView mCharacterCountView;

	private Button mShortenUrlButton;

	private Button mMediaButton;

	private Button mLocationButton;

	private Button mSubmitButton;

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
						new MediaUploadTask(captureBmp).execute();
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

					new MediaUploadTask(getPath(imageUri)).execute();
				}
				else {
					Log.i(TAG, "Picture not taken!");
				}
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.new_tweet_url_shorten:
				Log.i(TAG, "Url Shorten Button!!");
				break;

			case R.id.new_tweet_media:
				final CharSequence[] choices = {"Camera Picture", "Gallery image", 
						"Camera Video", "Gallery Video"};

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

	private static class Tokenizer implements MultiAutoCompleteTextView.Tokenizer {
		@Override
		public int findTokenEnd(CharSequence text, int cursor) {
			Log.i(TAG, "findTokenEnd: " + text.toString());

			int i = cursor;
			int len = text.length();

			while (i < len) {
				if (text.charAt(i) == ' ' || text.charAt(i) == '@') {
					return i;
				} else {
					i++;
				}
			}
			return len;
		}

		@Override
		public int findTokenStart(CharSequence text, int cursor) {
			Log.i(TAG, "findTokenStart: " + text.toString());
			int i = cursor;

			while (i > 0 && text.charAt(i - 1) != '@') {
				i--;
			}

			return i;
		}

		@Override
		public CharSequence terminateToken(CharSequence text) {
			int i = text.length();

			Log.i(TAG, "Terminate Token: " + text.toString());

			while (i > 0 && text.charAt(i - 1) == ' ') {
				i--;
			}

			if (i > 0 && text.charAt(0) == '@') {
				return text;
			} else {
				if (text instanceof Spanned) {
					SpannableString sp = new SpannableString(text+" ");
					TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
							Object.class, sp, 0);
					return sp;
				} else {
					return text + " ";
				}
			}
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

		private static final int CAMERA_IMG = 0;

		private static final int GALLERY_IMG = 1;

		public MediaUploadTask(Bitmap b) {
			mBitmap = b;
			mType = CAMERA_IMG;
		}

		public MediaUploadTask(String filepath) {
			mImageFile = new File(filepath);
			mType = GALLERY_IMG;
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
			props.put(PropertyConfiguration.MEDIA_PROVIDER_API_KEY,twitpicKey);

			Configuration conf = new PropertyConfiguration(props);

			ImageUpload upload = new ImageUploadFactory(conf).getInstance(MediaProvider.TWITPIC);

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
				mUrl = upload.upload(mFile);

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
				if(mUrl.startsWith("http://")) {
					mUrl = mUrl.substring(7);
				}

				//Add the url to the tweet
				String currentText = mTweetEditText.getText().toString();

				if(currentText.length() == 0) {
					currentText += mUrl;
				}
				else if( currentText.charAt(currentText.length()-1) == ' ' ) {
					currentText += mUrl;
				}
				else {
					currentText += " " + mUrl;
				}

				mTweetEditText.setText(currentText);
			}
		}
	}

}
