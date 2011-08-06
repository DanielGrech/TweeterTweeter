package com.DGSD.TweeterTweeter.Fragments;

import java.util.Random;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

	private static String mCurrentFragmentTag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = super.onCreateView(inflater, container, savedInstanceState);

		v.setBackgroundResource(R.drawable.sidebar_background);

		return v;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mApplication = (TTApplication) getActivity().getApplication();

		mListItems = getResources().getStringArray(R.array.menu_list);

		ListView lv = getListView();

		setListAdapter(new BorderedArrayAdapter(getActivity(), 
				R.layout.side_menu_list_item, mListItems));

		lv.setCacheColorHint(Color.TRANSPARENT);

		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		if(savedInstanceState != null) {
			mCurrentFragmentTag = savedInstanceState.getString("current_fragment_tag");

			System.err.println("RESTORING mCurrentFragmentTag to: " + mCurrentFragmentTag);

			mSelectedItem = savedInstanceState.getInt(KEY_SELECTED_ITEM);
			getListView().setItemChecked(mSelectedItem, true);
		} else {
			System.err.println("CANT RESTORE mCurrentFragmentTag");
			//We aren't creating after a rotation!
			mSelectedItem = 0;
			displayFragmentOfItem(mSelectedItem);
		}

	}

	@Override
	public void onListItemClick(ListView lv, View v, int pos, long id) {
		if(mSelectedItem == pos) {
			//We are already showing this item!
			return;
		} else {
			mSelectedItem = pos;

			displayFragmentOfItem(mSelectedItem);
		}
	}

	private void displayFragmentOfItem(int itemPos) {
		getListView().setItemChecked(itemPos, true);

		if(mCurrentFragmentTag != null) {
			try {
				BaseFragment oldFrag = 
						(BaseFragment) getFragmentManager().findFragmentByTag(mCurrentFragmentTag);

				if(oldFrag != null) {
					oldFrag.removeSpawnedFragments();
					getFragmentManager().beginTransaction().remove(oldFrag).commit();
				}
			} catch(IllegalStateException e) {
				Log.e(TAG, "Error removing mCurrentFragment", e);
			}
		} 

		String account = mApplication.getSelectedAccount();
		String username = mApplication.getUserName(account);

		Fragment f = null;

		switch(itemPos) {
			case ITEM_HOME_TIMELINE:
				f = HomeTimelineFragment.newInstance(account);
				break;

			case ITEM_DM_RECEIVED:

				break;

			case ITEM_DM_SENT:

				break;

			case ITEM_FAVOURITES:
				f = FavouritesListFragment.newInstance(account, username);
				break;

			case ITEM_FOLLOWERS:
				f = FollowersFragment.newInstance(account, username);
				break;

			case ITEM_FOLLOWING:
				f = FollowingFragment.newInstance(account, username);
				break;

			case ITEM_MENTIONS:
				f = MentionsListFragment.newInstance(account);
				break;

			case ITEM_RETWEETS_BY:
				f = RetweetsByFragment.newInstance(account, username);
				break;

			case ITEM_RETWEETS_OF:
				f = RetweetsOfFragment.newInstance(account);
				break;

			case ITEM_SAVED_SEARCH:

				break;

			case ITEM_LISTS:

				break;
			default:
				//O dear dear dear :(
				f = new Fragment();
				break;
		}

		mCurrentFragmentTag = String.valueOf(new Random().nextInt());

		getFragmentManager().beginTransaction()
		.add(R.id.data_container, f, mCurrentFragmentTag)
		.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
		.commit();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "Saving State: " + mSelectedItem);
		outState.putInt(KEY_SELECTED_ITEM, mSelectedItem);
		outState.putString("current_fragment_tag", mCurrentFragmentTag);
	}

	public static String getCurrentFragmentTag() {
		return mCurrentFragmentTag;
	}

	private class BorderedArrayAdapter extends ArrayAdapter<String> {
		private String[] mListItems;

		private Context mContext;

		public BorderedArrayAdapter(Context context, int textViewResourceId, String[] items) {
			super(context, textViewResourceId, items);

			mContext = context;

			mListItems = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(mContext).inflate(R.layout.side_menu_list_item, null);
				TextView tv = (TextView) v.findViewById(R.id.side_menu_text);
				v.setTag(tv);
			} 
			
			TextView tv = (TextView) v.getTag();
			
			tv.setText(mListItems[position]);
			
			//For UI consistency, we dont want to see the top border..
			if(position == 0) {
				v.findViewById(R.id.side_menu_top_border).setVisibility(View.GONE);
			}
			
			return v;
		}
	}


}
