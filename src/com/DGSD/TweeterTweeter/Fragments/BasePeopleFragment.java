package com.DGSD.TweeterTweeter.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.DGSD.TweeterTweeter.Tasks.DataLoadingTask;
import com.DGSD.TweeterTweeter.UI.Adapters.PeopleCursorAdapter;
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
						new DataLoadingTask(BasePeopleFragment.this, DataLoadingTask.CURRENT).execute();
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

		new DataLoadingTask(BasePeopleFragment.this, DataLoadingTask.CURRENT).execute();

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

				Toast.makeText(getActivity(), "Long click on position " + pos, 
						Toast.LENGTH_SHORT).show();
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
		if(mCursor == null) {
			Log.i(TAG, "CURSOR WAS NULL!! CANT SET ADAPTER!");
			return;
		}
		
		if(mCursor.getCount() == 0) {
			new DataLoadingTask(BasePeopleFragment.this, DataLoadingTask.NEWEST).execute();
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
}
