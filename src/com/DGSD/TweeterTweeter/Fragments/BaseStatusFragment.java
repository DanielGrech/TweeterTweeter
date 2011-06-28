package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.UI.ActionItem;
import com.DGSD.TweeterTweeter.UI.QuickAction;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.github.droidfu.widgets.WebImageView;

public abstract class BaseStatusFragment extends BaseFragment {

	private static final String TAG = BaseStatusFragment.class.getSimpleName();

	private static final String RECEIVE_DATA = 
		"com.DGSD.TweeterTweeter.RECEIVE_DATA";

	protected static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_USER,
		StatusData.C_TEXT, StatusData.C_IMG, StatusData.C_FAV };

	protected static final int[] TO = {R.id.timeline_date, R.id.timeline_source, R.id.timeline_tweet,
		R.id.timeline_profile_image, R.id.timeline_favourite_star }; 

	private static final int TWEET_ID_COLUMN = 1;

	protected QuickAction mQuickAction;

	protected final ActionItem mReplyAction = new ActionItem();

	protected final ActionItem mRetweetAction = new ActionItem();

	protected final ActionItem mFavouriteAction = new ActionItem();

	protected final ActionItem mShareAction = new ActionItem();

	protected Cursor mCursor;

	protected ResponseList<Status> mDataList;

	protected PortableReceiver mReceiver;

	protected IntentFilter mDataFilter;

	protected IntentFilter mNoDataFilter;

	//Adjust data from database for display
	protected static final ViewBinder mViewBinder = new ViewBinder() { 

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

			switch(view.getId()){
				case R.id.timeline_date:
					long timestamp = -1;
					try{
						timestamp = Long.valueOf( cursor.getString(columnIndex) );
					}catch(NumberFormatException e){
						Log.e(TAG, "Error converting time string", e);
					}

					((TextView)view).setText( 
							DateUtils.getRelativeTimeSpanString(view.getContext(), timestamp) );

					return true;

				case R.id.timeline_favourite_star:
					if(cursor.getInt(columnIndex) == 1) {
						view.setVisibility(View.VISIBLE);
					}
					else {
						view.setVisibility(View.GONE);
					}

					return true;

				case R.id.timeline_profile_image:
					String url = "";
					url = cursor.getString(columnIndex);

					WebImageView wiv = (WebImageView) view;

					wiv.setImageUrl(url);
					if(url != "") {
						wiv.loadImage();
					}

					return true;
			}

			return false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		setRetainInstance(true);

		createPopupActions();

		mReceiver = new PortableReceiver();

		mReceiver.setReceiver(new Receiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.v(TAG, "Received a Update Broadcast!");
				
				int dataType = intent.getIntExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.ALL_DATA);
				
				String account = intent.getStringExtra(UpdaterService.ACCOUNT);
				
				if(intent.getAction().equals(UpdaterService.SEND_DATA)) {
					Log.v(TAG, "Data Received");
					startRefresh(dataType, account);
				}
				else if (intent.getAction().equals(UpdaterService.NO_DATA)) {
					Log.v(TAG, "No Data Received");
					stopRefresh(dataType, account);
				}
				else {
					Log.v(TAG, "Received Myster Intent: " + intent.getAction());
				}
			}

		});

		mDataFilter = new IntentFilter(UpdaterService.SEND_DATA);

		mNoDataFilter = new IntentFilter(UpdaterService.NO_DATA);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, 
					int pos, long id) {

				//Get available tweet data..
				/*
				 * TODO: Should just return a whole data structure with all details possible
				 */
				long tweet_id = getTweetId(pos-1);

				if(tweet_id == -1) {
					//We couldn't get a tweet id :(
					Toast.makeText(getActivity(), 
							"Error getting data for tweet", Toast.LENGTH_SHORT).show();

					return false;
				}

				Log.i(TAG, "TWEET ID: " + tweet_id);

				//Set the listeners for each item
				/*
				 * TODO: Set OnClickListeners for each ActionItem
				 */

				//Add action items to the popup and display!
				mQuickAction = new QuickAction(view);

				mQuickAction.addActionItem(mReplyAction);
				mQuickAction.addActionItem(mRetweetAction);
				mQuickAction.addActionItem(mFavouriteAction);
				mQuickAction.addActionItem(mShareAction);
				mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

				mQuickAction.show();

				return false;
			}

		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");
		try {
			setupList();
		} catch (TwitterException e) {
			Log.e(TAG, "Error resuming list", e);
		}

		// Register the receiver
		getActivity().registerReceiver(mReceiver, mDataFilter,
				RECEIVE_DATA, null);

		getActivity().registerReceiver(mReceiver, mNoDataFilter,
				RECEIVE_DATA, null);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause()");
		getActivity().unregisterReceiver(mReceiver); 
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		Log.v(TAG, "onDestroyView()");

		mCursor = null;

		mAdapter = null;
	}
	
	@Override
	public void postSetup(boolean isUpdate) {
		if(isUpdate) {
			((SimpleCursorAdapter)mAdapter).changeCursor(mCursor);
			((SimpleCursorAdapter)mAdapter).notifyDataSetChanged();
		}
		else {
			mListView.setAdapter(mAdapter);
		}
	}

	private void startRefresh(int type, String account) {
		if(mType == type && account != null && mAccountId.equals(account)) {
			new DataLoadingTask(true).execute();
		} else {
			Log.i(TAG, "Received Irrelevant broadcast: " 
					+ type + "(My type=" + mType + ")");
		}
	}
	
	private void stopRefresh(int type, String account) {
		if(mType == type && account != null && mAccountId.equals(account)) {
    		if(mListView.isRefreshing()) {
    			mListView.onRefreshComplete();
    		}
		} else {
			Log.i(TAG, "Received Irrelevant broadcast: " 
					+ type + "(My type=" + mType + ")");
		}
	}
	
	private long getTweetId(int pos) {
		long retval = -1;

		try{
			if(mCursor != null && mCursor.moveToPosition(pos)) {
				retval = Long.valueOf(mCursor.getString(TWEET_ID_COLUMN));
			}
			else if(mDataList != null) {
				retval = mDataList.get(pos).getId();
			}
			else {
				Log.d(TAG,"No Tweet at position: " + pos);
			}
		}catch(RuntimeException e) {
			Log.e(TAG, "Error getting tweet id", e);
		}

		return retval;
	}

	private void createPopupActions(){
		Resources res = getActivity().getResources();

		//mReplyAction.setTitle("Reply");
		mReplyAction.setIcon(res.getDrawable(R.drawable.reply));

		//mRetweetAction.setTitle("Retweet");
		mRetweetAction.setIcon(res.getDrawable(R.drawable.retweet));

		//mFavouriteAction.setTitle("Favourite");
		mFavouriteAction.setIcon(res.getDrawable(R.drawable.favourite));

		//mShareAction.setTitle("Share");
		mShareAction.setIcon(res.getDrawable(R.drawable.share));
	}
}
