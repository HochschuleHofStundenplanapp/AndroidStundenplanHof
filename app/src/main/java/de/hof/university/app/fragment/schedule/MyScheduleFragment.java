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

import android.support.design.widget.NavigationView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Lukas on 22.06.2016.
 */
public class MyScheduleFragment extends ScheduleFragment {

    public final String TAG = "MyScheduleFragment";

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            LectureItem lectureItem = (LectureItem) listView.getItemAtPosition(info.position);

            DataManager dm = DataManager.getInstance();

            //Wenn in Mein Stundenplan enthalten -> löschen anzeigen
            if (dm.myScheduleContains(v.getContext(), lectureItem)) {
                menu.setHeaderTitle(R.string.myschedule);
                menu.add(Menu.NONE, 0, 0, R.string.deleteFromMySchedule);
            }
        }
    }


    @Override
    public final void onResume() {
        super.onResume();
        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.myschedule);

        final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_mySchedule).setChecked(true);

        if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 0) {
            Toast.makeText(getView().getContext(), getString(R.string.myScheduleInfo), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final LectureItem lectureItem = (LectureItem) listView.getItemAtPosition(info.position);

        if (item.getTitle().equals(getString(R.string.deleteFromMySchedule))) {
            DataManager.getInstance().deleteFromMySchedule(info.targetView.getContext(), lectureItem);
            dataList.remove(lectureItem);
            adapter.notifyDataSetChanged();
            Toast.makeText(getView().getContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
            if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 0) {
                Toast.makeText(getView().getContext(), getString(R.string.changesScheduleText), Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }


    /**
     * @param menu
     * @param inflater
     */
    @Override
    public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.myschedule_main, menu);
    }

    /**
     * @param item
     * @return
     */
    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        // handle item selection
        if (item.getItemId() == R.id.action_delete_all) {
            DataManager.getInstance().deleteAllFromMySchedule(getActivity().getApplicationContext());
            dataList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getView().getContext(), getString(R.string.changesScheduleText), Toast.LENGTH_LONG).show();
            return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected final ArrayList<Object> background(String[] params) {
        if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) > 0) {
            final String course = params[0];
            final String semester = params[1];
            final String termTime = params[2];
            List<LectureItem> scheduleList = DataManager.getInstance().getMySchedule(getActivity().getApplicationContext(), getString(R.string.language), course, semester, termTime, Boolean.valueOf(params[3]));

            if (BuildConfig.DEBUG) assert (scheduleList != null); // ob etwas zurück kommt

            if (scheduleList != null) {
                /*
                // Die ID's für den Mein Stundenplan nochmal speichern nachdem die Doppelten raus sortiert wurden
                DataManager.getInstance().deleteAllFromMySchedule(getActivity().getApplicationContext());
                Set<String> schedulesIds = new HashSet<>();
                for (Object object : scheduleList) {
                    if (object instanceof LectureItem ) {
                        LectureItem lectureItem = (LectureItem) object;
                        schedulesIds.add(String.valueOf(lectureItem.getId()));
                    }
                }
                DataManager.getInstance().addAllToMySchedule(getActivity().getApplicationContext(), schedulesIds);

                // erneut holen mit neu sortierten ID's
                // forceRefresh auf false damit er falls sich nichts ändert sie aus dem Cache holen kann
                scheduleList = DataManager.getInstance().getMySchedule(getActivity().getApplicationContext(), getString(R.string.language), course, semester, termTime, false);
                */

                //if (scheduleList != null) {
                    return super.updateListView(scheduleList);
                //}
            }
        }
        return null;
    }
}
