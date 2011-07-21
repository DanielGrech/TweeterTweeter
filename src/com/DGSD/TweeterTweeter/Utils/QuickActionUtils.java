package com.DGSD.TweeterTweeter.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Fragments.NewTweetFragment;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.UI.ActionItem;
import com.DGSD.TweeterTweeter.UI.QuickAction;

public class QuickActionUtils {
	private static final String TAG = QuickActionUtils.class.getSimpleName();
	
	private static final ActionItem mReplyAction = new ActionItem();

	private static final ActionItem mRetweetAction = new ActionItem();

	private static final ActionItem mFavouriteAction = new ActionItem();

	private static final ActionItem mShareAction = new ActionItem();
	
	public static QuickAction getTimelineQuickAction(Activity a, String account, 
			View view, Cursor mCursor, int pos) {
		QuickAction mQuickAction = new QuickAction(view);

		Resources res = view.getContext().getResources();
		
		mReplyAction.setIcon(res.getDrawable(R.drawable.reply));
		mRetweetAction.setIcon(res.getDrawable(R.drawable.retweet));
		mFavouriteAction.setIcon(res.getDrawable(R.drawable.favourite));
		mShareAction.setIcon(res.getDrawable(R.drawable.share));
		
		
		mQuickAction.addActionItem(mReplyAction);
		mQuickAction.addActionItem(mRetweetAction);
		mQuickAction.addActionItem(mFavouriteAction);
		mQuickAction.addActionItem(mShareAction);
		mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

		setupTimelineTweet(a, mQuickAction, account, mCursor, pos);
		
		return mQuickAction;
	}
	
	public static String getTweetProperty(Cursor mCursor, String column, int pos) {
		String retval = "";

		try{
			if(mCursor != null && mCursor.moveToPosition(pos)) {
				retval = mCursor.getString(mCursor.getColumnIndex(column));
			} else {
				Log.d(TAG,"No Tweet at position: " + pos);
			}
		}catch(RuntimeException e) {
			Log.e(TAG, "Error getting tweet id", e);
		}

		return retval;
	}
	
	public static void setupTimelineTweet(final Activity activity, 
			final QuickAction mQuickAction, final String mAccountId, 
			final Cursor mCursor, final int pos) {
		
		final String screenName = getTweetProperty(mCursor, StatusData.C_SCREEN_NAME, pos);
		final String tweetid = getTweetProperty(mCursor, StatusData.C_ID, pos);
		final String tweetText = getTweetProperty(mCursor, StatusData.C_TEXT, pos);
		final String[] userEntities = getTweetProperty(mCursor, StatusData.C_USER_ENT, pos).split(",");
		final TTApplication app = (TTApplication) activity.getApplication();
		
		mFavouriteAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				System.err.println("FAVOURITE FOR: " + tweetid);
				Intent intent = new Intent(activity, UpdaterService.class);
				intent.putExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.NEW_FAVOURITE);
				intent.putExtra(UpdaterService.ACCOUNT, mAccountId);
				intent.putExtra(UpdaterService.TWEETID, tweetid);
				activity.startService(intent);

				if(mQuickAction != null) {
					mQuickAction.dismiss();
				}
			}
		});

		mShareAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tweetText);
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tweet by " + screenName);
				activity.startActivity(Intent.createChooser(sharingIntent, "Share tweet"));

				if(mQuickAction != null) {
					mQuickAction.dismiss();
				}
			}
		});

		mReplyAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(userEntities.length > 0 && userEntities[0].length() > 0) {
					//We might want to reply to all mentioned users?
					final CharSequence[] choices = {"Reply", "Reply to all"};

					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					builder.setTitle("Reply");

					builder.setItems(choices, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							String val = "";
							switch(item) {
								case 0: 
									//Reply
									val = "@" + screenName;
									break;
								case 1:
									//Reply to all
									val = "@" + screenName;
									for(String s : userEntities) {
										if(s.length() > 0) {
											val += " @" + s;
										}
									}
									break;
							}

							activity.getFragmentManager()
        							.beginTransaction()
        							.replace(R.id.container, 
        									NewTweetFragment.newInstance(app.getSelectedAccount(), 
        											val + " "))
									.addToBackStack(null)
									.commit();

						}
					});

					builder.create().show();
				} else {
					//There are no other mentioned users, lets just reply
					activity.getFragmentManager()
							.beginTransaction()
							.replace(R.id.container, 
									NewTweetFragment.newInstance(app.getSelectedAccount(), 
											"@" + screenName + " "))
							.addToBackStack(null)
							.commit();
				}

				if(mQuickAction != null) {
					mQuickAction.dismiss();
				}
			}

		});

		mRetweetAction.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] choices = {"Retweet", "Retweet and edit"};

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("Retweet");

				builder.setItems(choices, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch(item) {
							case 0: 
								Toast.makeText(activity, "Retweet!", 
										Toast.LENGTH_SHORT).show();
								break;
							case 1:
								//Retweet & edit
								activity.getFragmentManager()
										.beginTransaction()
										.replace(R.id.container, 
												NewTweetFragment.newInstance(app.getSelectedAccount(),
														"RT " + tweetText))
										.addToBackStack(null)
										.commit();
								break;
						}
					}
				});

				builder.create().show();


				if(mQuickAction != null) {
					mQuickAction.dismiss();
				}
			}

		});
	}
}
