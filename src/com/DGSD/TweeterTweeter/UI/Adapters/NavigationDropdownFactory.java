package com.DGSD.TweeterTweeter.UI.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.appsolut.adapter.collections.view.ICollectionsAdapterViewFactory;
import com.github.droidfu.widgets.WebImageView;

public class NavigationDropdownFactory<T> implements ICollectionsAdapterViewFactory<T> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent, 
			T item, LayoutInflater inflater, Context context) {
		
		String[] holder = (String[]) item;
		
		ViewGroup retval;
		PeopleViewHolder vh;
		
		if (convertView == null) {
			retval = (ViewGroup) inflater.inflate(R.layout.navigation_dropdown_item, null, true);
			
			vh = new PeopleViewHolder( (TextView)retval.findViewById(R.id.screen_name), 
					(WebImageView)retval.findViewById(R.id.profile_image), null);
			
			retval.setTag(vh);
			
		} else {
			retval = (ViewGroup) convertView;
			vh = (PeopleViewHolder) convertView.getTag();
		}
		
		final TextView tv = (TextView) retval.findViewById(R.id.screen_name);
		tv.setText(holder[0]);//The screen name
		
		System.err.println("LOADING IMAGE: " + holder[1]);
		
		final WebImageView iv = (WebImageView) retval.findViewById(R.id.profile_image);
		iv.setImageUrl(holder[1]);
		iv.loadImage();
		
		
		return retval;
	}

}