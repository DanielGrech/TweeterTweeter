package com.DGSD.TweeterTweeter.UI.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.github.droidfu.widgets.WebImageView;

public class PeopleCursorAdapter extends SimpleCursorAdapter{
	private static final String TAG = PeopleCursorAdapter.class.getSimpleName();

	private int mLayoutRes;

	int screenCol = -1;

	int imgCol = -1;
	
	int idCol = -1;

	public PeopleCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to, 0);

		mLayoutRes = layout;

		screenCol = cursor.getColumnIndex(StatusData.C_SCREEN_NAME);

		imgCol = cursor.getColumnIndex(StatusData.C_IMG);
		
		idCol = cursor.getColumnIndex(StatusData.C_ID);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(mLayoutRes, parent, false);

		view.setTag(new PeopleViewHolder((TextView) view.findViewById(R.id.screen_name),
				(WebImageView) view.findViewById(R.id.profile_image),
				cursor.getString(idCol)
				));
		
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {

		PeopleViewHolder vh = (PeopleViewHolder) view.getTag();

		if(vh == null) {
			vh = new PeopleViewHolder((TextView) view.findViewById(R.id.screen_name),
					(WebImageView) view.findViewById(R.id.profile_image),
					c.getString(idCol));
			view.setTag(vh);
		}

		vh.img.reset();

		vh.screenName.setText(c.getString(screenCol));

		vh.img.setImageUrl(c.getString(imgCol));

		try{
			vh.img.loadImage();
		}catch(OutOfMemoryError e) {
			// :(
					Log.e(TAG, "OUT OF MEMORY!");
					vh.img.reset();
		}
	}
}