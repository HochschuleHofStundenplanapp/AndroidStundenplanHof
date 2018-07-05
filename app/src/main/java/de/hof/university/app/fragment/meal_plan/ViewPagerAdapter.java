/*
 * Copyright (c) 2018 Hochschule Hof
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
        String pageTitle;
        switch (position){
            case 0:
                pageTitle = context.getString(R.string.MEAL_DieseWoche);
                break;
            case 1:
                pageTitle = context.getString(R.string.MEAL_NaechsteWoche);
                break;
            case 2:
                pageTitle = context.getString(R.string.MEAL_UebernaechsteWoche);
                break;
                default: pageTitle = "";
        }


        return pageTitle;
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
