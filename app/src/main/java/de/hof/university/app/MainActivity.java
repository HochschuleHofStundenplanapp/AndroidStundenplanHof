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

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.CookieHandler;
import java.net.CookieManager;

import de.hof.university.app.Util.Define;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.fragment.NotenbekanntgabeFragment;
import de.hof.university.app.experimental.fragment.NotenblattFragment;
import de.hof.university.app.experimental.fragment.RaumsucheFragment;
import de.hof.university.app.fragment.AboutusFragment;
import de.hof.university.app.fragment.MapFragment;
import de.hof.university.app.fragment.MealFragment;
import de.hof.university.app.fragment.NavigationFragment;
import de.hof.university.app.fragment.PrimussTabFragment;
import de.hof.university.app.fragment.SettingsFragment;
import de.hof.university.app.fragment.schedule.ChangesFragment;
import de.hof.university.app.fragment.schedule.MyScheduleFragment;
import de.hof.university.app.fragment.schedule.ScheduleFragment;


public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private static Context appContext;
	private final String TAG = "MainActivity";
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private MealFragment mealFragment;
	private ScheduleFragment scheduleFragment;
	private ChangesFragment changesFragment;
	private MyScheduleFragment myScheduleFragment;
	private AboutusFragment aboutusFragment;
	// Experimentelle Fragmente
	private PrimussTabFragment primussTabFragment;
	private NotenblattFragment notenblattFragment;
	private NotenbekanntgabeFragment notenbekanntgabeFragment;
	private RaumsucheFragment raumsucheFragment;
	private MapFragment mapFragment;
	private NavigationFragment navigationFragment;
	private NavigationView navigationView;
	// für Navigation
	private boolean backButtonPressedOnce = false;
	private boolean firstStart = true;

	public static Context getAppContext() {
		return appContext;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		appContext = this;

		// Let the cookieManager handle the Cookies
		final CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

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
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		final boolean showExperimentalFeatures = sharedPreferences.getBoolean(getString(R.string.PREFERENCE_KEY_EXPERIMENTAL_FEATURES_ENABLED), false);
		displayExperimentalFeaturesMenuEntries(showExperimentalFeatures);


		// wurde die App gerade neu gestartet?
		if (savedInstanceState == null) {
			// ja die App wurde neu gestartet

			// Ist ein "Mein Stundenplan" vorhanden?
			if (DataManager.getInstance().getMyScheduleSize(getApplicationContext()) > 0) {
				// Es gibt einen "Mein Studnenplan". Also gehen wir zu ihm.
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_mySchedule));
			}
			// Sind die Einstellungen vorhanden?
			else if (!sharedPreferences.getString(getString(R.string.PREFERENCE_KEY_TERM_TIME), "").isEmpty()
					&& !sharedPreferences.getString(getString(R.string.PREFERENCE_KEY_STUDIENGANG), "").isEmpty()
					&& !sharedPreferences.getString(getString(R.string.PREFERENCE_KEY_SEMESTER), "").isEmpty()) {
				// ja, also gehen wir zum Stundenplan
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_stundenplan));
			} else {
				// In allen anderen Fällen gehen wir zu den Einstellungen
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_einstellungen));

				// returnen damit keine Intents gehandelt werden und keine Dialoge kommen.
				return;
			}
		}

		// wurde die Activity durch ein Intent gestartet, vermutlich durch klicken auf eine Benachrichtigung?
		handleIntent();

		// beim ersten Start einen Hinweis auf die experimentellen Funktionen geben
		showExperimentalFeaturesInfoDialog(sharedPreferences);

		// Fragen ob die Push-Benachrichtungen aktiviert werden sollen
		showPushNotificationDialog(sharedPreferences);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent();
	}

	private void handleIntent() {
		final String notification_type = getIntent().getStringExtra("notification_type");

		if (notification_type != null) {
			// Falls auf eine Änderungs-Benachrichtigung gedrückt wurde
			if ("change".equals(notification_type)) {
				// Direkt zu den Änderungen springen
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_aenderung));
			}
			// Hier noch weitere Überprüfungen für andere Intents falls nötig
		} else {
			final String action = getIntent().getAction();

			if (Define.SHORTCUT_INTENT_CHANGES.equals(action)) {
				firstStart = true;
				onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_aenderung));
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
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle your other action bar items...

		return super.onOptionsItemSelected(item);
	}

	public final void displayExperimentalFeaturesMenuEntries(final boolean enabled) {
		if (enabled) {
			navigationView.getMenu().findItem(R.id.nav_experimental).setVisible(true);
			navigationView.getMenu().findItem(R.id.nav_raumsuche).setVisible(true);    // Raumsuche anzeigen
//			navigationView.getMenu().findItem(R.id.nav_primuss).setVisible(true); 		// Primuss anzeigen
			navigationView.getMenu().findItem(R.id.nav_map).setVisible(true);            // Map anzeigen
			navigationView.getMenu().findItem(R.id.nav_navigation).setVisible(true);    // Navigation anzeigen

			// TODO Weil ausblenden solange die neue Authentifizierungsmethode noch nicht funktioniert
			if (Define.SHOW_NOTEN == false) {
				navigationView.getMenu().findItem(R.id.nav_notenbekanntgabe).setVisible(false);
				navigationView.getMenu().findItem(R.id.nav_notenblatt).setVisible(false);
			} else {
				// Nur bei höheren Versionen von Android funktioniert auch Primuss
				// HTML Connectivity mit Verschlüsselung ist dann erst vorhanden
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
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

	public void showPushNotificationDialog(final SharedPreferences sharedPreferences) { // Sichtbarkeit auf public geändert
		final boolean showPushNotificationsDialog = sharedPreferences.getBoolean("show_push_notifications_dialog", true);
		final boolean getPushNotifications = sharedPreferences.getBoolean("changes_notifications", false);

		if (showPushNotificationsDialog) {
			sharedPreferences.edit()
					.putBoolean("show_push_notifications_dialog", false)
					.apply();

			if (!getPushNotifications) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.notifications)
						.setMessage(R.string.notifications_question)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sharedPreferences.edit()
										.putBoolean("changes_notifications", true)
										.apply();
								DataManager.getInstance().registerFCMServerForce(getApplicationContext());
							}
						})
						.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//nothing to do here. Just close the dialog
							}
						})
						.setIcon(android.R.drawable.ic_dialog_info)
						.show();
			}
		}
	}

	private void showExperimentalFeaturesInfoDialog(SharedPreferences sharedPreferences) {
		final boolean showExperimentalFeaturesInfo = sharedPreferences.getBoolean("show_experimental_features_info", true);
		final boolean showExperimentalFeatures = sharedPreferences.getBoolean("experimental_features", false);

		if (showExperimentalFeaturesInfo) {
			sharedPreferences.edit()
					.putBoolean("show_experimental_features_info", false)
					.apply();

			// Anzeigen falls nicht schon aktiviert
			if (!showExperimentalFeatures) {
				new AlertDialog.Builder(this)
						.setTitle(R.string.experimental_features)
						.setMessage(R.string.experimental_features_infotext)
						.setPositiveButton(R.string.einstellungen, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_einstellungen));
							}
						})
						.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//nothing to do here. Just close the dialog
							}
						})
						.setIcon(android.R.drawable.ic_dialog_info)
						.show();
			}
		}
	}

	@Override
	// Idee: Wir wollen beim Rückwärtsgehen in den Activities nicht aus Versehen die App
	// verlassen.
	public final void onBackPressed() {
		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			// Bis zum letzten Fenster ganz normal zurückgehen,
			// aber in der Haupt-Activity wollen wir ja eben noch mal nachfragen.
			if (getFragmentManager().getBackStackEntryCount() >= 1) {
				getFragmentManager().popBackStack();
			} else {
				// Wurde der Zurück-Button zwei Mal gedrückt? Dann verlassen wir erst die App
				if (!backButtonPressedOnce) {
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
	public final boolean onNavigationItemSelected(@NonNull final MenuItem item) {
		final int id = item.getItemId();

		FragmentManager manager = getFragmentManager();


		switch (id) {
			case R.id.nav_speiseplan:
				if (!manager.popBackStackImmediate(MealFragment.class.getName(), 0)) {
					if (mealFragment == null) {
						mealFragment = new MealFragment();
					}
					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(MealFragment.class.getName());
					trans.replace(R.id.content_main, mealFragment, Define.mealsFragmentName);
					trans.commit();
				}
				break;

			case R.id.nav_notenbekanntgabe:
				if (!manager.popBackStackImmediate(NotenbekanntgabeFragment.class.getName(), 0)) {

					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(NotenbekanntgabeFragment.class.getName());
					if (notenbekanntgabeFragment == null) {
						notenbekanntgabeFragment = new NotenbekanntgabeFragment();
					}
					trans.replace(R.id.content_main, notenbekanntgabeFragment);
					trans.commit();

				}
				break;

			case R.id.nav_notenblatt:
				if (!manager.popBackStackImmediate(NotenblattFragment.class.getName(), 0)) {

					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(NotenblattFragment.class.getName());
					if (notenblattFragment == null) {
						notenblattFragment = new NotenblattFragment();
					}
					trans.replace(R.id.content_main, notenblattFragment);
					trans.commit();
				}
				break;

			case R.id.nav_raumsuche:
				if (!manager.popBackStackImmediate(RaumsucheFragment.class.getName(), 0)) {
					if (raumsucheFragment == null) {
						raumsucheFragment = new RaumsucheFragment();
					}
					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(RaumsucheFragment.class.getName());
					trans.replace(R.id.content_main, raumsucheFragment);
					trans.commit();
				}
				break;

			case R.id.nav_map:
				if (!manager.popBackStackImmediate(MapFragment.class.getName(), 0)) {
					if (mapFragment == null) {
						mapFragment = new MapFragment();
					}
					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(MapFragment.class.getName());
					trans.replace(R.id.content_main, mapFragment);
					trans.commit();
				}
				break;

			case R.id.nav_navigation:
				if (!manager.popBackStackImmediate(NavigationFragment.class.getName(), 0)) {
					if (navigationFragment == null) {
						navigationFragment = new NavigationFragment();
					}
					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(NavigationFragment.class.getName());
					trans.replace(R.id.content_main, navigationFragment);
					trans.commit();
				}
				break;

			case R.id.nav_stundenplan:
				if (!manager.popBackStackImmediate(ScheduleFragment.class.getName(), 0)) {
					if (scheduleFragment == null) {
						scheduleFragment = new ScheduleFragment();
					}
					// starting ist ein leerer Bildschirm
					// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
					FragmentTransaction trans = manager.beginTransaction();
					if (firstStart) {
						firstStart = false;
					} else {
						trans.addToBackStack(ScheduleFragment.class.getName());
					}
					trans.replace(R.id.content_main, scheduleFragment, Define.scheduleFragmentName);
					trans.commit();
				}
				break;

			case R.id.nav_mySchedule:
				if (!manager.popBackStackImmediate(MyScheduleFragment.class.getName(), 0)) {
					if (myScheduleFragment == null) {
						myScheduleFragment = new MyScheduleFragment();
					}
					// starting ist ein leerer Bildschirm
					// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
					FragmentTransaction trans = manager.beginTransaction();
					if (firstStart) {
						firstStart = false;
					} else {
						trans.addToBackStack(MyScheduleFragment.class.getName());
					}
					trans.replace(R.id.content_main, myScheduleFragment, Define.myScheduleFragmentName);
					trans.commit();
				}
				break;

			case R.id.nav_aenderung:
				if (!manager.popBackStackImmediate(ChangesFragment.class.getName(), 0)) {
					if (changesFragment == null) {
						changesFragment = new ChangesFragment();
					}
					FragmentTransaction trans = manager.beginTransaction();
					// starting ist ein leerer Bildschirm
					// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
					if (firstStart) {
						firstStart = false;
					} else {
						trans.addToBackStack(ChangesFragment.class.getName());
					}
					trans.replace(R.id.content_main, changesFragment, Define.changesFragmentName);
					trans.commit();
				}

				// Notifications entfernen wenn man zu den Änderungen geht
				NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancelAll();
				break;

			case R.id.nav_einstellungen:
				if (!manager.popBackStackImmediate(SettingsFragment.class.getName(), 0)) {
					// starting ist ein leerer Bildschirm
					// deswegen wollen wir beim Zurückgehen diesen Bildschirm nicht auf den BackStack... legen
					FragmentTransaction trans = manager.beginTransaction();
					// Wenn dies der erste Start der Application ist, so haben wir noch keinen
					// Rück-Bildschirm, das heißt, wir können auch nichts in den BackStack ablegen
					if (firstStart) {
						firstStart = false;
					} else {
						// Trage dieses Fragment in die Rückwärts-Historie ein.
						trans.addToBackStack(SettingsFragment.class.getName());
					}
					trans.replace(R.id.content_main, new SettingsFragment());
					trans.commit();
				}
				break;

			case R.id.nav_impressum: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Define.IMPRESSUMURL));
				startActivity(browserIntent);

				break;
			}

			case R.id.nav_aboutus:
				if (!manager.popBackStackImmediate(AboutusFragment.class.getName(), 0)) {

					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(AboutusFragment.class.getName());
					if (aboutusFragment == null) {
						aboutusFragment = new AboutusFragment();
					}
					trans.replace(R.id.content_main, aboutusFragment);
					trans.commit();
				}
				break;

			case R.id.nav_datenschutz: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Define.DATENSCHUTZURL));
				startActivity(browserIntent);
				break;
			}

			case R.id.nav_primuss:
				if (!manager.popBackStackImmediate(PrimussTabFragment.class.getName(), 0)) {

					FragmentTransaction trans = manager.beginTransaction();
					trans.addToBackStack(PrimussTabFragment.class.getName());
					if (primussTabFragment == null) {
						primussTabFragment = new PrimussTabFragment();
					}
					trans.replace(R.id.content_main, primussTabFragment);
					trans.commit();
				}
				break;
		}

		final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}
