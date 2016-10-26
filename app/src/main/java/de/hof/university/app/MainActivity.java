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

package de.hof.university.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;


import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.fragment.NotenbekanntgabeFragment;
import de.hof.university.app.experimental.fragment.NotenblattFragment;
import de.hof.university.app.experimental.fragment.RaumsucheFragment;
import de.hof.university.app.fragment.ImpressumFragment;
import de.hof.university.app.fragment.MenuFragment;
import de.hof.university.app.fragment.schedule.MyScheduleFragment;
import de.hof.university.app.fragment.settings.SettingsFragment;
import de.hof.university.app.fragment.schedule.ChangesFragment;
import de.hof.university.app.fragment.schedule.ScheduleFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MenuFragment menuFragment;
    private ScheduleFragment scheduleFragment;
    private ChangesFragment changesFragment;
    private MyScheduleFragment myScheduleFragment;
    private ImpressumFragment impressumFragment;

    //Experimentelle Fragmente
    private NotenblattFragment notenblattFragment;
    private NotenbekanntgabeFragment notenbekanntgabeFragment;
    private RaumsucheFragment raumsucheFragment;


    private NavigationView navigationView;

    // für Navigation
    private boolean backButtonPressedOnce = false;
    private boolean firstStart = true;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Notifications müssen nicht mehr angezeigt werden
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        //Experimentelle Features anzeigen?
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final boolean showExperimentalFeatures = sharedPreferences.getBoolean("experimental_features", false);
        setExperimentalFeatures(showExperimentalFeatures);

        DataManager.getInstance().cleanCache(getApplicationContext());

        if(savedInstanceState == null) {

            if (DataManager.getInstance().getMyScheduleSize(getApplicationContext()) > 0) {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mySchedule));
            } else {
                if (sharedPreferences.getString("term_time", "").isEmpty()) {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_einstellungen));
                } else {
                    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_stundenplan));
                }
            }
        }

    }

    public final void setExperimentalFeatures(final boolean enabled) {
        if(enabled){
            navigationView.getMenu().findItem(R.id.nav_experimental).setVisible(true);
            navigationView.getMenu().findItem(R.id.nav_raumsuche).setVisible(true); //Raumsuche anzeigen

            //Nur bei höheren Versionen funktioniert auch Primuss
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(false);
                navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(false);
            }else{
                navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(true);
            }
        }else{
            navigationView.getMenu().findItem(R.id.nav_experimental).setVisible(false);
        }
    }

    @Override
    public final void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getFragmentManager().getBackStackEntryCount() >= 1) {
                getFragmentManager().popBackStack();
            } else {
                if (!backButtonPressedOnce) {
                    backButtonPressedOnce = true;
                    Toast.makeText(getApplication(), R.string.doubleBackOnClose, Toast.LENGTH_SHORT).show();
                    getWindow().getDecorView().getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backButtonPressedOnce = false;
                        }
                    }, 2000);
                } else {
                    super.onBackPressed();
                }
            }
        }
    }


    @Override
    public final boolean onNavigationItemSelected(final MenuItem item) {
        final int id = item.getItemId();


        if ( R.id.nav_speiseplan == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(MenuFragment.class.getName(), 0)) {
                if (menuFragment == null) {
                    menuFragment = new MenuFragment();
                }
                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(MenuFragment.class.getName());
                trans.replace(R.id.content_main, menuFragment);
                trans.commit();
            }
        } else if ( R.id.nav_notenbekanntgabe == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(NotenbekanntgabeFragment.class.getName(), 0)) {

                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(NotenbekanntgabeFragment.class.getName());
                if (notenbekanntgabeFragment == null) {
                    notenbekanntgabeFragment = new NotenbekanntgabeFragment();
                }
                trans.replace(R.id.content_main, notenbekanntgabeFragment);
                trans.commit();
            }
        } else if ( R.id.nav_notenblatt == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(NotenblattFragment.class.getName(), 0)) {

                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(NotenblattFragment.class.getName());
                if (notenblattFragment == null) {
                    notenblattFragment = new NotenblattFragment();
                }
                trans.replace(R.id.content_main, notenblattFragment);
                trans.commit();
            }
        } else if ( R.id.nav_raumsuche == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(RaumsucheFragment.class.getName(), 0)) {
                if (raumsucheFragment == null) {
                    raumsucheFragment = new RaumsucheFragment();
                }
                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(RaumsucheFragment.class.getName());
                trans.replace(R.id.content_main, raumsucheFragment);
                trans.commit();
            }
        } else if ( R.id.nav_stundenplan == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(ScheduleFragment.class.getName(), 0)) {
                if (scheduleFragment == null) {
                    scheduleFragment = new ScheduleFragment();
                }
                FragmentTransaction trans = manager.beginTransaction();
                if (firstStart) {
                    firstStart = false;
                } else {
                    trans.addToBackStack(ScheduleFragment.class.getName());
                }
                trans.replace(R.id.content_main, scheduleFragment);
                trans.commit();
            }
        }else if( R.id.nav_mySchedule == id ){
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(MyScheduleFragment.class.getName(), 0)) {
                if (myScheduleFragment == null) {
                    myScheduleFragment = new MyScheduleFragment();
                }
                FragmentTransaction trans = manager.beginTransaction();
                if (firstStart) {
                    firstStart = false;
                } else {
                    trans.addToBackStack(MyScheduleFragment.class.getName());
                }
                trans.replace(R.id.content_main, myScheduleFragment);
                trans.commit();
            }

        } else if ( R.id.nav_aenderung == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(ChangesFragment.class.getName(), 0)) {

                if (changesFragment == null) {
                    changesFragment = new ChangesFragment();
                }
                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(ChangesFragment.class.getName());
                trans.replace(R.id.content_main, changesFragment);
                trans.commit();
            }

        } else if ( R.id.nav_einstellungen == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(SettingsFragment.class.getName(), 0)) {

                FragmentTransaction trans = manager.beginTransaction();
                if (firstStart) {
                    firstStart = false;
                } else {
                    trans.addToBackStack(SettingsFragment.class.getName());
                }
                trans.replace(R.id.content_main, new SettingsFragment());
                trans.commit();
            }
        } else if ( R.id.nav_impressum == id ) {
            FragmentManager manager = getFragmentManager();
            if (!manager.popBackStackImmediate(ImpressumFragment.class.getName(), 0)) {

                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(ImpressumFragment.class.getName());
                if (impressumFragment == null) {
                    impressumFragment = new ImpressumFragment();
                }
                trans.replace(R.id.content_main, impressumFragment);
                trans.commit();
            }
        }else if ( R.id.nav_aboutus == id ) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aboutusURL)));
            startActivity(browserIntent);
        }else if( R.id.nav_datenschutz == id ){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.datenschutzURL)));
            startActivity(browserIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
