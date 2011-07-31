package com.DGSD.TweeterTweeter.Fragments;

import java.net.MalformedURLException;
import java.util.LinkedList;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.TweetData;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.UI.LinkEnabledTextView;
import com.DGSD.TweeterTweeter.UI.LinkEnabledTextView.TextLinkClickListener;
import com.DGSD.TweeterTweeter.UI.WebViewWithLoading;
import com.DGSD.TweeterTweeter.Utils.StringUtils;
import com.github.droidfu.widgets.WebImageView;

public class TweetFragment extends DialogFragment {

	private TTApplication mApplication;

	private LinkedList<TabHost.TabSpec> mTabSpecs;

	private TabHost mTabs;

	private TextView mRetweetBtn;

	private TextView mReplyBtn;

	private TextView mFavouriteBtn;

	private TextView mShareBtn;

	private TextView mDate;

	private TextView mScreenName;

	private LinkEnabledTextView mText;

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
		View root = inflater.inflate(R.layout.tweet_layout, container, false);

		//Find the tab host
		mTabs = (TabHost)root.findViewById(R.id.tabhost);		

		//Find the control buttons
		mRetweetBtn = (TextView) root.findViewById(R.id.retweet);
		mReplyBtn = (TextView) root.findViewById(R.id.reply);
		mFavouriteBtn = (TextView) root.findViewById(R.id.favourite);
		mShareBtn = (TextView) root.findViewById(R.id.share);

		setupListeners();

		setupTabs();

		mvImage = (WebImageView) root.findViewById(R.id.profile_image);

		mDate = (TextView) root.findViewById(R.id.date);

		mScreenName = (TextView) root.findViewById(R.id.tweet_user);

		mText = (LinkEnabledTextView) root.findViewById(R.id.tweet_text);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		attachData();

	}

	public void attachData() {
		mText.setText(mData.text);

		linkifyText();

		mScreenName.setText(mData.screenName);

		mDate.setText(DateUtils.getRelativeTimeSpanString(getActivity(), 
				Long.valueOf(mData.date)));

		//Set the image view
		mvImage.setImageUrl(mData.img);
		mvImage.setAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.grow_from_bottom));
		mvImage.loadImage();

	}

	private void linkifyText() {
		mText.gatherLinksForText(mData.text + " ",//extra space to stop links at end of text..
				LinkEnabledTextView.MATCH_TWITTER);
		mText.setLinkTextColor(Color.BLUE);

		mText.setOnTextLinkClickListener(new TextLinkClickListener(){
			@Override
			public void onTextLinkClick(View textView, String clickedString) {
				if( clickedString.startsWith("#") ){
					System.err.println("HASHTAG: " + clickedString);
				}
				else if( clickedString.startsWith("@") ){
					System.err.println("PERSON: " + clickedString);
				}
				else{
					System.err.println("WEBSITE!: " + clickedString);
				}

			}
		});

		MovementMethod m = mText.getMovementMethod();
		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			if (mText.getLinksClickable()) {
				mText.setMovementMethod(LinkMovementMethod.getInstance());
				mText.setFocusable(false);
			}
		}

	}

	private void setupTabs() {
		mTabSpecs = new LinkedList<TabHost.TabSpec>();

		mTabs.setup();

		//Add a tab for each web address found
		LinkedList<String> urls = StringUtils.getUrls(mData.text);
		for(String url : urls) {
			TabHost.TabSpec webSpec = mTabs.newTabSpec(url);
			
			webSpec.setContent(new PreExistingViewFactory(
					WebViewWithLoading.getView(getActivity(), url)));
			
			try {
				webSpec.setIndicator(StringUtils.getWebsiteFromUrl(url));
			} catch(MalformedURLException e) {
				e.printStackTrace();
				webSpec.setIndicator("Website");
			}
			
			mTabs.addTab(webSpec);
			mTabSpecs.add(webSpec);
		}


		/*TabHost.TabSpec mediaSpec = mTabs.newTabSpec(null);
		mediaSpec.setContent(R.id.media);
		mediaSpec.setIndicator("Media");
		mTabs.addTab(mediaSpec);
		mTabSpecs.add(mediaSpec);*/
	}

	private void setupListeners() {
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

	/**
	 * Class needed when adding dynamic content to a tab
	 */
	class PreExistingViewFactory implements TabContentFactory{
		private final View preExisting;
		
		protected PreExistingViewFactory(View view){
			preExisting = view;
		}
		
		@Override
		public View createTabContent(String tag) {
			return preExisting;
		}
	}

}
