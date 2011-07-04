package com.DGSD.TweeterTweeter.Tasks;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.widget.TextView;
import android.widget.Toast;

import com.DGSD.TweeterTweeter.R;
import com.DGSD.TweeterTweeter.Utils.Log;
import com.rosaloves.bitlyj.BitlyException;

public class UrlShortenTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = UrlShortenTask.class.getSimpleName();
	
	private String mText;

	private String mUserName;

	private String mKey;

	private boolean has_error = false;
	
	private Activity mActivity;

	private ProgressDialog mProgressDialog;

	private Vector<Hyperlink> mLinkList;
	
	private TextView mTextView;

	Pattern hyperLinksPattern = 
		Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
	
	public UrlShortenTask(Activity a, TextView tv) {
		mLinkList = new Vector<Hyperlink>();
		
		mActivity = a;
		
		mTextView = tv;
	}

	@Override
	protected void onPreExecute() {
		mUserName = mActivity.getResources().getString(R.string.bitlyName);

		mKey = mActivity.getResources().getString(R.string.bitlyKey);

		mProgressDialog = ProgressDialog.show(mActivity, "", 
				"Shortening urls", true);
		
		mText = mTextView.getText().toString();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try{
			SpannableString linkableText = new SpannableString(mText);

			gatherLinks(linkableText, hyperLinksPattern);

			for(Hyperlink link : mLinkList) {
				String url = link.foundUrl.toString();
				if(!url.startsWith("http://")) {
					url = "http://".concat(url);
				}
				
				link.newUrl = as(mUserName, mKey)
						.call(shorten(url)).getShortUrl();
			}
		}catch(BitlyException e) {
			Log.e(TAG, "Error shortening URL", e);
			has_error = true;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void arg) {
		mProgressDialog.dismiss();

		if(has_error) {
			Toast.makeText(mActivity, 
					"Error shortening url", Toast.LENGTH_SHORT).show();
		}
		else {
			for(Hyperlink link : mLinkList) {
				System.err.println("NEW LINK: " + link.newUrl);
				
				if( link.newUrl.startsWith("http://")) {
					link.newUrl = link.newUrl.substring(7);
				}
				
				if( link.newUrl.startsWith("www.")) {
					link.newUrl = link.newUrl.substring(4);
				}
					
				
				mText = mText.replace(link.foundUrl, link.newUrl);
			}
			
			mTextView.setText("");
			
			mTextView.append(mText);
		}
	}

	private final void gatherLinks(Spannable s, Pattern pattern){
		// Matcher matching the pattern
		Matcher m = pattern.matcher(s);

		while (m.find()){
			int start = m.start();
			int end = m.end();

			Hyperlink spec = new Hyperlink();

			spec.foundUrl = s.subSequence(start, end);

			mLinkList.add(spec);
		}
	}

	private class Hyperlink{
		CharSequence foundUrl;
		String newUrl;
	}
}