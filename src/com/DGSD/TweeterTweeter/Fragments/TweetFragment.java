package com.DGSD.TweeterTweeter.Fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.TweetData;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.github.droidfu.widgets.WebImageView;

public class TweetFragment extends DialogFragment {

	private TTApplication mApplication;

	private TextView mRetweetBtn;

	private TextView mReplyBtn;

	private TextView mFavouriteBtn;

	private TextView mShareBtn;

	private TextView mDate;

	private TextView mScreenName;

	private TextView mText;

	private WebImageView mvImage;

	private TweetData mData;

	public static TweetFragment newInstance(ContentValues statusVals) {
		TweetFragment f = new TweetFragment();

		Bundle args = new Bundle();

		args.putParcelable("values", statusVals);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (TTApplication) getActivity().getApplication();

		mData = new TweetData((ContentValues)getArguments().getParcelable("values"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View root =  inflater.inflate(R.layout.tweet_layout, container, false);

		//Find the control buttons
		mRetweetBtn = (TextView) root.findViewById(R.id.retweet);
		mReplyBtn = (TextView) root.findViewById(R.id.reply);
		mFavouriteBtn = (TextView) root.findViewById(R.id.favourite);
		mShareBtn = (TextView) root.findViewById(R.id.share);

		setupListeners();


		mvImage = (WebImageView) root.findViewById(R.id.profile_image);

		mDate = (TextView) root.findViewById(R.id.date);

		mScreenName = (TextView) root.findViewById(R.id.tweet_user);

		mText = (TextView) root.findViewById(R.id.tweet_text);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		attachData();

	}

	public void attachData() {
		mText.setText(mData.text);

		mScreenName.setText(mData.screenName);

		mDate.setText(DateUtils.getRelativeTimeSpanString(getActivity(), 
				Long.valueOf(mData.date)));

		//Set the image view
		mvImage.setImageUrl(mData.img);
		mvImage.setAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.grow_from_bottom));
		mvImage.loadImage();
	}

	public void setupListeners() {
		mReplyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final String[] userEnt = mData.userEnt.split(",");

				if(userEnt.length > 0 && userEnt[0].length() > 0) {
					//We might want to reply to all mentioned users?
					final CharSequence[] choices = {"Reply", "Reply to all"};

					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Reply");

					builder.setItems(choices, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							String val = "";
							switch(item) {
								case 0: 
									//Reply
									val = "@" + mData.screenName;
									break;
								case 1:
									//Reply to all
									val = "@" + mData.screenName;
									for(String s : userEnt) {
										if(s.length() > 0) {
											val += " @" + s;
										}
									}
									break;
							}

							NewTweetFragment.newInstance(mApplication.getSelectedAccount(),
									val + " ").show(getActivity().getFragmentManager(), null);
						}
					});
					
					builder.create().show();
				} else {
					NewTweetFragment.newInstance(mApplication.getSelectedAccount(),
							"@" + mData.screenName + " ").show(getActivity().getFragmentManager(), null);
				}
			}
		});

		mRetweetBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				final CharSequence[] choices = {"Retweet", "Retweet and edit"};

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Retweet");

				builder.setItems(choices, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item) {
							case 0: 
								Toast.makeText(getActivity(), "Retweet!", 
										Toast.LENGTH_SHORT).show();
								break;
							case 1:
								//Retweet & edit
								NewTweetFragment.newInstance(mApplication.getSelectedAccount(),
										"RT " + mData.text).show(getActivity().getFragmentManager(), null);
								break;
						}
					}
				});

				builder.create().show();
			}
		});

		mFavouriteBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				System.err.println("FAVOURITE FOR: " + mData.id);
				Intent intent = new Intent(getActivity(), UpdaterService.class);
				intent.putExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.NEW_FAVOURITE);
				intent.putExtra(UpdaterService.ACCOUNT, mData.account);
				intent.putExtra(UpdaterService.TWEETID, mData.id);
				getActivity().startService(intent);
			}
		});

		mShareBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mData.text);
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tweet by " + mData.screenName);
				getActivity().startActivity(Intent.createChooser(sharingIntent, "Share tweet"));

			}
		});
	}

}