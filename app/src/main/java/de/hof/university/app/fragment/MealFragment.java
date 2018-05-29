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


import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.adapter.MealAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.LastUpdated;
import de.hof.university.app.model.MediumListItem;
import de.hof.university.app.model.meal.Meal;

public class MealFragment extends AbstractListFragment {

	public final static String TAG = "MealFragment";
	private static final String ARG_SECTION_NUMBER ="section_number";

	private int weekdayListPos;
	private int selectedWeek;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		weekdayListPos = 0;
		selectedWeek = this.getArguments().getInt(ARG_SECTION_NUMBER);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected final ArrayAdapter setArrayAdapter() {
		return new MealAdapter(getActivity(), dataList);
	}

	@Override
	protected final String[] setTaskParameter(boolean forceRefresh) {
		String[] params = new String[ 1 ];
		params[ 0 ] = String.valueOf(forceRefresh);
		return params;
	}

	@Override
	public final void onResume() {
		super.onResume();
		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.speiseplan);

		Log.d("MealFragment", "onResume");

		final NavigationView navigationView = mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_speiseplan).setChecked(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		final MainActivity mainActivity = (MainActivity) getActivity();
		final NavigationView navigationView = mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_speiseplan).setChecked(false);
	}


	@Override
	protected final void modifyListViewAfterDataSetChanged() {
		listView.setSelection(weekdayListPos);
	}

	private ArrayList<Object> updateListView(List<Meal> list) {
		String day = "";
		String category = "";
		String curWeekDay = new SimpleDateFormat("EEEE", Locale.GERMANY).format(new Date());
		final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

		final boolean isDish = sharedPref.getBoolean(getString(R.string.PREF_KEY_MAIN_COURSE), true);
		final boolean isSupplement = sharedPref.getBoolean(getString(R.string.PREF_KEY_SIDE_DISHES), true);
		final boolean isPasta = sharedPref.getBoolean(getString(R.string.PREF_KEY_PASTA), true);
		final boolean isDessert = sharedPref.getBoolean(getString(R.string.PREF_KEY_DESSERTS), true);
		final boolean isSalad = sharedPref.getBoolean(getString(R.string.PREF_KEY_SALAD), true);

//        tarif = sharedPref.getString("speiseplan_tarif", "1");

		ArrayList<Object> tmpDataList = new ArrayList<>();

		for ( Meal meal : list ) {
			if ( meal.getCategory().equalsIgnoreCase("Hauptgericht") && !isDish ) {
				continue;
			}
			if ( meal.getCategory().equalsIgnoreCase("Beilage") && !isSupplement ) {
				continue;
			}
			if ( meal.getCategory().equalsIgnoreCase("Nachspeise") && !isDessert ) {
				continue;
			}
			if ( meal.getCategory().equalsIgnoreCase("Pastatheke") && !isPasta ) {
				continue;
			}
			if ( meal.getCategory().equalsIgnoreCase("Salat") && !isSalad ) {
				continue;
			}

			if ( !day.equalsIgnoreCase(meal.getWeekDay()) ) {
				day = meal.getWeekDay();
				tmpDataList.add(new BigListItem(day + " - " + sdf.format(meal.getDay())));
				if ( day.equalsIgnoreCase(curWeekDay) && selectedWeek != 2 && selectedWeek != 3) {
					weekdayListPos = tmpDataList.size() - 1;
				}
				category = "";//Bei neuen Tagen immer auch die Kategorie anzeigen
			}
			if ( !category.equalsIgnoreCase(meal.getCategory()) ) {
				category = meal.getCategory();
				tmpDataList.add(new MediumListItem(meal.getCategory()));
			}
			tmpDataList.add(meal);
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
		return DataManager.getInstance().formatDate(DataManager.getInstance().getMealsLastSaved());
	}

	@Override
	protected final ArrayList<Object> background(String[] params) {
		List<Meal> meals = DataManager.getInstance().getMeals(getActivity().getApplicationContext(), Boolean.valueOf(params[ 0 ]),selectedWeek-1);

		if (meals != null ) {
			return updateListView(meals);
		} else {
			return null;
		}
	}
}
