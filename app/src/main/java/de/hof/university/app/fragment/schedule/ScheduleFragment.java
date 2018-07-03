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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.adapter.ScheduleAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.ChatFragment;
import de.hof.university.app.fragment.AbstractListFragment;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.LastUpdated;
import de.hof.university.app.model.schedule.LectureItem;


public class ScheduleFragment extends AbstractListFragment {

    public static final String TAG = "ScheduleFragment";

    private int weekdayListPos;

    //TODO onCreateView?
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        weekdayListPos = 0;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView myTextv = view.findViewById(R.id.stundenplan_details);
                Log.d("onItemClick", "i was clicked m8");
                Log.d("onItemClick", "v: " + myTextv.getText() );

                //ChatFragment chatfrgmnt = (ChatFragment) getFragmentManager().findFragmentById(R.id.chatfragment);
                LectureItem obc = (LectureItem) dataList.get(i);
                ChatFragment chatfrgmnt = ChatFragment.newInstance(obc.getId());
                if (chatfrgmnt != null){

                    // Execute a transaction, replacing any existing
                    // fragment with this one inside the frame.
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, chatfrgmnt);
                    ft.commit();
                }
            }
        });
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

            Object item = listView.getItemAtPosition(info.position);

            if (item instanceof LectureItem) {
                LectureItem lectureItem = (LectureItem) item;

                final DataManager dm = DataManager.getInstance();

                //Wenn noch nicht im Mein Stundenplan -> hinzufügen anzeigen
                if (!dm.myScheduleContains(v.getContext(), lectureItem)) {
                    menu.setHeaderTitle(R.string.myschedule);
                    menu.add(Menu.NONE, 0, 0, R.string.addToMySchedule);
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {

        junit.framework.Assert.assertTrue( item != null );

        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final LectureItem lectureItem = (LectureItem) listView.getItemAtPosition(info.position);

        if (item.getTitle().equals(getString(R.string.addToMySchedule))) {
            DataManager.getInstance().addToMySchedule(info.targetView.getContext().getApplicationContext(), lectureItem);
            Toast.makeText(getView().getContext(), getString(R.string.added), Toast.LENGTH_SHORT).show();
            if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 1) {
                Toast.makeText(getView().getContext(), getString(R.string.changesMyScheduleText), Toast.LENGTH_LONG).show();
            }
        }

        // Hier unnötig wird in MyScheduleFragment behandelt
        /*if(item.getTitle().equals(getString(R.string.deleteFromMySchedule))) {
            DataManager.getInstance().deleteFromMySchedule(info.targetView.getContext(), lectureItem);
        }*/

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getClass().getSimpleName().equals(ScheduleFragment.class.getSimpleName())) {
            final MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.getSupportActionBar().setTitle(R.string.stundenplan);

            final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
            navigationView.getMenu().findItem(R.id.nav_stundenplan).setChecked(true);

            // fragen, ob die Push Notifications aktiviert werden sollen
            final MainActivity mActivity = (MainActivity) getActivity();
            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
            mActivity.showPushNotificationDialog(sharedPreferences);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        final MainActivity mainActivity = (MainActivity) getActivity();
        final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_stundenplan).setChecked(false);
    }

    @Override
    protected final ArrayAdapter setArrayAdapter() {
        return new ScheduleAdapter(getActivity(), dataList);
    }

    @Override
    protected final String[] setTaskParameter(boolean forceRefresh) {

        final String[] params = new String[4];
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String course = sharedPref.getString(getString(R.string.PREF_KEY_STUDIENGANG), "");
        final String semester = sharedPref.getString(getString(R.string.PREF_KEY_SEMESTER), "");
        final String termTime = sharedPref.getString(getString(R.string.PREF_KEY_TERM_TIME), "");

        // Meldungen nur bringen wenn im Stundenplan Fragment
        if (this.getClass().getSimpleName().equals(ScheduleFragment.class.getSimpleName())) {
            if (termTime.isEmpty()) {
                Toast.makeText(getView().getContext(), getString(R.string.noTermTimeSelected), Toast.LENGTH_LONG).show();
                return null;
            }

            if (course.isEmpty()) {
                Toast.makeText(getView().getContext(), getString(R.string.noCourseSelected), Toast.LENGTH_LONG).show();
                return null;
            }
            if (semester.isEmpty()) {
                Toast.makeText(getView().getContext(), getString(R.string.noSemesterSelected), Toast.LENGTH_LONG).show();
                return null;
            }
        }

        params[0] = course;
        params[1] = semester;
        params[2] = termTime;
        params[3] = String.valueOf(forceRefresh);
        return params;
    }

    final ArrayList<Object> updateListView(List<LectureItem> list) {
        String weekday = "";
        final String curWeekDay = new SimpleDateFormat("EEEE", DataManager.getInstance().getLocale()).format(new Date());

        // Temporäre Liste für die neuen Vorlesungen damit sie erst später in ListView hinzugefügt werden können
        ArrayList<Object> tmpDataList = new ArrayList<>();

        // Temporäre Liste für die Vorlesungen die nur an einem Tag stattfinden (fix) damit sie am Ende angezeigt werden.
        final ArrayList<LectureItem> fixDataList = new ArrayList<>();
        for ( final LectureItem lectureItem : list ) {
            // Wenn eine Vorlesung nur an einem Tag stattfindet sind Start- und Enddate gleich
            final String startDate = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale()).format(lectureItem.getStartDate());
            final String endDate   = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale()).format(lectureItem.getEndDate());

            if ( startDate.equals(endDate) ) {
                fixDataList.add(lectureItem);
            } else {
                if ( !weekday.equals(lectureItem.getWeekday()) ) {
                    tmpDataList.add(new BigListItem(lectureItem.getWeekday()));
                    weekday = lectureItem.getWeekday();
                    if ( weekday.equalsIgnoreCase(curWeekDay) ) {
                        weekdayListPos = tmpDataList.size() - 1;
                    }
                }
                tmpDataList.add(lectureItem);
            }
        }

        // sortieren
        Collections.sort(fixDataList);
        ArrayList<Object> sortDataList = new ArrayList<>();
        if (!fixDataList.isEmpty()) {
            String tmpStartDate = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale()).format(fixDataList.get(0).getStartDate());
            sortDataList.add(new BigListItem(tmpStartDate));
            for (final LectureItem lectureItem : fixDataList) {
                final String startDate = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale()).format(lectureItem.getStartDate());
                if (!tmpStartDate.equals(startDate)) {
                    sortDataList.add(new BigListItem(startDate));
                    tmpStartDate = DateFormat.getDateInstance(DateFormat.DEFAULT, DataManager.getInstance().getLocale()).format(lectureItem.getStartDate());
                }
                sortDataList.add(lectureItem);
            }
            tmpDataList.addAll(sortDataList);
        }

        // Wenn Daten gekommen sind das ListItem LastUpdated hinzufügen
        if (!tmpDataList.isEmpty()) {
            tmpDataList.add(new LastUpdated(getString(R.string.lastUpdated) + ": " + getLastSaved()));
        }

        return tmpDataList;
    }

    /**
     * gibt das Datum zurück wann der Stundenplan zuletzt geholt wurde
     * @return lastSaved
     */
    String getLastSaved() {
        return DataManager.getInstance().formatDate(DataManager.getInstance().getScheduleLastSaved());
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
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final MainActivity mainActivity = (MainActivity) getActivity();
        final CharSequence title = mainActivity.getSupportActionBar().getTitle();

        junit.framework.Assert.assertTrue( title.length() > 0 );

        if (getString(R.string.stundenplan).contentEquals(title)) {
            inflater.inflate(R.menu.schedule_main, menu);
        }
    }

    /**
     * @param item
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected( final MenuItem item) {
        // handle item selection
        if (item.getItemId() == R.id.action_add_all) {

            final Set<String> schedulesIds = new HashSet<>();
            for (final Object object : dataList) {
                if (object instanceof LectureItem ) {
                    final LectureItem lectureItem = (LectureItem) object;
                    final String scheduleID = String.valueOf(lectureItem.getId());
                    // Haben wir eine ID erhalten oder doch nur eine "null"
                    junit.framework.Assert.assertTrue( ! "null".equals(scheduleID) );
                    schedulesIds.add( scheduleID );
                }
            }
            DataManager.getInstance().addAllToMySchedule(getActivity().getApplicationContext(), schedulesIds);

            Toast.makeText(getView().getContext(), getString(R.string.changesMyScheduleText), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected ArrayList<Object> background(final String[] params) {
        final String course = params[0];
        final String semester = params[1];
        final String termTime = params[2];
        final boolean bForceRefresh = Boolean.valueOf(params[3]);

        final List<LectureItem> scheduleList = DataManager.getInstance().getSchedule(
        		getActivity().getApplicationContext(),
		        getString(R.string.language), course, semester, termTime, bForceRefresh );

        if (scheduleList != null) {
            // wenn etwas zurück kommt, dann aktualisiere die View
            return this.updateListView(scheduleList);
        } else {
            // wenn nichts zurück kommt returne null, damit ein Toast angezeigt wird
            return null;
        }
    }
}
