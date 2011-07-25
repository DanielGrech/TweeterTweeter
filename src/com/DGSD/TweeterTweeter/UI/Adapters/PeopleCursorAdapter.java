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

	public PeopleCursorAdapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to, 0);

		mLayoutRes = layout;

		screenCol = cursor.getColumnIndex(StatusData.C_SCREEN_NAME);

		imgCol = cursor.getColumnIndex(StatusData.C_IMG);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(mLayoutRes, parent, false);

		view.setTag(new ViewHolder((TextView) view.findViewById(R.id.screen_name),
				(WebImageView) view.findViewById(R.id.profile_image)
				));
		
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor c) {

		ViewHolder vh = (ViewHolder) view.getTag();

		if(vh == null) {
			vh = new ViewHolder((TextView) view.findViewById(R.id.screen_name),
					(WebImageView) view.findViewById(R.id.profile_image));
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

	private class ViewHolder {
		public TextView screenName;
		public WebImageView img;


		public ViewHolder(TextView s, WebImageView i) {
			screenName = s;
			img = i;
		}
	}
}