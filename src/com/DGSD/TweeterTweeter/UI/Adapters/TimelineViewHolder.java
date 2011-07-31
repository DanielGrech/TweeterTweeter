package com.DGSD.TweeterTweeter.UI.Adapters;

import android.widget.TextView;

import com.github.droidfu.widgets.WebImageView;

public class TimelineViewHolder extends BaseViewHolder {

	public TextView screenName;
	public TextView createdAt;
	public TextView tweet;
	public WebImageView img;

	public TimelineViewHolder(TextView sn, TextView t, TextView c, WebImageView i, String _id) {
		id = _id;
		
		screenName = sn;
		createdAt = c;
		tweet = t;
		img = i;
	}
}
