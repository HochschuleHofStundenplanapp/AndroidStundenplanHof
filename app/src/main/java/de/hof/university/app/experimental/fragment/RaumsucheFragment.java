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


import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.experimental.LoginController;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RaumsucheFragment#} factory method to
 * create an instance of this fragment.
 */
public class RaumsucheFragment extends android.support.v4.app.Fragment {

    public final static String TAG = "RaumsucheFragment";

    // TODO: Rename and change types of parameters
    private EditText edt_date = null;
    private EditText edt_from = null;
    private EditText edt_to = null;
    private CheckBox cb_edv = null;
    private DatePickerDialog datePicker = null;
    private TimePickerDialog timeFromPicker = null;
    private TimePickerDialog timeToPicker = null;
    private DateFormat dateFormatter = null;
    private DateFormat timeFormatter = null;

    private Calendar cacheDate;
    private Calendar cacheTimeFrom;
    private Calendar cacheTimeTo;

    private LoginController loginController = null;

    public RaumsucheFragment() {
	    super();
	    cacheDate = Calendar.getInstance();
	    cacheTimeFrom = Calendar.getInstance();
	    cacheTimeTo = Calendar.getInstance();
	    cacheTimeTo.add(Calendar.HOUR_OF_DAY, 1); // für differenz der beiden DatePicker
	    cacheTimeTo.add(Calendar.MINUTE, 30);
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginController = LoginController.getInstance(getActivity());
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

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_raumsuche, container, false);

        dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

        cb_edv = (CheckBox) v.findViewById(R.id.cb_edv);
        edt_date = (EditText) v.findViewById(R.id.edt_date);
        edt_from = (EditText) v.findViewById(R.id.edt_from);
        edt_to = (EditText) v.findViewById(R.id.edt_to);

        edt_date.setInputType(InputType.TYPE_NULL);
        edt_from.setInputType(InputType.TYPE_NULL);
        edt_to.setInputType(InputType.TYPE_NULL);

        //***** EditText Felder mit Werten vorbelegen.
        edt_date.setText(dateFormatter.format(cacheDate.getTime()));
        edt_from.setText(timeFormatter.format(cacheTimeFrom.getTime()));
        edt_to.setText(timeFormatter.format(cacheTimeTo.getTime()));


        //***** OnclickListener für EditText setzen
        edt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.updateDate(cacheDate.get(Calendar.YEAR), cacheDate.get(Calendar.MONTH), cacheDate.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });
        edt_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeFromPicker.updateTime(cacheTimeFrom.get(Calendar.HOUR_OF_DAY), cacheTimeFrom.get(Calendar.MINUTE));
                timeFromPicker.show();
            }
        });
        edt_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeToPicker.updateTime(cacheTimeTo.get(Calendar.HOUR_OF_DAY), cacheTimeTo.get(Calendar.MINUTE));
                timeToPicker.show();
            }
        });

        // ******* Picker erstellen und aktuelle Zeit vorbelegen
        datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                cacheDate = Calendar.getInstance();
                cacheDate.set(year, monthOfYear, dayOfMonth);
                edt_date.setText(dateFormatter.format(cacheDate.getTime()));
            }
        }, cacheDate.get(Calendar.YEAR), cacheDate.get(Calendar.MONTH), cacheDate.get(Calendar.DAY_OF_MONTH));

        /*
          Picker from-Time
         */
        timeFromPicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                cacheTimeFrom.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cacheTimeFrom.set(Calendar.MINUTE, minute);
                edt_from.setText(timeFormatter.format(cacheTimeFrom.getTime()));
                timeToPicker.show(); // Öffne Automatisch den To-Time Dialog
            }
        }, cacheTimeFrom.get(Calendar.HOUR_OF_DAY), cacheTimeFrom.get(Calendar.MINUTE), true);
        timeFromPicker.setTitle(getString(R.string.timeFrom));

        /*
          Picker to-Time
         */
        timeToPicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                cacheTimeTo.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cacheTimeTo.set(Calendar.MINUTE, minute);

                edt_to.setText(timeFormatter.format(cacheTimeTo.getTime()));
            }
        }, cacheTimeTo.get(Calendar.HOUR_OF_DAY), cacheTimeTo.get(Calendar.MINUTE), true);
        timeToPicker.setTitle(getString(R.string.timeTo));


        //Onclick für Button setzen
        Button btnSearch = (Button) v.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cacheTimeFrom.after(cacheTimeTo)) {
                    Calendar tmp = cacheTimeFrom;
                    cacheTimeFrom = cacheTimeTo;
                    cacheTimeTo = tmp;

                    edt_from.setText(timeFormatter.format(cacheTimeFrom.getTime()));
                    edt_to.setText(timeFormatter.format(cacheTimeTo.getTime()));
                }
                /*if(Calendar.getInstance().after(cacheTimeFrom)){
                    cacheDate.add(Calendar.DAY_OF_MONTH, 1);
                    edt_date.setText(dateFormatter.format(cacheDate.getTime()));
                }*/


                /*
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
                String user = sharedPref.getString("username", "");
                String pswd = sharedPref.getString("password", "");
                if(user.isEmpty() || pswd.isEmpty() ){
                    Toast.makeText(getView().getContext(), getString(R.string.primussuserLoginFailed), Toast.LENGTH_LONG).show();
                    return;
                }
                */

                if (loginController.showDialog()) {
                    Bundle b = new Bundle();
                    b.putString("user", loginController.getUsername());
                    b.putString("password", loginController.getPassword());
                    try {
                        Calendar tmpCal = Calendar.getInstance();
                        tmpCal.setTime(dateFormatter.parse(edt_date.getText().toString()));
                        b.putString("year", String.valueOf(tmpCal.get(Calendar.YEAR)));
                        b.putString("month", String.valueOf(tmpCal.get(Calendar.MONTH) + 1));
                        b.putString("day", String.valueOf(tmpCal.get(Calendar.DAY_OF_MONTH)));
                        b.putString("timeStart", edt_from.getText().toString());
                        b.putString("timeEnd", edt_to.getText().toString());
                        if (cb_edv.isChecked()) {
                            b.putString("raumTyp", "#edvroomsearch");
                        } else {
                            b.putString("raumTyp", "#roomsearch");
                        }
                        b.putString("prettyDate", edt_date.getText().toString());
                        RaumlisteFragment raumFragment = new RaumlisteFragment();
                        raumFragment.setArguments(b);
                        getFragmentManager().beginTransaction().replace(R.id.content_main, raumFragment).addToBackStack(null).commit();
                    } catch (final ParseException e) {
                        Toast.makeText(getView().getContext(), getString(R.string.raumsuchefehler), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return v;
    }
}
