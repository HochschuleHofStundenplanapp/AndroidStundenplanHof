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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.hof.university.app.Util.Define;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.fragment.MapFragment;
import de.hof.university.app.experimental.fragment.NotenbekanntgabeFragment;
import de.hof.university.app.experimental.fragment.NotenblattFragment;
import de.hof.university.app.experimental.fragment.RaumsucheFragment;
import de.hof.university.app.fragment.ImpressumFragment;
import de.hof.university.app.fragment.MealFragment;
import de.hof.university.app.fragment.PrimussTabFragment;
import de.hof.university.app.fragment.schedule.ChangesFragment;
import de.hof.university.app.fragment.schedule.MyScheduleFragment;
import de.hof.university.app.fragment.schedule.ScheduleFragment;
import de.hof.university.app.fragment.settings.SettingsFragment;


public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	public final static String TAG = "MainActivity";

	private MealFragment mealFragment;
	private ScheduleFragment scheduleFragment;
	private ChangesFragment changesFragment;
	private MyScheduleFragment myScheduleFragment;
	private ImpressumFragment impressumFragment;

	// Experimentelle Fragmente
	private PrimussTabFragment primussTabFragment;
	private NotenblattFragment notenblattFragment;
	private NotenbekanntgabeFragment notenbekanntgabeFragment;
	private RaumsucheFragment raumsucheFragment;
	private MapFragment mapFragment;

	private NavigationView navigationView;

	DrawerLayout mDrawerLayout;
	ActionBarDrawerToggle mDrawerToggle;

	// für Navigation
	private boolean backButtonPressedOnce = false;
	private boolean firstStart = true;

	@Override
	protected final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		//Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		//setSupportActionBar(toolbar);

		DataManager.getInstance().cleanCache(getApplicationContext());

		// Notifications müssen nicht mehr angezeigt werden
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();


		// getActionBar geht nicht wahrscheinlich weil doch noch irgendwo dafür die Support Libary eingebunden wird
		// zum Nachlesen: http://codetheory.in/difference-between-setdisplayhomeasupenabled-sethomebuttonenabled-and-setdisplayshowhomeenabled/
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setHomeButtonEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(
				                                         this,                  /* host Activity */
				                                         mDrawerLayout,         /* DrawerLayout object */
				                                         //R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				                                         R.string.navigation_drawer_open,  /* "open drawer" description */
				                                         R.string.navigation_drawer_close  /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				//getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				//getActionBar().setTitle("mTitle");
				invalidateOptionsMenu();
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.addDrawerListener(mDrawerToggle);


		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		// Experimentelle Features anzeigen?
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		final boolean showExperimentalFeatures = sharedPreferences.getBoolean("experimental_features", false);
		displayExperimentalFeaturesMenuEntries(showExperimentalFeatures);

		// wurde die App gerade neu gestartet
		if(savedInstanceState == null) {
			// Habe ich schon einen eigenen Stundenplan "Mein Stundeplan" erstellt, dann damit beeginnen
			if (DataManager.getInstance().getMyScheduleSize(getApplicationContext()) > 0) {
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mySchedule));
			} else {
				// Ich habe keinen Stundenplan erstellt,
				// habe ich denn wenigstens schon Einstellungen vorgenommen?
				if (sharedPreferences.getString("term_time", "").isEmpty()) {
					// Noch nicht mal Einstellungen sind vorhanden, also gehen wir direkt in diesen Dialog
					onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_einstellungen));
				} else {
					onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_stundenplan));
				}
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle
		// If it returns true, then it has handled
		// the nav drawer indicator touch event
		if ( mDrawerToggle.onOptionsItemSelected(item) ) {
			return true;
		}

		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	public final void displayExperimentalFeaturesMenuEntries(final boolean enabled) {
		if ( enabled ) {
			navigationView.getMenu().findItem(R.id.nav_experimental).setVisible(true);
			navigationView.getMenu().findItem(R.id.nav_raumsuche).setVisible(true); //Raumsuche anzeigen
			navigationView.getMenu().findItem(R.id.nav_primuss).setVisible(true); //Primuss anzeigen
			navigationView.getMenu().findItem(R.id.nav_map).setVisible(true); //Map anzeigen

			// TODO Weil ausblenden solange die neue Authentifizierungsmethode noch nicht funktioniert
			if ( Define.SHOW_NOTEN == 0 ) {
				navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(false);
				navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(false);
			} else {
				// Nur bei höheren Versionen von Android funktioniert auch Primuss
				// HTML Connectivity mit Verschlüsselung ist dann erst vorhanden
				if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
					navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(false);
					navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(false);
				} else {
					navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(true);
					navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(true);
				}
			}
		} else {
			navigationView.getMenu().findItem(R.id.nav_experimental).setVisible(false);
		}
	}

	@Override
	// Idee: Wir wollen beim Rückwärtsgehen in den Activities nicht aus Versehen die App
	// verlassen.
	public final void onBackPressed() {
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if ( drawer.isDrawerOpen(GravityCompat.START) ) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			// Bis zum letzten Fenster ganz normal zurückgehen,
			// aber in der Haupt-Activity wollen wir ja eben noch mal nachfragen.
			if ( getFragmentManager().getBackStackEntryCount() >= 1 ) {
				getFragmentManager().popBackStack();
			} else {
				// Wurde der Zurück-Button zwei Mal gedrückt? Dann verlassen wir erst die App
				if ( !backButtonPressedOnce ) {
					backButtonPressedOnce = true;
					Toast.makeText(getApplication(), getString(R.string.doubleBackOnClose), Toast.LENGTH_SHORT).show();
					getWindow().getDecorView().getHandler().postDelayed(new Runnable() {
						@Override
						public void run() {
							backButtonPressedOnce = false;
						}
					}, 2000);
				} else {
					// Weitergeben an die Parent-Klasse, dann wird erst wirklich an die App
					// der Zurück-Button gesendet und das ist dann ggf. das Schließen der App
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
			if ( !manager.popBackStackImmediate(MealFragment.class.getName(), 0) ) {
				if ( mealFragment == null ) {
					mealFragment = new MealFragment();
				}
				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(MealFragment.class.getName());
				trans.replace(R.id.content_main, mealFragment, Define.mealsFragmentName);
				trans.commit();
			}
		} else if ( R.id.nav_notenbekanntgabe == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(NotenbekanntgabeFragment.class.getName(), 0) ) {

				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(NotenbekanntgabeFragment.class.getName());
				if ( notenbekanntgabeFragment == null ) {
					notenbekanntgabeFragment = new NotenbekanntgabeFragment();
				}
				trans.replace(R.id.content_main, notenbekanntgabeFragment);
				trans.commit();
			}
		} else if ( R.id.nav_notenblatt == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(NotenblattFragment.class.getName(), 0) ) {

				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(NotenblattFragment.class.getName());
				if ( notenblattFragment == null ) {
					notenblattFragment = new NotenblattFragment();
				}
				trans.replace(R.id.content_main, notenblattFragment);
				trans.commit();
			}
		} else if ( R.id.nav_raumsuche == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(RaumsucheFragment.class.getName(), 0) ) {
				if ( raumsucheFragment == null ) {
					raumsucheFragment = new RaumsucheFragment();
				}
				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(RaumsucheFragment.class.getName());
				trans.replace(R.id.content_main, raumsucheFragment);
				trans.commit();
			}
		} else if(R.id.nav_map == id){
			FragmentManager manager = getFragmentManager();
			if( !manager.popBackStackImmediate(MapFragment.class.getName(), 0)){
				if(mapFragment == null){
					mapFragment = new MapFragment();
				}
				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(MapFragment.class.getName());
				trans.replace(R.id.content_main, mapFragment);
				trans.commit();
			}
		} else if ( R.id.nav_stundenplan == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(ScheduleFragment.class.getName(), 0) ) {
				if ( scheduleFragment == null ) {
					scheduleFragment = new ScheduleFragment();
				}
				// starting ist ein leerer Bildschirm
				// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
				FragmentTransaction trans = manager.beginTransaction();
				if ( firstStart ) {
					firstStart = false;
				} else {
					trans.addToBackStack(ScheduleFragment.class.getName());
				}
				trans.replace(R.id.content_main, scheduleFragment, Define.scheduleFragmentName);
				trans.commit();
			}
		} else if ( R.id.nav_mySchedule == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(MyScheduleFragment.class.getName(), 0) ) {
				if ( myScheduleFragment == null ) {
					myScheduleFragment = new MyScheduleFragment();
				}
				// starting ist ein leerer Bildschirm
				// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
				FragmentTransaction trans = manager.beginTransaction();
				if ( firstStart ) {
					firstStart = false;
				} else {
					trans.addToBackStack(MyScheduleFragment.class.getName());
				}
				trans.replace(R.id.content_main, myScheduleFragment, Define.myScheduleFragmentName);
				trans.commit();
			}

		} else if ( R.id.nav_aenderung == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(ChangesFragment.class.getName(), 0) ) {

				if ( changesFragment == null ) {
					changesFragment = new ChangesFragment();
				}
				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(ChangesFragment.class.getName());
				trans.replace(R.id.content_main, changesFragment, Define.changesFragmentName);
				trans.commit();
			}

		} else if ( R.id.nav_einstellungen == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(SettingsFragment.class.getName(), 0) ) {

				// starting ist ein leerer Bildschirm
				// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
				FragmentTransaction trans = manager.beginTransaction();
				if ( firstStart ) {
					firstStart = false;
				} else {
					trans.addToBackStack(SettingsFragment.class.getName());
				}
				trans.replace(R.id.content_main, new SettingsFragment());
				trans.commit();
			}
		} else if ( R.id.nav_impressum == id ) {
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(ImpressumFragment.class.getName(), 0) ) {

				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(ImpressumFragment.class.getName());
				if ( impressumFragment == null ) {
					impressumFragment = new ImpressumFragment();
				}
				trans.replace(R.id.content_main, impressumFragment);
				trans.commit();
			}
		} else if ( R.id.nav_aboutus == id ) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.aboutusURL)));
			startActivity(browserIntent);
		} else if ( R.id.nav_datenschutz == id ) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.datenschutzURL)));
			startActivity(browserIntent);
		} else if (R.id.nav_primuss == id){
			FragmentManager manager = getFragmentManager();
			if ( !manager.popBackStackImmediate(PrimussTabFragment.class.getName(), 0) ) {

				FragmentTransaction trans = manager.beginTransaction();
				trans.addToBackStack(PrimussTabFragment.class.getName());
				if ( primussTabFragment == null ) {
					primussTabFragment = new PrimussTabFragment();
				}
				trans.replace(R.id.content_main, primussTabFragment);
				trans.commit();
			}

		}

		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
