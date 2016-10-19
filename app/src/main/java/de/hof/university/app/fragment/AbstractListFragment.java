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

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.hof.university.app.R;

/**
 * Created by larsg on 20.06.2016.
 */
public abstract class AbstractListFragment extends Fragment {

    private SwipeRefreshLayout swipeContainer;
    protected ListView listView;
    protected ArrayAdapter adapter;
    protected ArrayList<Object> dataList;
    protected AbstractListFragment.Task task;
    protected SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        dataList = new ArrayList<>();
    }

    protected abstract ArrayAdapter setArrayAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
    }


    @Override
    public final void onDestroyView() {
        if ( null != task ) {
            task.cancel(true);
        }
        swipeContainer.setRefreshing(false);
        super.onDestroyView();
    }

    protected abstract String[] setTaskParameter(boolean forceRefresh);

    private void updateData(boolean forceRefresh) {
        String[] params = setTaskParameter(forceRefresh);
        if(params != null) {
            task = new AbstractListFragment.Task();
            task.execute(params);
        }
    }

    private class Task extends AsyncTask<String, Void, Void> {
        @Override
        protected final void onPreExecute() {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(true);
                }
            });
            dataList.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected final Void doInBackground(String... params) {
            return background(params);
        }

        @Override
        protected final void onPostExecute(Void aVoid) {
            swipeContainer.post(new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(false);
                }
            });
            adapter.notifyDataSetChanged();
            modifyListViewAfterDataSetChanged();
            super.onPostExecute(aVoid);
        }
    }

    protected void modifyListViewAfterDataSetChanged(){}

    protected abstract Void background(String[] params);

}
