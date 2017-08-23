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
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.experimental.adapter.RaumlistAdapter;
import de.hof.university.app.experimental.model.Level;
import de.hof.university.app.experimental.model.Raum;
import de.hof.university.app.experimental.model.Raumkategorie;
import de.hof.university.app.experimental.model.Raumliste;
import de.hof.university.app.experimental.model.Suchdetails;


/**
 * A simple {@link Fragment} subclass.
 */
public class RaumlisteFragment extends Fragment {

    public final static String TAG = "RaumlisteFragment";

    private static final String ARG_PARAM1 = "user";
    private static final String ARG_PARAM2 = "password";
    private static final String ARG_PARAM3 = "year";
    private static final String ARG_PARAM4 = "month";
    private static final String ARG_PARAM5 = "day";
    private static final String ARG_PARAM6 = "timeStart";
    private static final String ARG_PARAM7 = "timeEnd";
    private static final String ARG_PARAM8 = "raumTyp";
    private static final String ARG_PARAM9 = "prettyDate";

    // TODO: Rename and change types of parameters
    private String user;
    private String password;
    private String year;
    private String month;
    private String day;
    private String timeStart;
    private String timeEnd;
    private String raumTyp;
    private String prettyDate;

    private ArrayList<Level> raumList;
    private RaumlistAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    private RaumlisteFragment.GetRaumTask task;


    public RaumlisteFragment() {
        // Required empty public constructor
    }


    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        if (getArguments() != null) {
            user = getArguments().getString(ARG_PARAM1);
            password = getArguments().getString(ARG_PARAM2);
            year = getArguments().getString(ARG_PARAM3);
            month = getArguments().getString(ARG_PARAM4);
            day = getArguments().getString(ARG_PARAM5);
            timeStart = getArguments().getString(ARG_PARAM6);
            timeEnd = getArguments().getString(ARG_PARAM7);
            raumTyp = getArguments().getString(ARG_PARAM8);
            prettyDate = getArguments().getString(ARG_PARAM9);
        }

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        if (swipeContainer != null) {
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateData(true);
                }
            });
        }

        raumList = new ArrayList<>();


        adapter = new RaumlistAdapter(getActivity(), raumList);

        ListView raumListe = (ListView) v.findViewById(R.id.listView);
        raumListe.setAdapter(adapter);

        if (getArguments() != null) {
            //Aktualisieren, wenn noch keine Daten darin stehen. 1. Element ist immer da
            //wegen Kopfzeile mit Infos zur Suchanfrage, daher >2 prüfen
            if (raumList.size() < 2) {
                updateData(false);
            }
        }

        return v;
    }

    @Override
    public final void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.raumsuche);

        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_experimental);
        //item.setChecked(true);
        item.getSubMenu().findItem(R.id.nav_raumsuche).setChecked(true);
    }

    @Override
    public void onPause() {
        MainActivity mainActivity = (MainActivity) getActivity();
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_experimental).getSubMenu().findItem(R.id.nav_raumsuche).setChecked(false);
        super.onPause();
    }

    private void updateData(boolean forceRefresh) {
        String[] params = new String[10];
        params[0] = user;
        params[1] = password;
        params[2] = year;
        params[3] = month;
        params[4] = day;
        params[5] = timeStart;
        params[6] = timeEnd;
        params[7] = raumTyp;
        params[8] = prettyDate;
        params[9] = String.valueOf(forceRefresh);

        task = new RaumlisteFragment.GetRaumTask();
        task.execute(params);
    }

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

    private class GetRaumTask extends AsyncTask<String, Void, ArrayList<Level>> {

        String errorText = "";

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
        protected final ArrayList<Level> doInBackground(String... params) {
            Object optRaumliste = RaumlisteFragment.readObject(getActivity().getApplicationContext(), Define.raumlistFilename);
            Raumliste raumliste = new Raumliste();
            Date lastCached = new Date();

            if (optRaumliste != null) {
                raumliste = (Raumliste) optRaumliste;

                //TODO was ist das? Warum 1? Kann man das wegwerfen?
                // Zeit in Minuten wie lange die Raumliste gecached wird, könnte auch in die Define.
                // 1 Minute da länger nicht von nöten ist. Dann hat man wahrscheinlich eh weil eine andere Zeit.
                // Ist quasi nur falls man gleich hintereinander der selbe Abfrage nochmal macht, dann wird sie gleich wieder angezeigt und nicht neu geholt.
                int raumlisteCache = 1;

                if ( raumliste.getLastSaved() != null ) {
                    lastCached = raumliste.getLastSaved();

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(lastCached);
                    cal.add(Calendar.MINUTE, raumlisteCache);
                    lastCached = cal.getTime();
                }
            }

            boolean forceRefresh = Boolean.valueOf(params[9]);

            if (
                forceRefresh
                || optRaumliste == null
                || raumliste.getLastSaved() == null
                || !lastCached.after(new Date())
                || !raumliste.getTimeStart().equals(params[5])
                || !raumliste.getTimeEnd().equals(params[6])
                || !raumliste.getRaumTyp().equals(params[7])
                || !raumliste.getDate().equals(params[8])
	            ) {

				//TODO hier stand mal ein Kommentar, wofür...
                System.setProperty("jsse.enableSNIExtension", "false");
                String user = params[ 0 ];
                String password = params[ 1 ];
                String year = params[ 2 ];
                String month = params[ 3 ];
                String day = params[ 4 ];
                String timeFrom = params[ 5 ];
                String timeTo = params[ 6 ];
                String raumTyp = params[ 7 ];
                String prettyDate = params[ 8 ];

                ArrayList<Level> tmpRaumList = new ArrayList<>();

                tmpRaumList.add(new Suchdetails(getString(R.string.date) + ' ' + prettyDate, getString(R.string.timeFrom) + ": " + timeFrom, getString(R.string.timeTo) + ": " + timeTo));

                Connection.Response loginForm = null;
                Document document;

                // TODO Temporäre Lösung durch gleich mehrere Versuche.
                int loginRetry = 2;

                while ( loginRetry > 0 ) {
                    try {
                        // Thread beenden wenn gecancelt
                        if ( isCancelled() ) break;
                        loginForm = Jsoup
                                            .connect(Define.URL_RAUMSUCHE_LOGIN)
                                            .timeout(5 * 1000) //4 Sekunden würden reichen aber 1 Sekunde zur Sicherheit
                                            .data("user", user, "pass", password)
                                            .data("logintype", "login")
                                            .data("pid", "27")
                                            .data("redirect_url", Define.URL_RAUMSUCHE_LOGIN_SUCCESS)
                                            .data("tx_felogin_pi1[noredirect]", "0")
                                            .method(Connection.Method.POST).execute();
                        loginRetry = 0;
                    } catch ( IOException e ) {
                        loginRetry--;
                        if ( loginRetry <= 0 ) {
                            errorText = getString(R.string.loginFailed);
                            return null;
                        }
                    }
                }

                // Daten lesen
                try {
                    document = Jsoup
                       .connect( Define.URL_RAUMSUCHE + raumTyp)
                       .timeout(10 * 1000) //5 Sekunden würden reichen aber auf älteren Geräten braucht es mehr
                       .data("tx_raumsuche_pi1[day]", day)
                       .data("tx_raumsuche_pi1[month]", month)
                       .data("tx_raumsuche_pi1[year]", year)
                       .data("tx_raumsuche_pi1[timestart]",timeFrom)
                       .data("tx_raumsuche_pi1[timeend]", timeTo)
                       .cookies(loginForm.cookies()).get();

                    final Elements tables = document.getElementsByTag("table");
                    String curCategory = "";
                    for ( Element td : tables.first().getElementsByTag("td") ) {
                        // Thread beenden wenn gecancelt
                        if ( isCancelled() ) break;
                        String room = td.text();
                    /* Füge Kategorie ein, wenn:
                            wenn Stringlänge > 2    >> da Kategorie 2 Zeichen
                            neue Kategorie != curCategory
                     */
                        if ( (room.length() > 2) && !room.substring(0, 2).equals(curCategory) ) {
                            room = room.substring(0, 2); // Erzeuge Kategorie
                            tmpRaumList.add(new Raumkategorie(room));
                            curCategory = room; // Lege neue Kategorie fest
                        }
                        tmpRaumList.add(new Raum(td.text()));
                    }

                } catch ( Exception e ) { // eigentlich IOException, aber es kann auch NullPointer wenn Login fehlgeschlagen ist
                    if ( e.getClass() == InterruptedIOException.class ) //Wurde einfach abgebrochen -> nichts tun
                    {
                        errorText = getString(R.string.raumsuchefehler);
                        return null;
                    } else {
                        errorText = getString(R.string.raumsuchefehler);
                    }
                }

                // Log.d(TAG, loginForm.cookie("fe_typo_user"));

                raumliste.setRaumlist(tmpRaumList);

                raumliste.setTimeStart(params[5]);
                raumliste.setTimeEnd(params[6]);
                raumliste.setRaumTyp(params[7]);
                raumliste.setDate(params[8]);

                raumliste.setLastSaved(new Date());
                RaumlisteFragment.saveObject(getActivity().getApplicationContext(), raumliste, Define.raumlistFilename);
            }

            return raumliste.getRaumlist();
        }

        @Override
        protected final void onPostExecute(ArrayList<Level> result) {
            swipeContainer.setRefreshing(false);

            // Wenn ein schlimmer Fehler passiert ist, dann kann das Objekt null sein
            if (result != null && errorText.isEmpty()) {
                if (result.size() > 1) {
                    raumList.clear();
                    raumList.addAll(result);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.keineraeume), Toast.LENGTH_LONG).show();
                }
            } else {
                //Wenn es einen Fehler gab -> ausgeben
                Toast.makeText(getActivity().getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(result);
        }
    }

	//TODO saveObject
    private static void saveObject(final Context context, final Object object, final String filename) {
        try {
            final File file = new File(context.getFilesDir(), filename);
            final FileOutputStream fos = new FileOutputStream(file);
            final ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (IOException e) {
            // TODO Fehlermeldung
            Log.e(TAG, "saveObject failed", e);
        }
    }

    private static Object readObject(final Context context, final String filename) {
        Object result = null;
        try {
            final File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                final FileInputStream fis = new FileInputStream(file);
                final ObjectInputStream is = new ObjectInputStream(fis);
                result = is.readObject();
                is.close();
                fis.close();
            }
        } catch (final Exception e) {
            // TODO Fehlermeldung
            Log.e(TAG, "readObject failed", e);
        }
        return result;
    }
}
