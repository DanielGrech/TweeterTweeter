package com.DGSD.TweeterTweeter;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

public class TTActivity extends Activity {

	private static final String TAG = TTActivity.class.getSimpleName();

	private TTApplication mApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		ActionBar mActionBar = getActionBar();

		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background_repeat));

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mActionBar.setListNavigationCallbacks(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"Daniel Grech", "DGSoftwareDev"}), 
				new OnNavigationListener(){
			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {
				
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

}
