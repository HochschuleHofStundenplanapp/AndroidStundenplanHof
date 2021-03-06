/*
 * Copyright (c) 2017 Hof University. Author Daniel Glaser.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.hof.university.app.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.util.Define;

/**
 * Created by Daniel on 02.08.2017.
 */

public class BusTrainScheduleFragment extends Fragment {

    public final static String TAG = "BusTrainScheduleFragment";
    private SwipeRefreshLayout swipeContainer;

    @Override
    public final void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(Html.fromHtml("<font color='"+ ContextCompat.getColor(MainActivity.getAppContext(), R.color.colorBlack)+"'>"
                + getString(R.string.navigation)+"</font>"));
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_accent_24dp);
    
    
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_navigation).setChecked(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) getActivity();
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_navigation).setChecked(false);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_webview, container, false);

        final WebView myWebView = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Insert your code here
                myWebView.loadUrl(Define.DBURL);
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                swipeContainer.setRefreshing(true);
            }

            public void onPageFinished(WebView view, String url) {
                swipeContainer.setRefreshing(false);
            }
        });

        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);
        myWebView.loadUrl(Define.DBURL);

        myWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                myWebView.loadUrl("javascript: (function() {document.getElementById('locZ0').value= 'Hochschule, Hof (Saale)';}) ();" );
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        swipeContainer.setRefreshing(false);
        swipeContainer.destroyDrawingCache();
        swipeContainer.clearAnimation();
        super.onDestroyView();
    }
}
