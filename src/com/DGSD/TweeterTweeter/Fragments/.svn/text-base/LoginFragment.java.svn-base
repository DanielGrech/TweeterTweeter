package com.DGSD.TweeterTweeter.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.TTApplication;
import com.DGSD.TweeterTweeter.TwitterConnection;
import com.DGSD.TweeterTweeter.TwitterConnection.TwDialogListener;

public class LoginFragment extends Fragment {
	
	private static final String TAG = LoginFragment.class.getSimpleName();
	
	private Button mLoginButton;

	private Context mContext;

	private TwitterConnection mTwitter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();
		
		TTApplication app = (TTApplication)getActivity().getApplication();
		
		mTwitter = new TwitterConnection(app, mContext);
		mTwitter.setListener(mTwLoginDialogListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View root = inflater.inflate(R.layout.login_fragment_layout, container, false);

		mLoginButton = (Button) root.findViewById(R.id.loginButton);

		mLoginButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mTwitter.authorize();
			}
		});

		return root;
	}

	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
		@Override
		public void onComplete(String value) {
			Log.i(TAG, "IVE COMPLETED!!!!! " + value);

			Toast.makeText(mContext, "Login Successful", Toast.LENGTH_LONG).show();

		}

		@Override
		public void onError(String value) {
			Log.i(TAG, "ERROR LOGGING IN! " + value);
			
			Toast.makeText(mContext, "Twitter connection failed: " + value, Toast.LENGTH_LONG).show();
		}
	};

	public void clickLoginButton(){
		if(mLoginButton != null)
			mLoginButton.performClick();
	}

}
