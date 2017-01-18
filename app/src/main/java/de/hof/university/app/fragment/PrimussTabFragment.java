package de.hof.university.app.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Log;

/**
 * Created by Christian Pfeiffer on 14.12.16.
 */

public class PrimussTabFragment extends Fragment {
	public final static String TAG = "PrimussFragment";

	private ProgressBar mPbar = null;

	@Override
	public final void onResume() {
		super.onResume();
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.primuss);

		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_primuss).setChecked(true);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
								   Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_webview, container, false);

		WebView myWebView = (WebView) v.findViewById(R.id.webview);
		mPbar = (ProgressBar) v.findViewById(R.id.web_view_progress);
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		myWebView.setWebChromeClient(new WebChromeClient());
		myWebView.setWebViewClient(new WebViewClient() {

									   @Override
									   public void onPageStarted(WebView view, String url, Bitmap favicon) {
										   mPbar.setVisibility(View.VISIBLE);
									   }

									   public void onPageFinished(WebView view, String url) {
										   mPbar.setVisibility(View.GONE);
									   }
								   });
		myWebView.getSettings().setSupportZoom(true);
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.getSettings().setDisplayZoomControls(false);
		myWebView.loadUrl("https://www3.primuss.de/cgi-bin/login/index.pl?FH=fhh");

		return v;

	}


}
