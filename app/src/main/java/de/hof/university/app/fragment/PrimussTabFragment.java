package de.hof.university.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;

/**
 * Created by Christian Pfeiffer on 14.12.16.
 */

public class PrimussTabFragment extends Fragment {
	public final String TAG = "PrimussFragment";

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

		MainActivity mainActivity = (MainActivity) getActivity();
		WebView myWebView = (WebView) mainActivity.findViewById(R.id.webview);
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		myWebView.setWebChromeClient(new WebChromeClient());
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.loadUrl("https://www3.primuss.de/cgi-bin/login/index.pl?FH=fhh");

		return v;

	}


}
