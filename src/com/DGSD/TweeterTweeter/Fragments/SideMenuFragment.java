package com.DGSD.TweeterTweeter.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.Utils.Log;

public class SideMenuFragment extends ListFragment {
	public static final String TAG = SideMenuFragment.class.getSimpleName();

	private static final String KEY_SELECTED_ITEM = "selected_item";
	
	private static final int ITEM_HOME_TIMELINE = 0;
	private static final int ITEM_DM_RECEIVED = 1;
	private static final int ITEM_DM_SENT = 2;
	private static final int ITEM_FAVOURITES = 3;
	private static final int ITEM_FOLLOWERS = 4;
	private static final int ITEM_FOLLOWING = 5;
	private static final int ITEM_MENTIONS = 6;
	private static final int ITEM_RETWEETS_BY = 7;
	private static final int ITEM_RETWEETS_OF = 8;
	private static final int ITEM_SAVED_SEARCH = 9;
	private static final int ITEM_LISTS = 10;

	private int mSelectedItem;

	private String[] mListItems;
	
	private TTApplication mApplication;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null) {
			mSelectedItem = savedInstanceState.getInt(KEY_SELECTED_ITEM);
		} else {
			mSelectedItem = 0;
		}

		mApplication = (TTApplication) getActivity().getApplication();
		
		mListItems = getResources().getStringArray(R.array.menu_list);

		ListView lv = getListView();

		setListAdapter(new ArrayAdapter<String>(getActivity(), 
				R.layout.side_menu_list_item, mListItems));

		lv.setCacheColorHint(Color.TRANSPARENT);
				
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		onListItemClick(lv, null, mSelectedItem, -1);

	}

	@Override
	public void onListItemClick(ListView lv, View v, int pos, long id) {
		mSelectedItem = pos;
		
		lv.setItemChecked(pos, true);
		
		Fragment fragment = null;
		
		String account = mApplication.getSelectedAccount();
		String username = mApplication.getUserName(account);
		
		switch(pos) {
			case ITEM_HOME_TIMELINE:
				fragment = HomeTimelineFragment.newInstance(account);
				break;
				
			case ITEM_DM_RECEIVED:
				
				break;
				
			case ITEM_DM_SENT:
				
				break;
				
			case ITEM_FAVOURITES:
				fragment = FavouritesListFragment.newInstance(account, username);
				break;
				
			case ITEM_FOLLOWERS:
				
				break;
				
			case ITEM_FOLLOWING:
				
				break;
				
			case ITEM_MENTIONS:
				
				break;
				
			case ITEM_RETWEETS_BY:
				
				break;
				
			case ITEM_RETWEETS_OF:
				
				break;
				
			case ITEM_SAVED_SEARCH:
				
				break;
				
			case ITEM_LISTS:
				
				break;
		}
		
		getFragmentManager().beginTransaction()
							.replace(R.id.container, fragment)
							.addToBackStack(null)
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
							.commit();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "Saving State: " + mSelectedItem);
		outState.putInt(KEY_SELECTED_ITEM, mSelectedItem);
	}
	
	
}