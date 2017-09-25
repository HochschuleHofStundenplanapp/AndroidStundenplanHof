/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

package de.hof.university.app.experimental.fragment;

/**
 * Created by Lukas on 05.07.2016.
 */

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InterruptedIOException;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.experimental.LoginController;


public class NotenblattFragment extends Fragment {

    public final static String TAG = "NotenblattFragment";

    private SwipeRefreshLayout swipeContainer;
    private WebView webView;
    private NotenblattFragment.GetNotenblattTask task;
    private String html;
    private LoginController loginController;
    private String session;

    @Override
    public final void onDestroyView() {
        if (task != null) {
            task.cancel(true);
        }
        swipeContainer.setRefreshing(false);
        swipeContainer.destroyDrawingCache();
        swipeContainer.clearAnimation();

        super.onDestroyView();
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginController = LoginController.getInstance(getActivity());
        if ((savedInstanceState == null) || !savedInstanceState.containsKey("DATA")) {
            html = "";
        } else {
            html = savedInstanceState.getString("DATA");
        }

        session = "";
    }

    @Override
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("DATA", html);
    }

    @Override
    public final void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.notenblatt);

        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_experimental);
        //item.setChecked(true);
        item.getSubMenu().findItem(R.id.nav_notenblatt).setChecked(true);
    }


    @Override
    public void onPause() {
        MainActivity mainActivity = (MainActivity) getActivity();
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_experimental).getSubMenu().findItem(R.id.nav_notenblatt).setChecked(false);
        super.onPause();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notenblatt, container, false);
        webView = (WebView) v.findViewById(R.id.webview);
        webView.getSettings().setSupportZoom(true);
        //webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setDisplayZoomControls(false);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.webViewSwipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });

        if (html.isEmpty()) {
            // Webview zum einloggen
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            final int[] counter = {3};

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    swipeContainer.setRefreshing(true);
                }

                public void onPageFinished(WebView view, String url) {
                    swipeContainer.setRefreshing(false);
                    if (view.getUrl().contains("idp")) {
                        LoginController loginController = LoginController.getInstance(getActivity());
                        if (!loginController.getUsername().isEmpty() && !loginController.getPassword().isEmpty()) {
                            if (counter[0] > 0) {
                                counter[0]--;
                                view.loadUrl("javascript: (function() {document.getElementById('username').value= '" + loginController.getUsername() + "';}) ();");
                                view.loadUrl("javascript: (function() {document.getElementById('password').value= '" + loginController.getPassword() + "';}) ();");
                                view.loadUrl("javascript: (function() {document.getElementsByName('_eventId_proceed')[0].click();}) ();");

                                // Not working
                                //myWebView.loadUrl("javascript: (function() {document.forms[0].submit();}) ();" );
                            }
                        }
                    } else {
                        if (view.getUrl().contains("Session=")) {
                            session = view.getUrl().substring(view.getUrl().indexOf("Session=") + 8, view.getUrl().indexOf("&User"));
                            updateData();
                        }
                    }
                }
            });

            webView.loadUrl(Define.PRIMUSSURL);
        } else {
            webView.loadData(html, "text/html", null);
        }
        return v;
    }


    private void updateData() {
        String[] params = new String[2];
        if (loginController.showDialog()) {
            params[0] = loginController.getUsername();
            params[1] = loginController.getPassword();
            task = new NotenblattFragment.GetNotenblattTask();
            task.execute(params);
        }
    }


    private class GetNotenblattTask extends AsyncTask<String, Void, String> {

        private String errorText = "";

        @Override
        protected final void onPreExecute() {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                }
            });
        }

        @Override
        protected final String doInBackground(String... params) {
            String result = "";
            System.setProperty("jsse.enableSNIExtension", "false");


            try {
                //Login und Session holen
                /*Connection.Response res = Jsoup.connect("https://www1.primuss.de/cgi/Sesam/sesam.pl").
                        data("User", params[0], "Javascript", "1", "Stage", "1", "Password", params[1], "Auth", "radius", "Portal", "1", "FH", "fhh", "Language", "de").
                        method(Connection.Method.POST).timeout(10000).execute();

                // Wir erhalten vom Notenblatt momentan noch eine kompette HTML-Seite
                Document doc = res.parse();
                String session = doc.getElementsByAttributeValueMatching("name", "Session").val();
                String user = doc.getElementsByAttributeValueMatching("name", "User").val();
//                String language = doc.getElementsByAttributeValueMatching("name", "Language").val();
                String fh = doc.getElementsByAttributeValueMatching("name", "FH").val();
                String portal = doc.getElementsByAttributeValueMatching("name", "Portal").val();
                String javascript = doc.getElementsByAttributeValueMatching("name", "Javascript").val();

                //Wenn session leer ist, dann ist der Login fehlgeschlagen
                if (session.isEmpty()) {
                    errorText = getString(R.string.loginFailed);
                    return "";
                }*/

                //Notenblatt
                Connection.Response res4 = Jsoup.connect("https://www1.primuss.de/cgi/pg_Notenblatt/index.pl")
                        //.cookies()
                        //.data("Session", session, "User", user, "Language", "de", "FH", fh, "Portal", portal, "Javascript", javascript)
                        .data("Session", session, "User", params[0], "Language", "de", "FH", "fhh", "Portal", "1")
                        .method(Connection.Method.POST)
                        .execute();
                Document doc2 = res4.parse();
                doc2.getElementsByTag("a").remove();
                result = doc2.html();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);

                if (e.getClass() == InterruptedIOException.class) { //Wurde einfach abgebrochen -> nichts tun
                    return "";
                } else {
                    errorText = getString(R.string.lesenotenblattfehler);
                }
            }
            return result;
        }

        @Override
        protected final void onCancelled() {
            html = "";
            super.onCancelled();
        }

        @Override
        protected final void onPostExecute(String aString) {
            swipeContainer.setRefreshing(false);

            //Wenn es einen Fehler gab -> ausgeben
            if (!errorText.isEmpty()) {
                Toast.makeText(getView().getContext(), errorText, Toast.LENGTH_LONG).show();
            }

            html = aString; //Ergebnis merken, damit beim erneuten Anzeigen nicht neu gelesen werden muss
            webView.loadData(aString, "text/html", null);
        }
    }

}
