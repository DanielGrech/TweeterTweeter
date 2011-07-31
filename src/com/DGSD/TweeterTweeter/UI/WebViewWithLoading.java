package com.DGSD.TweeterTweeter.UI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class WebViewWithLoading {

	private static LayoutParams lp = 
			new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.FILL_PARENT, 
					RelativeLayout.LayoutParams.FILL_PARENT );


	//TODO: Configuration option to hide zoom controls
	public static View getView(final Context c, String url) {
		final RelativeLayout rl = new RelativeLayout(c);
		rl.setGravity(Gravity.CENTER);


		final WebView wv = new WebView(c);
		wv.setLayoutParams(lp);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setBuiltInZoomControls(true);
		wv.getSettings().setDisplayZoomControls(true);
		wv.setDrawingCacheEnabled(true);
		wv.setAnimationCacheEnabled(true);
		wv.loadUrl(url);
		wv.setVisibility(View.GONE);

		final ProgressBar pb = new ProgressBar(c);

		rl.addView(wv);
		rl.addView(pb,30,30);

		wv.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				//Remove progress bar
				try{
					rl.removeView(pb);
				}catch(NullPointerException e){
					e.printStackTrace();
				}

				view.setVisibility(View.VISIBLE);
			}

			@Override
			public void onReceivedError (WebView view, int errorCode, 
					String description, String failingUrl) {
				//Remove progress bar
				try{
					rl.removeView(pb);
				}catch(NullPointerException e){
					e.printStackTrace();
				}

				view.loadUrl("file:///android_asset/error.html");

				//Show the image!
				view.setVisibility(View.VISIBLE);
			}

			/*TODO: Have a preference for loading in this view or not:
			 * if(PREFERENCE) {
			 * 		startActivity(Intent(url))
			 * 		return true;
			 * } else {
			 * 		return false;
			 * }
			 */
			@Override
			public boolean shouldOverrideUrlLoading(WebView wv, String url) {
				c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}
		});

		return rl;
	}
}
