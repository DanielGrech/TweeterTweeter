package com.DGSD.TweeterTweeter.Fragments;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Tasks.AddFriendTask;
import com.DGSD.TweeterTweeter.Tasks.BlockUserTask;
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.Tasks.ReportSpamTask;
import com.DGSD.TweeterTweeter.UI.Adapters.PeopleCursorAdapter;
import com.DGSD.TweeterTweeter.Utils.ListUtils;
import com.DGSD.TweeterTweeter.Utils.Log;



public abstract class BasePeopleFragment extends BaseFragment {
	private static final String TAG = BasePeopleFragment.class.getSimpleName();

	private static final String RECEIVE_DATA = 
			"com.DGSD.TweeterTweeter.RECEIVE_DATA";

	protected static final String[] FROM = { StatusData.C_SCREEN_NAME, StatusData.C_IMG, StatusData.C_ID};

	protected static final int[] TO = {R.id.screen_name, R.id.profile_image}; 

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
					if(mType == dataType && account != null && 
							mAccountId.equals(account)) {
						mCurrentTask = new DataLoadingTask(BasePeopleFragment.this, 
								DataLoadingTask.CURRENT);
						mCurrentTask.execute();
					}
				}
				else if(intent.getAction().equals(UpdaterService.NO_DATA)) {
					Log.v(TAG, "No Data Received");
				} 
				else if(intent.getAction().equals(UpdaterService.ERROR)) {
					if(mType == dataType && account != null && mAccountId.equals(account)) {
						Toast.makeText(getActivity(), "Error refreshing data", 
								Toast.LENGTH_SHORT).show();
					}
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
		View root = inflater.inflate(R.layout.list_fragment_layout_people, container, false);

		mListView = (ListView) root.findViewById(R.id.list);

		if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
			mCurrentTask.cancel(true);
		}

		if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
			mCurrentTask.cancel(true);
		}

		mCurrentTask = new DataLoadingTask(BasePeopleFragment.this, DataLoadingTask.CURRENT);
		mCurrentTask.execute();

		Log.i(TAG, "Returning root from onCreateView");

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstance) {
		super.onActivityCreated(savedInstance);

		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, 
					int pos, long id) {

				mCurrentActionMode = getActivity().startActionMode(
						new PeopleCallback(pos));

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
		if(mListView == null || mCursor == null) {
			Log.i(TAG, "Listview/cursor was null!");
			return;
		}

		if(mCursor.getCount() == 0) {
			if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
				mCurrentTask.cancel(true);
			}

			mCurrentTask = new DataLoadingTask(BasePeopleFragment.this, 
					DataLoadingTask.NEWEST);
			mCurrentTask.execute();
		}

		if(mAdapter == null) {
			mAdapter = new PeopleCursorAdapter(getActivity(), R.layout.people_list_item, 
					mCursor, FROM, TO);
		}

		if(mListView.getAdapter() == null) {
			Log.i(TAG, "ADAPTER WAS NULL! SETTING IT!");
			mListView.setAdapter(mAdapter);
		} else {
			Log.i(TAG, "REFRESHING CURSOR");
			((PeopleCursorAdapter)mAdapter).changeCursor(mCursor);
			((PeopleCursorAdapter)mAdapter).notifyDataSetChanged();
		}
	}
	
	private class PeopleCallback implements ActionMode.Callback {

		private String mPersonId;

		private String mScreenName;
		
		private boolean mIsFriend;
		
		private boolean mIsBlocked;
		
		private boolean hasError;

		public PeopleCallback(int pos) {
			hasError = false;

			mPersonId = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_ID, pos);

			mScreenName = ListUtils.getTweetProperty(mCursor, 
					StatusData.C_SCREEN_NAME, pos);
			
			mIsFriend = mApplication.isFollowing(mApplication.getSelectedAccount(), 
					mPersonId);
			
			//TODO: NEED TO STORE/GET THIS DATA FROM DB!
			mIsBlocked = false;

			if(mPersonId == "" || mPersonId == null) {
				//We couldn't get a mPersonId id :(
				Toast.makeText(getActivity(), 
						"Error getting data for person", Toast.LENGTH_SHORT).show();

				hasError = true;
			}
		}

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
			if(hasError) {
				return false;
			} else {
				actionMode.setTitle(mScreenName);
				
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.people_context_menu, menu);
				
				final MenuItem followerItem = menu.getItem(2);
				
				if(mIsFriend) {
					followerItem.setIcon(R.drawable.icon_unfollow_user);
					followerItem.setTitle("Unfollow");
				} else {
					followerItem.setIcon(R.drawable.ic_menu_invite);
					followerItem.setTitle("Follow");
				}
				
				final MenuItem blockedItem = menu.getItem(1);
				
				if(mIsBlocked) {
					blockedItem.setIcon(R.drawable.ic_menu_cc);
					blockedItem.setTitle("Unblock");
				} else {
					blockedItem.setIcon(R.drawable.ic_menu_blocked_user);
					blockedItem.setTitle("Block");
				}
				
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
				case R.id.menu_mention:
					NewTweetFragment.newInstance(mApplication.getSelectedAccount(), 
							"@" + mScreenName + " ").show(getFragmentManager(), null);
					handled = true;
					break;

				case R.id.menu_block:
					if(mIsBlocked) {
						new BlockUserTask(mApplication, mScreenName, 
								BlockUserTask.UNBLOCK_USER).execute();
					} else {
						new BlockUserTask(mApplication, mScreenName, 
								BlockUserTask.BLOCK_USER).execute();
					}
					
					
					handled = true;
					break;

				case R.id.menu_add:
					//TODO: Need to sync new/old friends with local database!
					if(mIsFriend) {
						new AddFriendTask(mApplication, mScreenName, 
								AddFriendTask.REMOVE_USER).execute();
					} else {
						new AddFriendTask(mApplication, mScreenName, 
								AddFriendTask.ADD_USER).execute();
					}
					
					handled = true;
					break;

				case R.id.menu_report:
					new ReportSpamTask(mApplication, mScreenName).execute();
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
