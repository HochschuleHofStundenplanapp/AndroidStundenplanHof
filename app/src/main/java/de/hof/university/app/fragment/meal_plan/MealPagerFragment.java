package de.hof.university.app.fragment.meal_plan;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.fragment.MealFragment;

/**
 * Created and © by Christian G. Pfeiffer on 21.12.17.
 */

public class MealPagerFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    int selectedTabPosition;

    private static final String ARG_SECTION_NUMBER ="section_number";


    public static MealFragment newInstance(int sectionNumber) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        //View Elemente aufbauen
        viewPager = (ViewPager) view.findViewById(R.id.my_viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.my_tab_layout);
        adapter = new ViewPagerAdapter(getChildFragmentManager(), getActivity(), viewPager, tabLayout);
        viewPager.setAdapter(adapter);
        setEvents();
        addPage("Diese Woche");
        addPage("Nächste Woche");
        addPage("Übernächste Woche");
        return view;
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
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) getActivity();
        NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_speiseplan).setChecked(false);
    }

    private void setEvents() {

        tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                viewPager.setCurrentItem(tab.getPosition());
                selectedTabPosition = viewPager.getCurrentItem();
                Log.d("Selected", "Selected " + tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                Log.d("Unselected", "Unselected " + tab.getPosition());
            }
        });
    }

    public void addPage(String pagename) {
        Bundle bundle = new Bundle();
        bundle.putString("data", pagename);
        MealWeekFragment fragmentChild = new MealWeekFragment();
        fragmentChild.setArguments(bundle);
        adapter.addFrag(fragmentChild, pagename);
        adapter.notifyDataSetChanged();
        if (adapter.getCount() > 0) tabLayout.setupWithViewPager(viewPager);
        setupTabLayout();
    }

    public void setupTabLayout() {

        selectedTabPosition = viewPager.getCurrentItem();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(adapter.getTabView(i));
        }
    }






}
