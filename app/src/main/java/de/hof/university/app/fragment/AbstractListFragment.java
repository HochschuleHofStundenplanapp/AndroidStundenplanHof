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

package de.hof.university.app.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.schedule.ChangesFragment;
import de.hof.university.app.fragment.schedule.MyScheduleFragment;
import de.hof.university.app.fragment.schedule.ScheduleFragment;
import de.hof.university.app.util.Define;


/**
 * Created by larsg on 20.06.2016.
 */
public abstract class AbstractListFragment extends android.support.v4.app.Fragment {

    private final static String TAG = "AbstractListFragment";

    private SwipeRefreshLayout swipeContainer;
    protected ListView listView;
    protected ArrayAdapter adapter;
    protected ArrayList<Object> dataList;
    private AbstractListFragment.Task task;
    SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dataList = new ArrayList<>();
    }

    protected abstract ArrayAdapter setArrayAdapter();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData(true);
            }
        });

        adapter = setArrayAdapter();
        listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData(false);
        // falls es eine andere Toolbar Farbe soll
        //MainActivity mainActivity = (MainActivity) getActivity();
        //mainActivity.findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorBlue));
    }


    @Override
    public final void onDestroyView() {
        if (task != null) {
            task.cancel(true);
        }
        dataList.clear();
        adapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
        swipeContainer.destroyDrawingCache();
        swipeContainer.clearAnimation();
        super.onDestroyView();
    }

    protected abstract String[] setTaskParameter(boolean forceRefresh);

    private void updateData(boolean forceRefresh) {
        String[] params = setTaskParameter(forceRefresh);
        if (params != null) {
            task = new AbstractListFragment.Task();
            task.execute(params);
        }
    }

    private class Task extends AsyncTask<String, Void, ArrayList<Object>> {

        @Override
        protected final void onPreExecute() {
            if (!isCancelled()) {
                swipeContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(true);
                    }
                });
            }
        }

        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            if (!isCancelled()) {
                return background(params);
            }
            return null;
        }

        @Override
        protected final void onPostExecute(ArrayList<Object> result) {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(false);
                }
            });

            if (!this.isCancelled()) {
                if (result != null) {
                    dataList.clear();
                    dataList.addAll(result);
                    adapter.notifyDataSetChanged();
                    modifyListViewAfterDataSetChanged();

                    // Damit man unter Änderungen ein Feedback bekommt wenn es keine Änderungen gibt.
                    final ChangesFragment changesFragment = (ChangesFragment) getFragmentManager().findFragmentByTag(Define.changesFragmentName);
                    if ((changesFragment != null) && changesFragment.isVisible() && (dataList.isEmpty())) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noChanges), Toast.LENGTH_SHORT).show();
                    }

                    // Damit man unter Speiseplan ein Feedback bekommt wenn es keinen Speiseplan gibt.
                    final MealFragment mealFragment = (MealFragment) getFragmentManager().findFragmentByTag(Define.mealsFragmentName);
                    if ((mealFragment != null) && mealFragment.isVisible() && (dataList.isEmpty())) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noMeal), Toast.LENGTH_SHORT).show();
                    }

                    // Damit man unter Stundenplan ein Feedback bekommt wenn kein Stundenplan vorhanden ist.
                    final ScheduleFragment scheduleFragment = (ScheduleFragment) getFragmentManager().findFragmentByTag(Define.scheduleFragmentName);
                    if ((scheduleFragment != null) && scheduleFragment.isVisible() && (dataList.isEmpty())) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noScheduleText), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    final ChangesFragment changesFragment = (ChangesFragment) getFragmentManager().findFragmentByTag(Define.changesFragmentName);
                    final MyScheduleFragment myScheduleFragment = (MyScheduleFragment) getFragmentManager().findFragmentByTag(Define.myScheduleFragmentName);

                    // Damit man ein Feedback bekommt wenn das aktualisieren fehlgeschlagen ist
                    if (((changesFragment != null) && changesFragment.isVisible())
                            || ((myScheduleFragment != null) && myScheduleFragment.isVisible())) {
                        if ((changesFragment != null) && changesFragment.isVisible()) {
                            if (DataManager.getInstance().getScheduleSize(getActivity().getApplicationContext()) > 0) {
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.refreshFailed), Toast.LENGTH_SHORT).show();
                            }
                        } else if ((myScheduleFragment != null) && myScheduleFragment.isVisible()) {
                            if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) > 0) {
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.refreshFailed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.refreshFailed), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            super.onPostExecute(result);
        }

        @Override
        protected final void onCancelled(ArrayList<Object> result) {
            Log.d(TAG, "onCancelled");
        }
    }

    protected void modifyListViewAfterDataSetChanged() {
    }
    
    /**
     *
     * @param params
     * @return
     */
    protected abstract ArrayList<Object> background(String[] params);

}
