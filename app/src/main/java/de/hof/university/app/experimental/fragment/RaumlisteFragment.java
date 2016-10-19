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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import de.hof.university.app.R;
import de.hof.university.app.experimental.adapter.RaumlistAdapter;
import de.hof.university.app.experimental.model.Level;
import de.hof.university.app.experimental.model.Raum;
import de.hof.university.app.experimental.model.Raumkategorie;
import de.hof.university.app.experimental.model.Suchdetails;


/**
 * A simple {@link Fragment} subclass.
 */
public class RaumlisteFragment extends Fragment {

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
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });

        raumList = new ArrayList<Level>();


        adapter = new RaumlistAdapter(getActivity(), raumList);

        ListView raumListe = (ListView) v.findViewById(R.id.listView);
        raumListe.setAdapter(adapter);

        //Aktualisieren, wenn noch keine Daten darin stehen. 1. Element ist immer da
        //wegen Kopfzeile mit Infos zur Suchanfrage, daher >2 prüfen
        if(raumList.size()<2) {
            updateData();
        }

        return v;
    }

    @Override
    public final void onResume() {
        super.onResume();
    }

    private void updateData(){
        String[] params = new String[ 9 ];
        params[0]=user;
        params[1]=password;
        params[2]=year;
        params[3]=month;
        params[4]=day;
        params[5]=timeStart;
        params[6]=timeEnd;
        params[7]=raumTyp;
        params[8]=prettyDate;

        task = new RaumlisteFragment.GetRaumTask();
        task.execute(params);
    }

    @Override
    public final void onDestroyView() {
        if(task!=null) {
            task.cancel(true);
        }
        swipeContainer.setRefreshing(false);
        super.onDestroyView();
    }

    private class GetRaumTask extends AsyncTask<String, Void, Void> {

        String errorText = "";

        @Override
        protected final void onPreExecute() {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                }
            });
            raumList.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected final Void doInBackground(String... params) {

            System.setProperty("jsse.enableSNIExtension", "false");
            String user = params[0];
            String password = params[1];
            String year = params[2];
            String month = params[3];
            String day = params[4];
            String timeFrom = params[5];
            String timeTo = params[6];
            String raumTyp = params[7];
            String prettyDate = params[8];

            raumList.add(new Suchdetails(getString(R.string.date)+ ' ' +prettyDate,getString(R.string.timeFrom)+": "+ timeFrom,getString(R.string.timeTo)+": "+ timeTo));

            Connection.Response loginForm;
            Document document;

            try {
                loginForm = Jsoup
                        .connect("https://www.hof-university.de/anmelden.html")
                        .data("user", user, "pass", password)
                        .data("logintype", "login")
                        .data("pid", "27")
                        .data("redirect_url",
                                "http://www.hof-university.de/anmeldung-erfolgreich.html")
                        .data("tx_felogin_pi1[noredirect]", "0")
                        .method(Connection.Method.POST).execute();
            } catch (IOException e) {
                errorText="Fehler beim Login aufgetreten";
                return null;
            }


            // Daten lesen
            try {
                document = Jsoup
                        .connect(
                                "https://www.hof-university.de/studierende/info-service/it-service/raumhardsoftwaresuche.html"
                                        + raumTyp)
                        .timeout(10 * 1000)
                        .data("tx_raumsuche_pi1[day]", day)
                        .data("tx_raumsuche_pi1[month]", month)
                        .data("tx_raumsuche_pi1[year]", year)
                        .data("tx_raumsuche_pi1[timestart]",
                                timeFrom)
                        .data("tx_raumsuche_pi1[timeend]", timeTo)
                        .cookies(loginForm.cookies()).get();

                Elements tables = document.getElementsByTag("table");
                String curCategory = "";
                for (Element td : tables.first().getElementsByTag("td")) {
                    String room = td.text();
                    /* Füge Kategorie ein, wenn:
                            wenn Stringlänge > 2    >> da Kategorie 2 Zeichen
                            neue Kategorie != curCategory
                     */
                    if( (room.length() > 2) && !room.substring(0, 2).equals(curCategory) ){
                        room = room.substring(0,2); // Erzeuge Kategorie
                        raumList.add(new Raumkategorie(room));
                        curCategory = room; // Lege neue Kategorie fest
                    }
                    raumList.add(new Raum(td.text()));
                }

            } catch (IOException e) {
                if( e.getClass() == InterruptedIOException.class ) //Wurde einfach abgebrochen -> nichts tun
                {
                    return null;
                } else {
                    errorText = getString(R.string.raumsuchefehler);
                }
            }

            // System.out.println(loginForm.cookie("fe_typo_user"));




            return null;
        }

        @Override
        protected final void onPostExecute(Void aVoid) {
            swipeContainer.setRefreshing(false);
            adapter.notifyDataSetChanged();

            //Wenn es einen Fehler gab -> ausgeben
            if ( errorText.isEmpty() ) {
                if ( raumList.isEmpty() ) {
                    Toast.makeText(getView().getContext(), getString(R.string.keineraeume), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getView().getContext(), errorText, Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(aVoid);
        }
    }
}
