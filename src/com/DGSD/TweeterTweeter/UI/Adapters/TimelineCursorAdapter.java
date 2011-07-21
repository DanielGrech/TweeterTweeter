package com.DGSD.TweeterTweeter.UI.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.StatusData;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.github.droidfu.widgets.WebImageView;

public class TimelineCursorAdapter extends SimpleCursorAdapter{
	private static final String TAG = TimelineCursorAdapter.class.getSimpleName();


	private int mLayoutRes;

	int screenCol = -1;

	int textCol = -1;

	int createdCol = -1;

	int imgCol = -1;

	public TimelineCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to, 0);

		mLayoutRes = layout;

		screenCol = cursor.getColumnIndex(StatusData.C_SCREEN_NAME);

		textCol = cursor.getColumnIndex(StatusData.C_TEXT);

		createdCol = cursor.getColumnIndex(StatusData.C_CREATED_AT);

		imgCol = cursor.getColumnIndex(StatusData.C_IMG);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(mLayoutRes, parent, false);

		view.setTag(new ViewHolder((TextView) view.findViewById(R.id.timeline_source), 
				(TextView) view.findViewById(R.id.timeline_tweet), 
				(TextView) view.findViewById(R.id.timeline_date), 
				(WebImageView) view.findViewById(R.id.timeline_profile_image)
		));

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {

		ViewHolder vh = (ViewHolder) view.getTag();

		if(vh == null) {
			vh = new ViewHolder(
					(TextView) view.findViewById(R.id.timeline_source),
					(TextView) view.findViewById(R.id.timeline_date),
					(TextView) view.findViewById(R.id.timeline_tweet),
					(WebImageView) view.findViewById(R.id.timeline_profile_image)
			);

			view.setTag(vh);
		} 

		vh.screenName.setText(c.getString(screenCol));

		vh.tweet.setText(c.getString(textCol));

		try{
			vh.createdAt.setText( 
					DateUtils.getRelativeTimeSpanString(view.getContext(), 
							Long.valueOf( c.getString(createdCol) )) );

		} catch(NumberFormatException e){
			Log.e(TAG, "Error converting time string", e);
		} catch(ClassCastException e) {
			Log.e(TAG, "Error casting to textview", e);
		}

		vh.img.setImageUrl(c.getString(imgCol));
		
		try{
			vh.img.loadImage();
		}catch(OutOfMemoryError e) {
			// :(
			Log.e(TAG, "OUT OF MEMORY!");
			vh.img.reset();
		}

	}

	private class ViewHolder {
		public TextView screenName;
		public TextView createdAt;
		public TextView tweet;
		public WebImageView img;

		public ViewHolder(TextView sn, TextView t, TextView c, WebImageView i) {
			screenName = sn;
			createdAt = c;
			tweet = t;
			img = i;
		}

	}
}