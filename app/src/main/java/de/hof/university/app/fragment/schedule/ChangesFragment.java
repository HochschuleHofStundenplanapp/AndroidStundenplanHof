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
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.adapter.ChangesAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.AbstractListFragment;
import de.hof.university.app.model.LastUpdated;


/**
 * Created by larsg_000 on 30.11.2015.
 */
public class ChangesFragment extends AbstractListFragment {

	private final static String TAG = "ChangesFragment";

	@Override
	protected final ArrayAdapter setArrayAdapter() {
		return new ChangesAdapter(getActivity(), dataList);
	}

	@Override
	public final void onResume() {
		super.onResume();
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.aenderung);

		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_aenderung).setChecked(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		MainActivity mainActivity = (MainActivity) getActivity();
		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_aenderung).setChecked(false);
	}

	@Override
	protected final String[] setTaskParameter(boolean forceRefresh) {
		final String[] params = new String[ 4 ];
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final String course = sharedPref.getString(getString(R.string.PREFERENCE_KEY_STUDIENGANG), "");
		final String semester = sharedPref.getString(getString(R.string.PREFERENCE_KEY_SEMESTER), "");
		final String termTime = sharedPref.getString(getString(R.string.PREFERENCE_KEY_TERM_TIME), "");

		// Meldungen nur bringen wenn kein "Mein Stundenplan" angelegt ist
		if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 0) {
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

		params[ 0 ] = course;
		params[ 1 ] = semester;
		params[ 2 ] = termTime;
		params[ 3 ] = String.valueOf(forceRefresh);
		return params;
	}

	private ArrayList<Object> updateListView(ArrayList<Object> list) {
		ArrayList<Object> tmpDataList = new ArrayList<>();

		for (Object obt: list) {
			tmpDataList.add(obt);
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
	private String getLastSaved() {
		return DataManager.getInstance().formatDate(DataManager.getInstance().getChangesLastSaved());
	}

	@Override
	protected final ArrayList<Object> background(String[] params) {
		final String course = params[ 0 ];
		final String semester = params[ 1 ];
		final String termTime = params[ 2 ];
		ArrayList<Object> changesList = DataManager.getInstance().getChanges(getActivity().getApplicationContext(), course, semester, termTime, Boolean.valueOf(params[ 3 ]));

		if (changesList != null ) {
			// wenn etwas zurück kommt, dann aktualisiere die View
			return this.updateListView(changesList);
		} else {
			// wenn nichts zurück kommt returne null, damit ein Toast angezeigt wird
			return null;
		}
	}
}
