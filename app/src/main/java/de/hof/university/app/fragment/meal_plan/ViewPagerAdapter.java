package de.hof.university.app.fragment.meal_plan;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;

/**
 * Created and © by Christian G. Pfeiffer on 10.01.18.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private final ArrayList<String> mFragmentTitleList = new ArrayList<>();
    Context context;

    public ViewPagerAdapter(FragmentManager manager, Context context, ViewPager viewPager,
                            TabLayout tabLayout) {
        super(manager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return MealPagerFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ("Woche " + (position+1));
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public View getTabView(final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_menutab_item, null);
        TextView tabItemName = (TextView) view.findViewById(R.id.textViewTabItemName);

        tabItemName.setText(mFragmentTitleList.get(position));
        tabItemName.setTextColor(context.getResources().getColor(android.R.color.background_light));

        return view;
    }
}
