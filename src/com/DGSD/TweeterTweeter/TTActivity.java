//TODO: Add 'passcode' to preferences .. must enter to unlock!

package com.DGSD.TweeterTweeter;

import java.util.LinkedList;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.Fragments.NewTweetFragment;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class TTActivity extends MapActivity implements TabListener{

	private static final String TAG = TTActivity.class.getSimpleName();

	public static final String SIDE_MENU = "side_menu";
	
	//We can only have 1 per activity, so we need it here for all fragments
	public static MapView mMapView;

	private TTApplication mApplication;

	private ViewGroup mDataContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		if(mMapView == null) {
			setMapView( new MapView(this, TTApplication.MAPS_KEY) );
		}

		mApplication = (TTApplication) getApplication();

		mDataContainer = (ViewGroup) findViewById(R.id.data_container);

		LayoutTransition lt = new LayoutTransition();

		lt.setStagger(LayoutTransition.CHANGE_APPEARING, 30);
		lt.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30);
		lt.setDuration(150);

		mDataContainer.setLayoutTransition(lt);

		ActionBar mActionBar = getActionBar();

		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(false);

		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_background));

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		LinkedList<String[]> list = mApplication.getAccountListWithImage();
		
		for(int i = 0, size = list.size(); i<size; i++) {
			ActionBar.Tab tab = mActionBar.newTab();
			tab.setText(list.get(i)[0]);
			tab.setTabListener(this);
			mActionBar.addTab(tab);
		}
		
		
		/*if(savedInstanceState != null) {
			mSecondaryContainer.setVisibility(
					savedInstanceState.getInt("second_container_visibility", 
							View.GONE));
		}*/
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Handle the 'Search View' in the action bar
		getMenuInflater().inflate(R.menu.menu, menu);

		final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

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

		searchView.setOnDragListener(new OnDragListener(){
			@Override
			public boolean onDrag(View v, DragEvent event) {
				final int action = event.getAction();

				switch(action) {
					case DragEvent.ACTION_DRAG_STARTED:
						if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
							return true;
						} else {
							return false;
						}

					case DragEvent.ACTION_DRAG_ENTERED:

						if(searchView.isIconified()) {
							searchView.setBackgroundResource(R.drawable.background_repeat);
						} else {
							searchView.setBackgroundColor(Color.TRANSPARENT);
						}
						break;

					case DragEvent.ACTION_DRAG_EXITED:
						searchView.setBackgroundColor(Color.TRANSPARENT);
						break;

					case DragEvent.ACTION_DROP:
						searchView.setIconified(false);

						ClipData.Item item = event.getClipData().getItemAt(0);

						String dragData = item.getText().toString();
						searchView.setQuery(dragData, true);
						break;

					case DragEvent.ACTION_DRAG_ENDED:
						System.err.println("ENDING DRAG!");
						searchView.setBackgroundColor(Color.TRANSPARENT);
						break;
				}

				return false;
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

			case R.id.menu_preferences:
				startActivity(new Intent(this, Preferences.class));
				break;
			default:
				return false;
		}

		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//outState.putInt("second_container_visibility", mSecondaryContainer.getVisibility());
	}

	@Override
	public void onDestroy() {
		mMapView = null;

		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public synchronized MapView getMapView() {
		return mMapView;
	}

	public synchronized void setMapView(MapView m) {
		mMapView = m;
		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.getController().setZoom(12);
	}


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mApplication.setSelectedAccount("account" + (tab.getPosition()+1));		
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}
}
