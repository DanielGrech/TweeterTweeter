package com.DGSD.TweeterTweeter;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.NewTweetFragment;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.appsolut.adapter.collections.CollectionsAdapter;

public class TTActivity extends Activity {

	private static final String TAG = TTActivity.class.getSimpleName();

	private TTApplication mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mApplication = (TTApplication) getApplication();

		ActionBar mActionBar = getActionBar();

		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background_repeat));

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mActionBar.setListNavigationCallbacks(new CollectionsAdapter<String>(this, 
				android.R.layout.simple_dropdown_item_1line, mApplication.getAccountList()), 
				new OnNavigationListener(){
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				/*
				 * This is itemPosition+1 as account naming is not 0-indexed
				 */
				Log.i(TAG, "Setting selected account to account" + (itemPosition+1));
				mApplication.setSelectedAccount("account" + (itemPosition+1));
				return false;
			}
		});
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Handle the 'Search View' in the action bar
		getMenuInflater().inflate(R.menu.menu, menu);

		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange( String newText ) {
				//Do something here?
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				Toast.makeText(TTActivity.this, "Searching for " + query, Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_new_tweet:
				NewTweetFragment.newInstance(mApplication.getSelectedAccount(), 
						null).show(getFragmentManager(), null);
				break;

			default:
				return false;
		}

		return true;
	}


}
