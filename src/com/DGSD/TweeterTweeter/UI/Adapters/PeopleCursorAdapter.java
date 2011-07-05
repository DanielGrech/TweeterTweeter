package com.DGSD.TweeterTweeter.UI.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.github.droidfu.widgets.WebImageView;

public class PeopleCursorAdapter extends SimpleCursorAdapter{
	private static final String TAG = PeopleCursorAdapter.class.getSimpleName();
	

	private int mLayoutRes;
	
	int screenCol = -1;
	
	int nameCol = -1;
	
	int imgCol = -1;

	public PeopleCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to, 0);

		mLayoutRes = layout;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Cursor c = getCursor();

		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(mLayoutRes, parent, false);

		if(screenCol == -1) {
			screenCol = c.getColumnIndex(StatusData.C_SCREEN_NAME);
		}
		
		if(nameCol == -1) {
			nameCol = c.getColumnIndex(StatusData.C_NAME);
		}
		
		if(imgCol == -1) {
			imgCol = c.getColumnIndex(StatusData.C_IMG);
		}
		
		TextView screen_name = (TextView) view.findViewById(R.id.screen_name);
		screen_name.setText(c.getString(screenCol));
		
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setText(c.getString(nameCol));
		
		String url = "";
		url = cursor.getString(imgCol);
		
		WebImageView wiv = (WebImageView) view.findViewById(R.id.profile_image);
		wiv.setImageUrl(url);
		
		if(url != "") {
			try{
				if(!wiv.isLoaded()) {
	        		wiv.loadImage();
	        	}
	        }catch(OutOfMemoryError e) {
	        	// :(
	        	wiv.reset();
	        }
		}
		
		view.setTag(new ViewHolder(screen_name, name, wiv));
		
		return view;
	}

	@Override
    public void bindView(View view, Context context, Cursor c) {

        ViewHolder vh = (ViewHolder) view.getTag();
        
        if(vh == null) {
        	Log.i(TAG, "VH IS NULL. MUST DEFINE");
        	vh = new ViewHolder(
        			(TextView) view.findViewById(R.id.screen_name),
        			(TextView) view.findViewById(R.id.name),
        			(WebImageView) view.findViewById(R.id.profile_image)
        	);
        	
        	view.setTag(vh);
        } 
        
        vh.screenName.setText(c.getString(screenCol));
        
        vh.name.setText(c.getString(nameCol));
        
        vh.img.setImageUrl(c.getString(imgCol));
        
        try{
        	if(!vh.img.isLoaded()) {
        		vh.img.loadImage();
        	}
        }catch(OutOfMemoryError e) {
        	// :(
        	vh.img.reset();
        }
        
    }
	
	private class ViewHolder {
		public TextView screenName;
		public TextView name;
		public WebImageView img;
		
		public ViewHolder(TextView sn, TextView n, WebImageView i) {
			screenName = sn;
			name = n;
			img = i;
		}
		
	}
}
