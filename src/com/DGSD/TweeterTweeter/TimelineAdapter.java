package com.DGSD.TweeterTweeter;

import java.util.List;

import twitter4j.Status;
import android.app.Activity;
import android.text.format.DateUtils;
import com.DGSD.TweeterTweeter.Utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.UI.LinkEnabledTextView;
import com.github.droidfu.widgets.WebImageView;

public class TimelineAdapter  extends ArrayAdapter<Object> {
	private static final String TAG = TimelineAdapter.class.getSimpleName();
	
	private final Activity mActivity;
	
	private final List<Status> mItemList;

	public TimelineAdapter(Activity activity, List<Status> itemList) {
		super(activity, R.layout.timeline_list_item);
		
		mActivity = activity;
		
		mItemList = itemList;
	}

	@Override
	public int getCount() {
		return mItemList == null ? 0: mItemList.size();
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View root = convertView;
		RowItem rowItem = null;

		if(root == null){
			LayoutInflater inflater = mActivity.getLayoutInflater();
			root = inflater.inflate(R.layout.timeline_list_item, null);

			rowItem = new RowItem();
			rowItem.tweet = (LinkEnabledTextView) root.findViewById(R.id.timeline_tweet);
			rowItem.tweet_details = (TextView) root.findViewById(R.id.timeline_source);
			rowItem.img  = (WebImageView) root.findViewById(R.id.timeline_profile_image);
			rowItem.favourite  = (ImageView) root.findViewById(R.id.timeline_favourite_star);
			rowItem.date = (TextView) root.findViewById(R.id.timeline_date);

			root.setTag(rowItem);
		} else {
			rowItem = (RowItem) root.getTag();
		}
		
		Status s = mItemList.get(pos);
		
		//Set the tweet text ----------
		rowItem.tweet.setText(s.getText());
		
		//Set the image url ----------
		String imageUrl = s.getUser().getProfileImageURL().toString();
		rowItem.img.setImageUrl(imageUrl);
		if(imageUrl != "")
			rowItem.img.loadImage();
		
		//Set the favourite status ----------
		if(s.isFavorited()) {
			rowItem.favourite.setVisibility(View.VISIBLE);
		}
		else {
			rowItem.favourite.setVisibility(View.GONE);
		}
		
		//Set the details ----------
		rowItem.tweet_details.setText(s.getUser().getScreenName());
		
		//Set the date ----------
		long timestamp = -1;
		try{
			timestamp = s.getCreatedAt().getTime();
		}catch(NumberFormatException e){
			Log.e(TAG, "Error converting time string", e);
		}
		
		rowItem.date.setText( 
				DateUtils.getRelativeTimeSpanString(root.getContext(), timestamp) );
		
		return root;
	}
	
	protected class RowItem {
		public LinkEnabledTextView tweet;
		public TextView tweet_details;
		public WebImageView img;
		public ImageView favourite;
		public TextView date;
	}

}
