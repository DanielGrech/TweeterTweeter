package com.DGSD.TweeterTweeter.Fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserListFragment extends DialogFragment {
	public static final String LOG = UserListFragment.class.getSimpleName();
	
	public static UserListFragment newInstance(String accountId) {
		UserListFragment f = new UserListFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();

		args.putString("accountId", accountId);

		f.setArguments(args);

		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
}
