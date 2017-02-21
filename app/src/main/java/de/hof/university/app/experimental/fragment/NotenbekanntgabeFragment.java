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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.experimental.adapter.NotenAdapter;
import de.hof.university.app.experimental.model.Noten;


public class NotenbekanntgabeFragment extends Fragment {

    public final static String TAG = "NotenbekanntgabeFragment";

    private NotenAdapter adapter;
    private ArrayList<Noten> items;
    private SwipeRefreshLayout swipeContainer;
    private NotenbekanntgabeFragment.GetNotenTask task;
    private LoginController loginController;

    public NotenbekanntgabeFragment() {
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginController = LoginController.getInstance(getActivity());

        items = new ArrayList<>();
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
        View v = inflater.inflate(R.layout.fragment_list, container, false);
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

        //Wenn noch keine Daten gelesen wurden
        if (items.isEmpty()) {
            updateData();
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

        public GetNotenTask(Context context) {
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
                //Login und Session holen
                Connection.Response res = Jsoup.connect("https://www1.primuss.de/cgi/Sesam/sesam.pl").
                        data("User", params[0], "Javascript", "1", "Stage", "1", "Password", params[1], "Auth", "radius", "Portal", "1", "FH", "fhh", "Language", "de").
                        method(Connection.Method.POST).timeout(10000).execute();
                Document doc = res.parse();

                String session = doc.getElementsByAttributeValueMatching("name", "Session").val();
                String user = doc.getElementsByAttributeValueMatching("name", "User").val();
                String language = doc.getElementsByAttributeValueMatching("name", "Language").val();
                String fh = doc.getElementsByAttributeValueMatching("name", "FH").val();
                String portal = doc.getElementsByAttributeValueMatching("name", "Portal").val();
                String javascript = doc.getElementsByAttributeValueMatching("name", "Javascript").val();

                //Wenn session leer ist, dann ist der Login fehlgeschlagen
                if (session.isEmpty()) {
                    errorText = getString(R.string.loginFailed);
                    return null;
                }

                //Rechtsbelehrung
                Connection.Response res2 = Jsoup.connect("https://www3.primuss.de/cgi-bin/pg_Notenbekanntgabe/index.pl").data(
                        "Session", session, "User", user, "Language", language, "FH", fh, "Portal", portal, "Javascript", javascript).method(Connection.Method.POST).timeout(10000).execute();
                Document doc2 = res2.parse();
                String poison = doc2.getElementsByAttributeValueMatching("name", "Poison").val();

                //Notenbekanntgabe
                Connection.Response res3 = Jsoup.connect("https://www3.primuss.de/cgi-bin/pg_Notenbekanntgabe/showajax.pl").data(
                        "Language", "de", "Session", session, "Poison", poison, "User", user, "FH", fh, "Accept", "X").method(Connection.Method.GET).timeout(10000).execute();

                // TODO falls keine Daten kommen Fehlermeldung anzeigen

                SharedPreferences.Editor edit = sp.edit();
                Document doc3 = res3.parse();
                // Prüfe ob Notenbekanntgabe bereits beendet wurde.
                if (doc3.getElementsByTag("h2").hasClass("error")) {
                    Elements tr = doc3.getElementsByClass("table1").get(0).getElementsByTag("tr");
                    for (org.jsoup.nodes.Element line : tr) {
                        items.add(new Noten(line.child(0).text(), line.child(1).text()));
                    }
                } else {
                    Elements elements = doc3.getElementsByClass("table2").get(0).child(1).children();
                    for (int i = 0; i < elements.size(); ++i) {
                        items.add(new Noten(elements.get(i).child(2).text(), elements.get(i).child(5).child(0).text()));
                        edit.putString(String.valueOf(elements.get(i).child(2).text().hashCode()), elements.get(i).child(5).child(0).text());
                    }
                    edit.apply();
                }


                //Notenblatt
//			DBConnection.Response res4 = Jsoup.connect("https://www1.primuss.de/cgi/pg_Notenblatt/index.pl").data(
//					"Session", session, "User", user, "Language", "de", "FH", fh, "Portal", portal, "Javascript", javascript).method(Method.POST).execute();
//			System.out.println(res4.body());
            } catch (IOException e) {

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
                Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(aVoid);
        }
    }

}
