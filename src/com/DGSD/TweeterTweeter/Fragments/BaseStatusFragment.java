package com.DGSD.TweeterTweeter.Fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView.OnRefreshListener;
import com.DGSD.TweeterTweeter.UI.Adapters.EndlessListAdapter;
import com.DGSD.TweeterTweeter.UI.Adapters.TimelineCursorAdapter;
import com.DGSD.TweeterTweeter.Utils.ListUtils;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseStatusFragment extends BaseFragment {

	private static final String TAG = BaseStatusFragment.class.getSimpleName();

	private static final String RECEIVE_DATA = 
			"com.DGSD.TweeterTweeter.RECEIVE_DATA";

	protected static final String[] FROM = { StatusData.C_CREATED_AT, StatusData.C_SCREEN_NAME,
		StatusData.C_TEXT, StatusData.C_IMG, StatusData.C_ID};

	protected static final int[] TO = {R.id.timeline_date, R.id.timeline_source, R.id.timeline_tweet,
		R.id.timeline_profile_image }; 

	protected PortableReceiver mReceiver;

	protected IntentFilter mDataFilter;

	protected IntentFilter mNoDataFilter;

	protected IntentFilter mErrorFilter;

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
		View root = inflater.inflate(R.layout.list_fragment_layout, container, false);

		mListView = (PullToRefreshListView) root.findViewById(R.id.list);

		if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
			mCurrentTask.cancel(true);
		}

		mCurrentTask = new DataLoadingTask(BaseStatusFragment.this, DataLoadingTask.CURRENT);
		mCurrentTask.execute();

		Log.i(TAG, "Returning root from onCreateView");

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		((PullToRefreshListView)mListView).setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.i(TAG, "STARTING REFRESH!");
				if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
					mCurrentTask.cancel(true);
				}

				mCurrentTask = new DataLoadingTask(BaseStatusFragment.this, DataLoadingTask.NEWEST);
				mCurrentTask.execute();
			}
		});

		((PullToRefreshListView)mListView).setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if(mAdapter != null && (totalItemCount != visibleItemCount)) {
					((EndlessListAdapter)mAdapter).setKeepApending(true);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, 
					int pos, long id) {

				mCurrentActionMode = getActivity().startActionMode(
						new StatusCallback(pos - 1));
				
				return true;
			}

		});

		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int pos, long id) {
				//Show the view
				View container = getActivity().findViewById(R.id.secondary_container);
				container.setVisibility(View.VISIBLE);
				
				//Insert the fragment!
				getFragmentManager().beginTransaction()
									.replace(R.id.secondary_container, 
											TweetFragment.newInstance(StatusData.getStatus(mCursor)))
										.addToBackStack(null)
										.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
										.commit();
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

		if(mCurrentActionMode != null) {
			mCurrentActionMode.finish();
			mCurrentActionMode = null;
		}
		
		mAdapter = null;
	}

	@Override
	public void appendData() {
		if(mListView == null || mCursor == null) {
			Log.i(TAG, "Listview/cursor was null!");
			return;
		}

		if(mCursor.getCount() == 0) {
			((PullToRefreshListView)mListView).refresh();
		}

		if(mAdapter == null) {
			TimelineCursorAdapter tca = new TimelineCursorAdapter(getActivity(), R.layout.timeline_list_item, 
					mCursor, FROM, TO);

			mAdapter = new EndlessListAdapter(BaseStatusFragment.this, tca);
		}

		if(mListView.getAdapter() == null) {
			Log.i(TAG, "ADAPTER WAS NULL! SETTING IT!");
			mListView.setAdapter(mAdapter);
		} else {
			Log.i(TAG, "REFRESHING CURSOR");
			((TimelineCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).changeCursor(mCursor);
			((TimelineCursorAdapter)((EndlessListAdapter)mAdapter).getAdapter()).notifyDataSetChanged();
		}

		if(((PullToRefreshListView)mListView).isRefreshing()) {
			((PullToRefreshListView)mListView).onRefreshComplete();
		}
	}

	private void startRefresh(int type, String account) {
		if(mType == type && account != null && mAccountId.equals(account)) {
			if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
				mCurrentTask.cancel(true);
			}

			mCurrentTask = new DataLoadingTask(BaseStatusFragment.this, DataLoadingTask.CURRENT);
			mCurrentTask.execute();
		} else {
			Log.i(TAG, "Received Irrelevant broadcast: " 
					+ type + "(My type=" + mType + ")");
		}
	}

	private void stopRefresh(int type, String account) {
		if(mType == type && account != null && mAccountId.equals(account)) {
			if(((PullToRefreshListView)mListView).isRefreshing()) {
				((PullToRefreshListView)mListView).onRefreshComplete();
			}
		} else {
			Log.i(TAG, "Received Irrelevant broadcast - TYPE: " 
					+ type + " ACCOUNT: " + account 
					+ "(My type=" + mType + " My Account = " + mAccountId + ")");
		}
	}

	private class StatusCallback implements ActionMode.Callback {

		private String mTweetId;

		private String mScreenName;

		private String mTweetText;
		
		final String[] userEntities;

		private boolean hasError;

		public StatusCallback(int pos) {
			hasError = false;

			mTweetId = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_ID, pos);

			mScreenName = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_SCREEN_NAME, pos);

			mTweetText = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_TEXT, pos);
			
			userEntities = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_USER_ENT, pos).split(",");

			if(mTweetId == "" || mTweetId == null) {
				//We couldn't get a tweet id :(
				Toast.makeText(getActivity(), 
						"Error getting data for tweet", Toast.LENGTH_SHORT).show();

				hasError = true;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
			if(hasError) {
				return false;
			} else {
				actionMode.setTitle(mScreenName + "'s tweet");

				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.status_context_menu, menu);
				return true;
			}
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

			boolean handled = false;
			switch (menuItem.getItemId()) {
				case R.id.menu_reply:
					if(userEntities.length > 0 && userEntities[0].length() > 0) {
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
										val = "@" + mScreenName;
										break;
									case 1:
										//Reply to all
										val = "@" + mScreenName;
										for(String s : userEntities) {
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
						//There are no other mentioned users, lets just reply
						NewTweetFragment.newInstance(mApplication.getSelectedAccount(), 
								"@" + mScreenName + " ").show(getActivity().getFragmentManager(), null);
					}

					handled = true;
					break;

				case R.id.menu_retweet:
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
											"RT " + mTweetText).show(getActivity().getFragmentManager(), null);
									break;
							}
						}
					});

					builder.create().show();

					handled = true;
					break;

				case R.id.menu_favourite:
					System.err.println("FAVOURITE FOR: " + mTweetId);
					Intent intent = new Intent(getActivity(), UpdaterService.class);
					intent.putExtra(UpdaterService.DATA_TYPE, UpdaterService.DATATYPES.NEW_FAVOURITE);
					intent.putExtra(UpdaterService.ACCOUNT, mAccountId);
					intent.putExtra(UpdaterService.TWEETID, mTweetId);
					getActivity().startService(intent);

					handled = true;
					break;

				case R.id.menu_share:
					Intent sharingIntent = new Intent(Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mTweetText);
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Tweet by " + mScreenName);
					getActivity().startActivity(Intent.createChooser(sharingIntent, "Share tweet"));

					handled = true;
					break;
			}

			actionMode.finish();

			return handled;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			mCurrentActionMode = null;
		}

	}
}
