package com.DGSD.TweeterTweeter.Fragments;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TweetData;
import com.github.droidfu.widgets.WebImageView;

public class TweetFragment extends DialogFragment {
	
	private TextView mDate;
	
	private TextView mScreenName;
	
	private TextView mText;
	
	private WebImageView mvImage;
	
	private TweetData mData;

	public static TweetFragment newInstance(ContentValues statusVals) {
		TweetFragment f = new TweetFragment();

		Bundle args = new Bundle();

		args.putParcelable("values", statusVals);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mData = new TweetData((ContentValues)getArguments().getParcelable("values"));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View root =  inflater.inflate(R.layout.tweet_layout, container, false);

		mvImage = (WebImageView) root.findViewById(R.id.profile_image);

		mDate = (TextView) root.findViewById(R.id.date);
		
		mScreenName = (TextView) root.findViewById(R.id.tweet_user);
		
		mText = (TextView) root.findViewById(R.id.tweet_text);
		
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		attachData();

	}

	public void attachData() {
		mText.setText(mData.text);
		
		mScreenName.setText(mData.screenName);
		
		mDate.setText(DateUtils.getRelativeTimeSpanString(getActivity(), 
				Long.valueOf(mData.date)));
		
		//Set the image view
		mvImage.setImageUrl(mData.img);
		mvImage.setAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.grow_from_bottom));
		mvImage.loadImage();
		
		
	}

	
}
