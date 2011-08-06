package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView.OnRefreshListener;
import com.DGSD.TweeterTweeter.UI.Adapters.BaseViewHolder;
import com.DGSD.TweeterTweeter.UI.Adapters.EndlessListAdapter;
import com.DGSD.TweeterTweeter.Utils.Log;

public abstract class BaseFragment extends DialogFragment {

	private static final String TAG = BaseFragment.class.getSimpleName();

	private static final String RECEIVE_DATA = "com.DGSD.TweeterTweeter.RECEIVE_DATA";

	public static final int ELEMENTS_PER_PAGE = 50;

	protected TTApplication mApplication;

	protected PullToRefreshListView mListView;

	protected SimpleCursorAdapter mWrappedAdapter;

	protected EndlessListAdapter mEndlessAdapter;

	protected String mAccountId;

	protected String mUserName;

	protected String mLastSelectedListItemId;

	protected int mLastVisibileItem;

	protected PortableReceiver mReceiver;

	protected IntentFilter mDataFilter;

	protected IntentFilter mNoDataFilter;

	protected IntentFilter mErrorFilter;

	protected ActionMode mCurrentActionMode;

	protected DataLoadingTask mCurrentTask;

	protected String mCurrentFragmentTag;
	
	boolean mIsPortrait;

	/**
	 * Check if there is any newer data available on twitter
	 * @return true if more data loaded, false otherwise
	 * @throws TwitterException
	 */
	public abstract boolean getNewest() throws TwitterException;

	/**
	 * Get a cursor to the current data in the database
	 * @return A cursor to the items in the database
	 * @throws TwitterException
	 */
	public abstract Cursor getCurrent() throws TwitterException;

	/**
	 * Get some older data than that which is currently loaded
	 * @return true if more data loaded, false otherwise
	 * @throws TwitterException
	 */
	public abstract boolean getOlder() throws TwitterException;


	/**
	 * @return a receiver to be used for getting results from a service
	 */
	public abstract Receiver getReceiver();

	/**
	 * @param pos the position in the list which was clicked
	 * @return a callback relevant to the argument
	 */
	public abstract Callback getCallback(int pos);

	/**
	 * Called when an item in the list is clicked
	 * @param pos the position of the list item which is clicked
	 */
	public abstract void onListItemClick(int pos);

	/**
	 * 
	 * @return The type of data this fragment holds. 
	 * See UpdaterService.DATATYPE
	 */
	protected abstract int getType();

	/**
	 * @param cursor The cursor which supplies the list objects. 
	 * @return An adapter which will be wrapped with an EndlessAdapter
	 */
	protected abstract SimpleCursorAdapter getListAdapter(Cursor cursor);


	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);

		mLastSelectedListItemId = "";

		if(savedInstance != null) {
			mLastSelectedListItemId = 
					savedInstance.getString("last_selected_list_item");
			Log.d(TAG, "Restored item to: " + mLastSelectedListItemId);

			mLastVisibileItem = 
					savedInstance.getInt("first_item_visible", 0);
			
			mCurrentFragmentTag =
					savedInstance.getString("current_fragment_tag");

			Log.d(TAG, "Restored visible to: " + mLastVisibileItem);
		}

		mApplication = (TTApplication) getActivity().getApplication();	

		mDataFilter = new IntentFilter(UpdaterService.SEND_DATA);

		mNoDataFilter = new IntentFilter(UpdaterService.NO_DATA);

		mErrorFilter = new IntentFilter(UpdaterService.ERROR);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mIsPortrait = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT;
		
		mReceiver = new PortableReceiver();

		//Register the receive to receive results from a service
		mReceiver.setReceiver(getReceiver());

		setupListView();

		if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
			mCurrentTask.cancel(true);
		}

		mCurrentTask = new DataLoadingTask(BaseFragment.this, DataLoadingTask.CURRENT);
		mCurrentTask.execute();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("last_selected_list_item", mLastSelectedListItemId);
		outState.putString("current_fragment_tag", mCurrentFragmentTag);
		outState.putInt("first_item_visible", mListView == null ? 0 : 
			mListView.getFirstVisiblePosition());
		Log.d(TAG, "Saving item as: " + mLastSelectedListItemId);
		Log.d(TAG, "First visible item: " + (mListView == null ? 0 : 
			mListView.getFirstVisiblePosition()));
	}

	@Override
	public void onStop() {
		super.onStop();
		if(mListView != null) {
			mListView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if(mListView != null) {
			mListView.setVisibility(View.VISIBLE);
		}
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

		if(mWrappedAdapter != null) {
			try {
				if(mWrappedAdapter.getCursor() != null) {
					mWrappedAdapter.getCursor().close();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			mWrappedAdapter = null;
		}

		mEndlessAdapter = null;

		//mListView = null;

		Log.i(TAG, "Destroying view");
	}

	private void setupListView() {
		if(mListView == null) {
			Log.w(TAG, "Listview was null while trying to setup");
			return;
		}

		mListView.setFastScrollEnabled(true);

		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Log.i(TAG, "STARTING REFRESH!");
				if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
					mCurrentTask.cancel(true);
				}

				mCurrentTask = new DataLoadingTask(BaseFragment.this, DataLoadingTask.NEWEST);
				mCurrentTask.execute();
			}
		});

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int pos, long id) {

				BaseViewHolder vh = (BaseViewHolder) view.getTag(); 

				//If we click on the same item, we know it is already showing!
				if(!mLastSelectedListItemId.equals(vh.id)) {
					onListItemClick(pos);
					mLastSelectedListItemId = vh.id;
				}
			}
		});

		//Display a callback when long clicked
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, 
					int pos, long id) {
				mCurrentActionMode = getActivity().startActionMode(getCallback(pos -1));
				return true;
			}

		});
	}

	public void changeCursor(Cursor cursor) {

		if(mWrappedAdapter == null) {
			mWrappedAdapter = getListAdapter(cursor);
		} else {
			mWrappedAdapter.changeCursor(cursor);
		}

		if(mEndlessAdapter == null) {
			mEndlessAdapter = new EndlessListAdapter(this, mWrappedAdapter);
		}

		if(mListView.getAdapter() == null) {
			Log.i(TAG, "Setting list adapter");
			mListView.setAdapter(mEndlessAdapter);
		} else {
			Log.i(TAG, "Notifying adapter of dataset change");
			mEndlessAdapter.notifyDataSetChanged();
		}
	}

	public void removeSpawnedFragments() {
		if(mCurrentFragmentTag != null) {
			try {
				Fragment f = getFragmentManager().findFragmentByTag(mCurrentFragmentTag);
				if(f != null) {
					getFragmentManager().beginTransaction().remove(f).commit();
				} 
			} catch(IllegalStateException e) {
				Log.e(TAG, "Error removing mCurrentFragment", e);
			}
		}
	}

	public EndlessListAdapter getEndlessAdapter() {
		return mEndlessAdapter;
	}

	public Cursor getCurrentCursor() {
		if(mWrappedAdapter != null) {
			return mWrappedAdapter.getCursor();
		} else {
			return null;
		}
	}

	public void showView(View v) {
		if(v != null) {
			v.setVisibility(View.VISIBLE);
		}
	}

	public void hideView(View v) {
		if(v != null) {
			v.setVisibility(View.GONE);
		}
	}

	protected void startRefresh(int type, String account) {
		if(getType() == type && account != null && mAccountId.equals(account)) {
			if(mCurrentTask != null && !mCurrentTask.isCancelled()) {
				mCurrentTask.cancel(true);
			}

			mCurrentTask = new DataLoadingTask(this, DataLoadingTask.CURRENT);
			mCurrentTask.execute();
		} else {
			Log.i(TAG, "Received Irrelevant broadcast: " 
					+ type + "(My type=" + getType() + ")");
		}
	}

	protected void stopRefresh(int type, String account) {
		if(getType() == type && account != null && mAccountId.equals(account)) {
			if(mListView.isRefreshing()) {
				mListView.onRefreshComplete();
			}
		} else {
			Log.i(TAG, "Received Irrelevant broadcast - TYPE: " 
					+ type + " ACCOUNT: " + account 
					+ "(My type=" + getType() + " My Account = " + mAccountId + ")");
		}
	}


	/**
	 * We have received some new data, lets attach it to the list
	 * @param cursor A cursor containing a complete list of data (both old and new)
	 */
	public void attachData(Cursor cursor) {
		if(cursor == null) {
			Toast.makeText(getActivity(), "No new data", Toast.LENGTH_SHORT).show();
		}

		if(mListView == null) {
			Log.i(TAG, "Listview was null!");
			return;
		}

		if(mWrappedAdapter == null) {
			mWrappedAdapter = getListAdapter(cursor);
		} else {
			mWrappedAdapter.changeCursor(cursor);
		}

		if(mEndlessAdapter == null) {
			mEndlessAdapter = new EndlessListAdapter(this, mWrappedAdapter);
		} else {
			mEndlessAdapter.notifyDataSetChanged();
		}

		if(mListView.isRefreshing()) {
			mListView.onRefreshComplete();
		}
	}
}
