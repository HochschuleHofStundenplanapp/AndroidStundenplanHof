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

package de.hof.university.app.fragment.schedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.adapter.ScheduleAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.AbstractListFragment;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.schedule.Schedule;


public class ScheduleFragment extends AbstractListFragment {
    private int weekdayListPos;

    //TODO onCreateView?
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        weekdayListPos=0;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Versuch den Fehler mit alten Fragment im Hintergrund zu beheben
        /*if (container != null) {
            container.removeAllViews();
        }*/
        View v = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(listView);
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if(v.getId()==R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            Schedule schedule = (Schedule) listView.getItemAtPosition(info.position);

            final DataManager dm = DataManager.getInstance();

            //Wenn noch nicht im Mein Stundenplan -> hinzufügen anzeigen
            if(!dm.myScheduleContains(v.getContext(), schedule)) {
                menu.setHeaderTitle(R.string.myschedule);
                menu.add(Menu.NONE, 0, 0, R.string.addToMySchedule);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Schedule schedule = (Schedule) listView.getItemAtPosition(info.position);

        if(item.getTitle().equals(getString(R.string.addToMySchedule))) {
            DataManager.getInstance().addToMySchedule(info.targetView.getContext(), schedule);
        }

        if(item.getTitle().equals(getString(R.string.deleteFromMySchedule))) {
            DataManager.getInstance().deleteFromMySchedule(info.targetView.getContext(), schedule);
        }

        return true;

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.stundenplan);

        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_stundenplan).setChecked(true);
    }

    @Override
    protected final ArrayAdapter setArrayAdapter() {
        return new ScheduleAdapter(getActivity(), dataList);
    }

    @Override
    protected final String[] setTaskParameter(boolean forceRefresh) {
        String[] params = new String[4];
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String course = sharedPref.getString("studiengang", "");
        final String semester = sharedPref.getString("semester", "");
        final String termTime = sharedPref.getString("term_time","");

        if(termTime.isEmpty()){
            Toast.makeText(getView().getContext(), getString(R.string.noTermTimeSelected), Toast.LENGTH_LONG).show();
            return null;
        }

        if(course.isEmpty()){
            Toast.makeText(getView().getContext(), getString(R.string.noCourseSelected), Toast.LENGTH_LONG).show();
            return null;
        }
        if(semester.isEmpty()){
            Toast.makeText(getView().getContext(), getString(R.string.noSemesterSelected), Toast.LENGTH_LONG).show();
            return null;
        }

        // TODO Warum geben wir eine Parametersammlung zurück?
        params[0] = course;
        params[1] = semester;
        params[2] = termTime;
        params[3] = String.valueOf(forceRefresh);
        return params;
    }

    protected final ArrayList<Object> updateListView(List<Object> list) {
        final String curWeekDay = new SimpleDateFormat("EEEE", Locale.GERMANY).format(new Date());
        String weekday = "";

        // Temporäre Liste für die neuen Vorlesungen damit sie erst später in ListView hinzugefügt werden können
        ArrayList<Object> tmpDataList = new ArrayList<>();

        // Temporäre Liste für die Vorlesungen die nur an einem Tag stattfinden (fix) damit sie am Ende angezeigt werden.
        ArrayList<Schedule> fixDataList = new ArrayList<>();
        for (Object object : list) {
            if(object instanceof Schedule) {
                Schedule schedule = (Schedule)object;
                // Wenn eine Vorlesung nur an einem Tag stattfindet sind Start- und Enddate gleich
                if (schedule.getStartdate().equals(schedule.getEnddate())) {
                    fixDataList.add(schedule);
                } else {
                    if (!weekday.equals(schedule.getWeekday())) {
                        tmpDataList.add(new BigListItem(schedule.getWeekday()));
                        weekday = schedule.getWeekday();
                        if (weekday.equalsIgnoreCase(curWeekDay)) {
                            weekdayListPos = tmpDataList.size() - 1;
                        }
                    }
                    tmpDataList.add(schedule);
                }
            }
        }
        // sortieren
        Collections.sort(fixDataList);
        ArrayList<Object> sortDataList = new ArrayList<>();
        String date = "";
        for (Schedule schedule : fixDataList) {
            if (!date.equals(schedule.getStartdate())) {
                sortDataList.add(new BigListItem(schedule.getStartdate()));
                date = schedule.getStartdate();
            }
            sortDataList.add(schedule);
        }

        /*dataList.clear();
        dataList.addAll(tmpDataList);
        dataList.addAll(sortDataList);
        */

        tmpDataList.addAll(sortDataList);

        return tmpDataList;
    }

    @Override
    protected final void modifyListViewAfterDataSetChanged() {
        listView.setSelection(weekdayListPos);
    }


    /**
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity mainActivity = (MainActivity) getActivity();
        CharSequence title = mainActivity.getSupportActionBar().getTitle();
        if (title.equals(getString(R.string.stundenplan))) {
            inflater.inflate(R.menu.schedule_main, menu);
        }
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        if (item.getItemId() == R.id.action_add_all) {
            ArrayList<String> schedulesIds = new ArrayList<>();
            for (Object object : dataList) {
                if (object instanceof Schedule) {
                    Schedule schedule = (Schedule)object;
                    schedulesIds.add(String.valueOf(schedule.getId()));
                }
            }
            DataManager.getInstance().addAllToMySchedule(getView().getContext(), schedulesIds);
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected ArrayList<Object> background(String[] params) {
        final String course = params[0];
        final String semester = params[1];
        final String termTime = params[2];
        List<Object> scheduleList = DataManager.getInstance().getSchedule(getActivity().getApplicationContext(), course, semester, termTime, Boolean.valueOf(params[3]));

        if (scheduleList != null) {
            return this.updateListView(scheduleList);
        } else {
            return null;
        }
    }
}
