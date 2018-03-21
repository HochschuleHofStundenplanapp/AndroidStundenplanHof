package de.hof.university.app.fragment.meal_plan;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hof.university.app.R;
import de.hof.university.app.adapter.MealAdapter;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.AbstractListFragment;
import de.hof.university.app.fragment.MealFragment;
import de.hof.university.app.fragment.schedule.ChangesFragment;
import de.hof.university.app.fragment.schedule.MyScheduleFragment;
import de.hof.university.app.fragment.schedule.ScheduleFragment;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.LastUpdated;
import de.hof.university.app.model.MediumListItem;
import de.hof.university.app.model.meal.Meal;
import de.hof.university.app.util.Define;

/**
 * Created and © by Christian G. Pfeiffer on 21.12.17.
 */

public class MealWeekFragment extends Fragment {
    public final static String TAG = "MealFragment";
    int selectedWeek = -1;
    int weekdayListPos = 0;

    private SwipeRefreshLayout swipeContainer;
    protected ListView listView;
    protected ArrayAdapter adapter;
    protected ArrayList<Object> dataList;
    private MealWeekFragment.Task task;
    SharedPreferences sharedPref;

    private static final String ARG_SECTION_NUMBER ="section_number";

    public static MealPagerFragment newInstance(int sectionNumber) {
        MealPagerFragment fragment = new MealPagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);


        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list,container,false);
        Bundle bundle = getArguments();
        weekdayListPos = 0;
        selectedWeek = bundle.getInt("data");

        swipeContainer = v.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData(true);
            }
        });

        adapter = setArrayAdapter();
        listView = v.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        return v;
    }

    private void updateData(boolean forceRefresh) {
        String[] params = setTaskParameter(forceRefresh);
        if (params != null) {
            task = new MealWeekFragment.Task();
            task.execute(params);
        }
    }


    protected final ArrayAdapter setArrayAdapter() {
        return new MealAdapter(getActivity(), dataList);
    }


    protected final String[] setTaskParameter(boolean forceRefresh) {
        String[] params = new String[ 1 ];
        params[ 0 ] = String.valueOf(forceRefresh);
        return params;
    }


    protected final void modifyListViewAfterDataSetChanged() {
        listView.setSelection(weekdayListPos);
    }

    private ArrayList<Object> updateListView(List<Meal> list) {
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


    protected final ArrayList<Object> background(String[] params) {
        List<Meal> meals = DataManager.getInstance().getMeals(getActivity().getApplicationContext(), Boolean.valueOf(params[ 0 ]),selectedWeek-1);

        if (meals != null ) {
            return updateListView(meals);
        } else {
            return null;
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

                    // Damit man unter Speiseplan ein Feedback bekommt wenn es keinen Speiseplan gibt.
                    final MealFragment mealFragment = (MealFragment) getFragmentManager().findFragmentByTag(Define.mealsFragmentName);
                    if ((mealFragment != null) && mealFragment.isVisible() && (dataList.isEmpty())) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noMeal), Toast.LENGTH_SHORT).show();
                    }

                }
            super.onPostExecute(result);
        }
        }

        @Override
        protected final void onCancelled(ArrayList<Object> result) {
            Log.d(TAG, "onCancelled");
        }
    }
}