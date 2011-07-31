package com.DGSD.TweeterTweeter.UI.Adapters;

import android.widget.TextView;

import com.github.droidfu.widgets.WebImageView;

public class PeopleViewHolder extends BaseViewHolder{
	public TextView screenName;
	public WebImageView img;

	public PeopleViewHolder(TextView s, WebImageView i, String _id) {
		id = _id;
		
		screenName = s;
		img = i;
	}
}