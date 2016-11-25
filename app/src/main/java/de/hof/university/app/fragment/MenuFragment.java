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
import android.widget.ArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.adapter.MenuAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.MediumListItem;
import de.hof.university.app.model.menu.Meal;

public class MenuFragment extends AbstractListFragment {
    // --Commented out by Inspection (17.07.2016 20:12):private String tarif;
    private int weekdayListPos;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        weekdayListPos = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected final ArrayAdapter setArrayAdapter() {
        return new MenuAdapter(getActivity(), dataList);
    }

    @Override
    protected final String[] setTaskParameter(boolean forceRefresh) {
        String[] params = new String[1];
        params[0] = String.valueOf(forceRefresh);
        return params;
    }

    @Override
    public final void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.speiseplan);

        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_speiseplan).setChecked(true);
    }


    @Override
    protected final void modifyListViewAfterDataSetChanged() {
        listView.setSelection(weekdayListPos);
    }

    private ArrayList<Object> updateListView(List<Object> list) {
        String day = "";
        String category = "";
        String curWeekDay = new SimpleDateFormat("EEEE", Locale.GERMANY).format(new Date());
        final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        final boolean isDish = sharedPref.getBoolean("speiseplan_hauptgericht", true);
        final boolean isSupplement = sharedPref.getBoolean("speiseplan_beilage", true);
        final boolean isPasta = sharedPref.getBoolean("speiseplan_pasta", true);
        final boolean isDessert = sharedPref.getBoolean("speiseplan_dessert", true);
        final boolean isSalad = sharedPref.getBoolean("speiseplan_salat", true);

//        tarif = sharedPref.getString("speiseplan_tarif", "1");

        ArrayList<Object> tmpDataList = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Meal) {
                Meal meal = (Meal) object;

                if (meal.getCategory().equalsIgnoreCase("Hauptgericht") && !isDish) {
                    continue;
                }
                if (meal.getCategory().equalsIgnoreCase("Beilage") && !isSupplement) {
                    continue;
                }
                if (meal.getCategory().equalsIgnoreCase("Nachspeise") && !isDessert) {
                    continue;
                }
                if (meal.getCategory().equalsIgnoreCase("Pastatheke") && !isPasta) {
                    continue;
                }
                if (meal.getCategory().equalsIgnoreCase("Salat") && !isSalad) {
                    continue;
                }

                if (!day.equalsIgnoreCase(meal.getWeekDay())) {
                    day = meal.getWeekDay();
                    tmpDataList.add(new BigListItem(day + " - " + sdf.format(meal.getDay())));
                    if (day.equalsIgnoreCase(curWeekDay)) {
                        weekdayListPos = tmpDataList.size() - 1;
                    }
                    category = "";//Bei neuen Tagen immer auch die Kategorie anzeigen
                }
                if (!category.equalsIgnoreCase(meal.getCategory())) {
                    category = meal.getCategory();
                    tmpDataList.add(new MediumListItem(meal.getCategory()));
                }
                tmpDataList.add(meal);
            }
        }
        return tmpDataList;
    }

    @Override
    protected final ArrayList<Object> background(String[] params) {
        List<Object> meals = DataManager.getInstance().getMeals(getActivity().getApplicationContext(), Boolean.valueOf(params[0]));

        if (meals != null) {
            return updateListView(meals);
        } else {
            return null;
        }
    }
}
