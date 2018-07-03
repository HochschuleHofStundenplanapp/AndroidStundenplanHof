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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.experimental.adapter.NotenAdapter;
import de.hof.university.app.experimental.model.Noten;
import de.hof.university.app.util.Define;

import static android.widget.Toast.makeText;


public class NotenbekanntgabeFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "NotenbekanntgabeFragm";

    private NotenAdapter adapter;
    private ArrayList<Noten> items;
    private SwipeRefreshLayout swipeContainer;
    private NotenbekanntgabeFragment.GetNotenTask task;
    private LoginController loginController;

    private String session;

    public NotenbekanntgabeFragment() {
	    super();
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginController = LoginController.getInstance(getActivity());

        items = new ArrayList<>();
        session = "";
    }


    @Override
    public final void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.notenbekanntgabe);

        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_experimental);
        //item.setChecked(true);
        item.getSubMenu().findItem(R.id.nav_notenbekanntgabe).setChecked(true);
    }


    @Override
    public void onPause() {
        MainActivity mainActivity = (MainActivity) getActivity();
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_experimental).getSubMenu().findItem(R.id.nav_notenbekanntgabe).setChecked(false);
        super.onPause();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notenbekanntgabe, container, false);
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });
        adapter = new NotenAdapter(getActivity(), items);
        ListView listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // Webview zum einloggen
        final WebView myWebView = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        final int[] counter = {3};

        myWebView.setWebViewClient(new WebViewClient() {

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

        //Wenn noch keine Daten gelesen wurden
        if (items.isEmpty()) {
            myWebView.loadUrl(Define.PRIMUSSURL);
        }

        return v;
    }

    @Override
    public final void onDestroyView() {
        if (task != null) {
            task.cancel(true);
        }
        swipeContainer.setRefreshing(false);
        super.onDestroyView();
    }

    private void updateData() {
        String[] params = new String[2];
        if (loginController.showDialog()) {
            params[0] = loginController.getUsername();
            params[1] = loginController.getPassword();
            task = new NotenbekanntgabeFragment.GetNotenTask(getActivity());
            task.execute(params);
        }
    }

    private class GetNotenTask extends AsyncTask<String, Void, Void> {

        String errorText = "";
        final Context context;

        GetNotenTask(Context context) {
	        super();
	        this.context = context;
        }

        @Override
        protected final void onPreExecute() {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                }
            });
            items.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected final Void doInBackground(String... params) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            /*
            It enables TLS connections to virtual servers, in which multiple servers for different network names are
            hosted at a single underlying network address
            If you disable jsse.enableSNIExtension you won't be able to connect to pages under a virtual server

            http://docs.oracle.com/javase/7/docs/technotes/guides/security/jsse/JSSERefGuide.html
            Server Name Indication option

            * jsse.enableSNIExtension system property. Server Name Indication (SNI) is a TLS extension,
            * defined in RFC 4366. It enables TLS connections to virtual servers, in which multiple servers
            * for different network names are hosted at a single underlying network address.

            Some very old SSL/TLS vendors may not be able handle SSL/TLS extensions. In this case,
            set this property to false to disable the SNI extension.
             */
            System.setProperty("jsse.enableSNIExtension", "false");

            try {
                /*URL url = new URL("http://ipaddress:port/module/manager");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "*application/json;*charset=UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(request.getBytes());
                os.flush();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                String output;
                StringBuffer sb = new StringBuffer();
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                return sb.toString();*/

                //Login URL holen
                // Try to go to the SP
               /* Connection.Response res0 = Jsoup.connect(Define.PRIMUSSURL).
                        method(Connection.Method.GET).timeout(10000).execute();
                Document doc0 = res0.parse();

                URL idpURL = res0.url();
                Map<String, String> cookies = res0.cookies();

                // Login to the IDP
                HttpsURLConnection idpCon = (HttpsURLConnection)idpURL.openConnection();

                String charset = "UTF-8";

                try {
                    idpCon.setRequestMethod("POST");
                    idpCon.setDoInput(true);
                    idpCon.setDoOutput(true);

                    idpCon.setRequestProperty("Accept-Charset", charset);

                    // Cookies setzen
                    *//*for (String key: cookies.keySet()) {
                        idpCon.addRequestProperty(key, cookies.get(key));
                    }*//*

                    // Login Daten
                    *//*String paramsString = URLEncoder.encode("j_username", charset) + "="+ params[0] + "&" + URLEncoder.encode("j_password", charset) + "=" + URLEncoder.encode(params[1], charset);

                    DataOutputStream wr = new DataOutputStream(idpActionCon.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();*//*

                    String userPassword = params[0] + ":" + params[1];
                    String encoding = Base64.encodeToString(userPassword.getBytes(charset), Base64.DEFAULT);

                    idpCon.setRequestProperty("Authorization", "Basic " + encoding);
                    idpCon.connect();

                    *//*idpLoginRequest.getParams().setBooleanParameter(AUTH_IN_PROGRESS, true);
                    idpLoginRequest.addHeader(HttpHeaders.AUTHORIZATION,
                            "Basic " + Base64.encodeBytes((params[0] + ":" + params[1]).getBytes()));
                    idpLoginRequest.setEntity(new StringEntity(xmlToString(idpLoginSoapRequest)));
                    HttpResponse idpLoginResponse = client.execute(idpLoginRequest);*//*

                    *//*Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password.toCharArray());
                        }
                    });*//*

                    if (idpCon.getResponseCode() == 302) {
                        String newUrl = idpCon.getHeaderField("Location");
                    }


                    idpCon.getURL();

                    InputStream in = new BufferedInputStream(idpCon.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    Log.d(TAG, "result: " + result.toString());
                } finally {
                    idpCon.disconnect();
                }*/



                //Login und Session holen
//                Connection.Response res = Jsoup.connect("https://www1.primuss.de/cgi/Sesam/sesam.pl").
                /*Connection.Response res = Jsoup.connect(idpURL.toString()).
                        cookies(cookies).
                        //data("User", params[0], "Javascript", "1", "Stage", "1", "Password", params[1], "Auth", "radius", "Portal", "1", "FH", "fhh", "Language", "de").
                        //data("j_username", params[0], "j_password", params[1]).
                        method(Connection.Method.POST).timeout(10000).execute();
                Document idpDoc = res.parse();

                *//*Element idpFormElement = idpDoc.select("form").get(0);
                String idpActionPath = idpFormElement.attr("action");

                String idpURLString = idpURL.toString();
                idpURLString = idpURLString.substring(0, idpURLString.indexOf("?"));
                idpURLString += idpActionPath;
                URL idpActionUrl = new URL(idpURLString);*//*

                String session = idpDoc.getElementsByAttributeValueMatching("name", "Session").val();
                String user = idpDoc.getElementsByAttributeValueMatching("name", "User").val();
                String language = idpDoc.getElementsByAttributeValueMatching("name", "Language").val();
                String fh = idpDoc.getElementsByAttributeValueMatching("name", "FH").val();
                String portal = idpDoc.getElementsByAttributeValueMatching("name", "Portal").val();
                String javascript = idpDoc.getElementsByAttributeValueMatching("name", "Javascript").val();

                *//*HttpPost httpPost2 = new HttpPost(idpUrl.replace("/idp/shibboleth", idpActionPath));
                List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>();
                nameValuePairs2.add(new BasicNameValuePair("j_username", username));
                nameValuePairs2.add(new BasicNameValuePair("j_password", password));
                httpPost2.setEntity(new UrlEncodedFormEntity(nameValuePairs2, HTTP.UTF_8));
                HttpResponse response2 = httpClient.execute(httpPost2);
                String strResponse2 = readResponse(response2.getEntity().getContent()).toString();*//*


                //Wenn session leer ist, dann ist der Login fehlgeschlagen
                if (session.isEmpty()) {
                    errorText = getString(R.string.loginFailed);
                    return null;
                }*/


                //Methode mit Webview
                final Map<String, String> cookies = new HashMap<>();

                final String cookiesString = CookieManager.getInstance().getCookie(Define.PRIMUSSURL);

                if (cookiesString == null) {
                    Log.e(TAG, "cookieString ist null");
                    errorText = "Internal (e0): " + getString(R.string.lesenotenbekanntgabefehler);
                    return null;
                }

                final String[] cookiesArray = cookiesString.split(";");

                for (String ar1 : cookiesArray ) {
                    final String[] temp = ar1.split("=");
                    cookies.put(temp[0], temp[1]);
                }

                if (cookies.isEmpty() || session.isEmpty()) {
                    errorText = getString(R.string.loginFailed);
                    return null;
                }

                // Rechtsbelehrung
                //Connection.Response res2 = Jsoup.connect("https://www3.primuss.de/cgi-bin/pg_Notenbekanntgabe/index.pl").data(
                Connection.Response res2 = Jsoup.connect(Define.PRIMUSSRECHTSBELEHRUNGURL)
                        .cookies(cookies)
                        //.data("Session", session, "User", user, "Language", language, "FH", fh, "Portal", portal, "Javascript", javascript)
                        .data("Session", session, "Language", "de", "User", params[0], "FH", "fhh", "Portal", "1")
                        .method(Connection.Method.POST)
                        .timeout(10000)
                        .execute();

                Document doc2 = null;
                try {
                    doc2 = res2.parse();
                } catch ( final IOException e )
                {
                    Log.e(TAG, "Parse error 1", e);
	                errorText = "Internal (e1): "+ getString(R.string.lesenotenbekanntgabefehler);
	                return null;
                }
                final String poison = doc2.getElementsByAttributeValueMatching("name", "Poison").val();

                //Notenbekanntgabe
                //Connection.Response res3 = Jsoup.connect("https://www3.primuss.de/cgi-bin/pg_Notenbekanntgabe/showajax.pl")
                Connection.Response res3 = Jsoup.connect(Define.PRIMUSSNOTENBEKANNTGABEURL)
                        .cookies(cookies)
                        //.data("Language", "de", "Session", session, "Poison", poison, "User", user, "FH", fh, "Accept", "X")
                        .data("Language", "de", "Session", session, "Poison", poison, "User", params[0], "FH", "fhh", "Accept", "X")
                        .method(Connection.Method.GET)
                        .timeout(10000)
                        .execute();

                // falls keine Daten kommen Fehlermeldung anzeigen
	            Document doc3 ;
	            try {
                    doc3 = res3.parse();
	            } catch ( final IOException e )
	            {
		            Log.e(TAG, "Parse error 2", e);
		            errorText = "Internal (e2): "+ getString(R.string.lesenotenbekanntgabefehler);
		            return null;
	            }


	            // Prüfe ob Notenbekanntgabe bereits beendet wurde.
                if (doc3.getElementsByTag("h2").hasClass("error")) {
                    final Elements tr = doc3.getElementsByClass("table1").get(0).getElementsByTag("tr");
                    for (org.jsoup.nodes.Element line : tr) {
                        items.add(new Noten(line.child(0).text(), line.child(1).text()));
                    }
                } else {
                    final SharedPreferences.Editor edit = sp.edit();

	                Elements elements ;
	                try {
		                final Elements eTable = doc3.getElementsByClass("table2");
		                final Element element = eTable.get(0).child(1) ;
	                    elements = element.children();

	                } catch ( final Exception e )
		            {
			            Log.e(TAG, "Parse error 3", e);
			            errorText = "Internal (e3): "+ getString(R.string.lesenotenbekanntgabefehler);
			            return null;
		            }
	                for (int i = 0; i < elements.size(); ++i) {
                    	final String text1 = elements.get(i).child(2).text();
						final String text2 = elements.get(i).child(5).child(0).text();

                    	final Noten noten = new Noten( text1, text2);
                        items.add( noten );
                        edit.putString(String.valueOf(elements.get(i).child(2).text().hashCode()), elements.get(i).child(5).child(0).text());
                    }
                    edit.apply();
                }


                //Notenblatt
//			DBConnection.Response res4 = Jsoup.connect("https://www1.primuss.de/cgi/pg_Notenblatt/index.pl").data(
//					"Session", session, "User", user, "Language", "de", "FH", fh, "Portal", portal, "Javascript", javascript).method(Method.POST).execute();
//			Log.d(TAG, res4.body());
            } catch (final IOException e) {

                if (e.getClass() == InterruptedIOException.class) //Wurde einfach abgebrochen -> nichts tun
                {
                    return null;
                } else {
                    errorText = getString(R.string.lesenotenbekanntgabefehler);
                }
            }
            return null;
        }

        @Override
        protected final void onPostExecute(Void aVoid) {
            swipeContainer.setRefreshing(false);
            adapter.notifyDataSetChanged();

            //Wenn es einen Fehler gab -> ausgeben
            if (!errorText.isEmpty()) {
                makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(aVoid);
        }
    }

}
