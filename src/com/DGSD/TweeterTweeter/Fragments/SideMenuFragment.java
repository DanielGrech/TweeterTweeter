package com.DGSD.TweeterTweeter.Fragments;

import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.Utils.Log;

public class SideMenuFragment extends ListFragment {
	public static final String TAG = SideMenuFragment.class.getSimpleName();

	public static final String KEY_SELECTED_ITEM = "selected_item";

	private int mSelectedItem;

	private String[] mListItems;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null) {
			mSelectedItem = savedInstanceState.getInt(KEY_SELECTED_ITEM);
		} else {
			mSelectedItem = 0;
		}

		mListItems = getResources().getStringArray(R.array.menu_list);

		ListView lv = getListView();

		setListAdapter(new ArrayAdapter<String>(getActivity(), 
				R.layout.side_menu_list_item, mListItems));

		lv.setCacheColorHint(Color.TRANSPARENT);
				
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		lv.setItemChecked(mSelectedItem, true);

	}

	@Override
	public void onListItemClick(ListView lv, View v, int pos, long id) {
		mSelectedItem = pos;
		
		Toast.makeText(getActivity(), "Clicked Item " + mSelectedItem, 
				Toast.LENGTH_SHORT).show();
		
		lv.setItemChecked(pos, true);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "Saving State: " + mSelectedItem);
		outState.putInt(KEY_SELECTED_ITEM, mSelectedItem);
	}
	
	
}
