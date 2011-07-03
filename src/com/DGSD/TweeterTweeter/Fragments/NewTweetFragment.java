package com.DGSD.TweeterTweeter.Fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Services.NewStatusService;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.DGSD.TweeterTweeter.Utils.MediaUploadTask;
import com.DGSD.TweeterTweeter.Utils.Tokenizer;
import com.DGSD.TweeterTweeter.Utils.UrlShortenTask;
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
	
	private long mLatitude = -1;
	
	private long mLongitude = -1;

	protected static final String[] FROM = 
	{ StatusData.C_SCREEN_NAME, StatusData.C_IMG, StatusData.C_ID };

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

	public static NewTweetFragment newInstance(String tweetText){
		NewTweetFragment f = new NewTweetFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		
		args.putString("tweet", tweetText);

		f.setArguments(args);

		return f;
	}
	
	
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
		
		addToTweet(mTweetEditText, getArguments().getString("tweet"));
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
						
						new MediaUploadTask(getActivity(), mTweetEditText, 
								MediaUploadTask.CAMERA_IMG,	captureBmp).execute();
						
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

					new MediaUploadTask(getActivity(), mTweetEditText,
							MediaUploadTask.GALLERY_IMG, getPath(imageUri)).execute();
				}
				else {
					Log.i(TAG, "Picture not chosen!");
				}
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.new_tweet_url_shorten:
				new UrlShortenTask(getActivity(), mTweetEditText ).execute();
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
		Cursor cursor = mApplication.getStatusData().getFriends(mAccountId, FROM);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), 
				R.layout.people_list_item,cursor, FROM, TO, 0);


		adapter.setCursorToStringConverter(new CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				Log.i(TAG, "Converting Cursor String");
				return cursor.getString(cursor.getColumnIndex(StatusData.C_SCREEN_NAME));
			}
		});

		//Filter as we type
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			@Override
			public Cursor runQuery(CharSequence constraint) {
				String where = StatusData.C_SCREEN_NAME + " LIKE \"" + constraint + "%\"";
				return mApplication.getStatusData().getFriends(mAccountId, where, FROM);
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

	public static void addToTweet(TextView tv, String text) {
		if(text == null) {
			return;
		}
		
		String currentText = tv.getText().toString();

		if(currentText.length() == 0) {
			tv.append(text);
		}
		else if( currentText.charAt(currentText.length()-1) == ' ' ) {
			tv.append(text);
		}
		else {
			tv.append(" " + text);
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
}
