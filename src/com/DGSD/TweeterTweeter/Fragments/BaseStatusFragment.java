package com.DGSD.TweeterTweeter.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.EndlessListAdapter;
import com.DGSD.TweeterTweeter.UI.QuickAction;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.DGSD.TweeterTweeter.Utils.QuickActionUtils;
import com.github.droidfu.widgets.WebImageView;

public abstract class BaseStatusFragment extends BaseFragment {

	private static final String TAG = BaseStatusFragment.class.getSimpleName();

	private static final String RECEIVE_DATA = 
		"com.DGSD.TweeterTweeter.RECEIVE_DATA";

	protected static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_SCREEN_NAME,
		StatusData.C_TEXT, StatusData.C_IMG, StatusData.C_ID};

	protected static final int[] TO = {R.id.timeline_date, R.id.timeline_source, R.id.timeline_tweet,
		R.id.timeline_profile_image }; 

	protected QuickAction mQuickAction;

	protected PortableReceiver mReceiver;

	protected IntentFilter mDataFilter;

	protected IntentFilter mNoDataFilter;

	protected IntentFilter mErrorFilter;

	//Adjust data from database for display
	protected static final ViewBinder mViewBinder = new ViewBinder() { 

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			switch(view.getId()){
				case R.id.timeline_date:
					try{
						((TextView)view).setText( 
								DateUtils.getRelativeTimeSpanString(view.getContext(), 
										Long.valueOf( cursor.getString(columnIndex) )) );

					} catch(NumberFormatException e){
						Log.e(TAG, "Error converting time string", e);
					} catch(ClassCastException e) {
						Log.e(TAG, "Error casting to textview", e);
					}

					return true;

				case R.id.timeline_profile_image:
					String url = "";
					url = cursor.getString(columnIndex);

					((WebImageView) view).setImageUrl(url);
					if(url != "") {
						((WebImageView) view).loadImage();
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

		mReceiver = new PortableReceiver();

		mReceiver.setReceiver(new Receiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int dataType = intent.getIntExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.ALL_DATA);

				String account = intent.getStringExtra(UpdaterService.ACCOUNT);

				if(intent.getAction().equals(UpdaterService.SEND_DATA)) {
					Log.v(TAG, "Data Received");
					startRefresh(dataType, account);
				}
				else if(intent.getAction().equals(UpdaterService.NO_DATA)) {
					Log.v(TAG, "No Data Received");
					stopRefresh(dataType, account);
				} 
				else if(intent.getAction().equals(UpdaterService.ERROR)) {
					if(mType == dataType && account != null && mAccountId.equals(account)) {
						Toast.makeText(getActivity(), "Error refreshing data", 
								Toast.LENGTH_SHORT).show();
					}
					stopRefresh(dataType, account);
				}
				else {
					Log.v(TAG, "Received Mystery Intent: " + intent.getAction());
				}
			}

		});

		mDataFilter = new IntentFilter(UpdaterService.SEND_DATA);

		mNoDataFilter = new IntentFilter(UpdaterService.NO_DATA);

		mErrorFilter = new IntentFilter(UpdaterService.ERROR);
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
				String tweet_id = QuickActionUtils.getTweetProperty(mCursor, 
						StatusData.C_ID, pos-1);

				if(tweet_id == "") {
					//We couldn't get a tweet id :(
					Toast.makeText(getActivity(), 
							"Error getting data for tweet", Toast.LENGTH_SHORT).show();

					return false;
				}

				Log.i(TAG, "TWEET ID: " + tweet_id);

				QuickActionUtils.getTimelineQuickAction(getActivity(), mAccountId, 
						view, mCursor, pos-1).show();

				return false;
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");

		// Register the receiver
		getActivity().registerReceiver(mReceiver, mDataFilter,
				RECEIVE_DATA, null);

		getActivity().registerReceiver(mReceiver, mNoDataFilter,
				RECEIVE_DATA, null);

		getActivity().registerReceiver(mReceiver, mErrorFilter,
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

		if(mCursor != null) {
			mCursor.close();
			mCursor = null;
		}

		mAdapter = null;
	}

	@Override
	public void appendData() {
		if(mCursor.getCount() == 0) {
			mListView.refresh();
		}

		if(mAdapter == null) {
			SimpleCursorAdapter sca = new SimpleCursorAdapter(getActivity(), R.layout.timeline_list_item, 
					mCursor, FROM, TO, 0);
			
			sca.setViewBinder(mViewBinder);
			
			mAdapter = new EndlessListAdapter(BaseStatusFragment.this, sca);
		}
		
		if(mListView.getAdapter() == null) {
			Log.i(TAG, "ADAPTER WAS NULL! SETTING IT!");
			mListView.setAdapter(mAdapter);
		} else {
			Log.i(TAG, "REFRESHING CURSOR");
			((SimpleCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).changeCursor(mCursor);
			((SimpleCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).notifyDataSetChanged();
		}
		
		if(mListView.isRefreshing()) {
			mListView.onRefreshComplete();
		}
	}
	
	private void startRefresh(int type, String account) {
		if(mType == type && account != null && mAccountId.equals(account)) {
			new DataLoadingTask(BaseStatusFragment.this, DataLoadingTask.NEWEST).execute();
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
			Log.i(TAG, "Received Irrelevant broadcast - TYPE: " 
					+ type + " ACCOUNT: " + account 
					+ "(My type=" + mType + " My Account = " + mAccountId + ")");
		}
	}
}
