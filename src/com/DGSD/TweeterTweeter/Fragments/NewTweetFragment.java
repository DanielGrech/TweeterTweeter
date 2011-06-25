package com.DGSD.TweeterTweeter.Fragments;

import java.io.File;
import java.io.FileOutputStream;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class NewTweetFragment extends DialogFragment 
implements OnClickListener {

	public static final String TAG = NewTweetFragment.class.getSimpleName();

	private static final int GET_CAMERA_IMAGE = 0;

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == GET_CAMERA_IMAGE) {
			if (resultCode == Activity.RESULT_OK) {
				Log.i(TAG, "Picture taken!");
				Bitmap b = (Bitmap) data.getExtras().get("data");
				
				new MediaUploadTask(b).execute();

			}
			else {
				Log.i(TAG, "Picture not taken!");
			}
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

						if(item == 0) { //Camera picture..
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							startActivityForResult(intent, GET_CAMERA_IMAGE);
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

		private Bitmap mBitmap;
		
		private String mUrl;
		
		private boolean has_error;

		public MediaUploadTask(Bitmap b) {
			mBitmap = b;
		}

		@Override
		protected void onPreExecute() {

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

			try {
				Log.i(TAG, "About to compress file");
				mFile = File.createTempFile(Integer.toString(new Random().nextInt()), "jpg");
				FileOutputStream out = new FileOutputStream(mFile);
				mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			} catch(Exception e){
				Log.e(TAG, "Error saving image", e);
				has_error = true;
				return null;
			}
			
			Log.i(TAG, "finished compressing file");
			
			try {
				Log.i(TAG, "About to upload file");
				mUrl = upload.upload(mFile);
				Log.i(TAG, "Done uploading file!");
			} catch (TwitterException e) {
				Log.e(TAG, "Error uploading image", e);
				has_error = true;
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if(has_error) {
				Toast.makeText(getActivity(), "Error occured!", Toast.LENGTH_SHORT);
				Log.i(TAG, "Error occured!");
			}
			else {
				Toast.makeText(getActivity(), "Photo at: " + mUrl, Toast.LENGTH_SHORT);
				Log.i(TAG, "Photo at " + mUrl);
			}
		}
	}

}
