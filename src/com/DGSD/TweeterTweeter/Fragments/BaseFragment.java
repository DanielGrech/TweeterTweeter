package com.DGSD.TweeterTweeter.Fragments;

import twitter4j.TwitterException;
import android.app.DialogFragment;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;

import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver;
import com.DGSD.TweeterTweeter.Receivers.PortableReceiver.Receiver;
import com.DGSD.TweeterTweeter.Services.UpdaterService;
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.PullToRefreshListView;
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

	protected PortableReceiver mReceiver;

	protected IntentFilter mDataFilter;

	protected IntentFilter mNoDataFilter;

	protected IntentFilter mErrorFilter;

	protected ActionMode mCurrentActionMode;

	protected DataLoadingTask mCurrentTask;
	
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

	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		mApplication = (TTApplication) getActivity().getApplication();	

		mDataFilter = new IntentFilter(UpdaterService.SEND_DATA);

		mNoDataFilter = new IntentFilter(UpdaterService.NO_DATA);

		mErrorFilter = new IntentFilter(UpdaterService.ERROR);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mReceiver = new PortableReceiver();

		//Register the receive to receive results from a service
		mReceiver.setReceiver(getReceiver());

		setupListView();
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

		mEndlessAdapter = null;

		mWrappedAdapter = null;

		try {
			mWrappedAdapter.getCursor().close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		mListView = null;

		Log.i(TAG, "Destroying view");
	}

	private void setupListView() {
		mListView = new PullToRefreshListView(getActivity());

		if(mListView != null) {
			mListView.setFastScrollEnabled(true);
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int pos, long id) {
				onListItemClick(pos);
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
		mWrappedAdapter.changeCursor(cursor);

		mEndlessAdapter.notifyDataSetChanged();
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


	public void attachNewData() {

	}

	public void attachOldData() {

	}
}
