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

package de.hof.university.app.fragment.settings;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hof.university.app.Communication.RegisterLectures;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.calendar.CalendarInterfaceController;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.model.settings.StudyCourse;

/**
 * Created by Lukas on 24.11.2015.
 */
public class SettingsFragment extends PreferenceFragment {

	public final static String TAG = "SettingsFragment";

	private ProgressDialog progressDialog;
	private List<StudyCourse> studyCourseList;
	private LoginController loginController = null;
	private CalendarInterfaceController calendarInterfaceController = null;

    private final int REQUEST_CODE_ASK_CALENDAR_PERMISSIONS =  2;

	/**
	 * @param savedInstanceState
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		loginController = LoginController.getInstance(getActivity());
		calendarInterfaceController = CalendarInterfaceController.getInstance(getActivity().getApplicationContext());

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final ListPreference lpSemester = (ListPreference) findPreference("semester");

		if ( lpSemester != null ) {
			lpSemester.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					return ((ListPreference) preference).getEntries().length > 0;
				}
			});
			lpSemester.setEnabled(false);
		}


		final ListPreference lpCourse = (ListPreference) findPreference("studiengang");
		if ( lpCourse != null ) {
			lpCourse.setEnabled(false);
		}

		// Benachrichtigungen
		final PreferenceCategory category_notification = (PreferenceCategory) findPreference("category_notification");
		final CheckBoxPreference changes_notifications = (CheckBoxPreference) findPreference("changes_notifications");

		PreferenceScreen preferenceScreen = getPreferenceScreen();

		if (Define.PUSH_NOTIFICATIONS_ENABLED) {
			changes_notifications.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if ( (Boolean) newValue ) {
						// für Push-Notifications registrieren,
						// falls schon ein Stundenplan angelegt wurde
						DataManager.getInstance().registerFCMServerForce(getActivity().getApplicationContext());
                        new AlertDialog.Builder(getView().getContext())
                                .setTitle(R.string.notifications)
                                .setMessage(R.string.notifications_infotext)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //nothing to do here. Just close the message
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
					} else {
						// von Push-Notifications abmelden
						new RegisterLectures().deRegisterLectures();
					}
					return true;
				}
			});
		} else {
			preferenceScreen.removePreference(category_notification);
			preferenceScreen.removePreference(changes_notifications);
		}

		// Calendar syncronization
		final CheckBoxPreference calendar_syncronization = (CheckBoxPreference) findPreference("calendar_synchronization");

		calendar_syncronization.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( (Boolean) newValue ) {
					if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
							|| ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
						requestCalendarPermission();
					} else {
						turnCalendarSyncOn();
					}
				} else {
					CalendarInterfaceController.getInstance(getActivity().getApplicationContext()).removeCalendar();
				}
				return true;
			}
		});

		//Login für die experimentellen Funktionen
		Preference edtLogin = findPreference("login");
		edtLogin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (isVisible()) { //nur wenn die Activity sichtbar ist den Dialog anzeigen
					loginController.showLoginDialog();
					return true;
				} else {
					return false;
				}
			}
		});

		final CheckBoxPreference experimentalFeatures = (CheckBoxPreference) findPreference("experimental_features");

		if ( experimentalFeatures.isChecked() ) {
			edtLogin.setEnabled(true);
			calendar_syncronization.setEnabled(true);
			//changes_notifications.setEnabled(true);
		} else {
			edtLogin.setEnabled(false);
			calendar_syncronization.setEnabled(false);
			//changes_notifications.setEnabled(false);
		}

		experimentalFeatures.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final Preference edtLogin = findPreference("login");
//				final CheckBoxPreference changes_notifications = (CheckBoxPreference) findPreference("changes_notifications");
				final MainActivity activity = (MainActivity) getActivity();
				if ( (Boolean) newValue ) {
					new AlertDialog.Builder(getView().getContext())
							.setTitle(getString(R.string.enableExperimental))
							.setMessage(getString(R.string.enableExperimentalSure))
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//nothing to do here. Just close the message
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
					edtLogin.setEnabled(true);
					calendar_syncronization.setEnabled(true);
					/*if (changes_notifications != null) {
						changes_notifications.setEnabled(true);
						// falls ausgewählt war
						if (changes_notifications.isChecked()) {
							// für Push-Notifications registrieren,
							// falls schon ein Stundenplan angelegt wurde
							DataManager.getInstance().registerFCMServerForce(MainActivity.contextOfApplication);
						}
					}*/
					activity.displayExperimentalFeaturesMenuEntries(true);

				} else {
					edtLogin.setEnabled(false);
					calendar_syncronization.setEnabled(false);
					/*if (changes_notifications != null) {
						changes_notifications.setEnabled(false);
						// von Push-Notifications abmelden
						new RegisterLectures().deRegisterLectures();
					}*/
					activity.displayExperimentalFeaturesMenuEntries(false);
				}
				return true;
			}
		});

		enableSettingsSummary();
	}

	/**
	 *
	 */
	@Override
	public final void onResume() {
		super.onResume();
		MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.einstellungen);

		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(true);

		if ( studyCourseList == null ) {
			updateCourseListPreference("", false);
		}

		updateSemesterData(PreferenceManager.getDefaultSharedPreferences(getView().getContext()).getString("studiengang", ""));
//        updateSemesterListPreference();
		refreshSummaries();
	}

	@Override
	public void onPause() {
		super.onPause();
		MainActivity mainActivity = (MainActivity) getActivity();
		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(false);
	}

	private void refreshSummaries() {
	    /*
        EditTextPreference edtName = (EditTextPreference) findPreference("primuss_user");
        edtName.setSummary(edtName.getText());
        */

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());

		ListPreference lpCourse = (ListPreference) findPreference("studiengang");
		lpCourse.setSummary(sharedPreferences.getString("studiengang", ""));

		ListPreference lpSemester = (ListPreference) findPreference("semester");
		lpSemester.setSummary(sharedPreferences.getString("semester", ""));

		ListPreference lpTarif = (ListPreference) findPreference("speiseplan_tarif");
		lpTarif.setSummary(lpTarif.getEntry());

		ListPreference lpTermTime = (ListPreference) findPreference("term_time");
		lpTermTime.setSummary(lpTermTime.getEntry());
	}


	/**
	 * Alle eingestellten Werte werden nun unterhalb des jeweiligen Punktes angezeigt.
	 */
	private void enableSettingsSummary() {
		ListPreference lpCourse = (ListPreference) findPreference("studiengang");
		lpCourse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, final Object newValue) {
				preference.setSummary(newValue.toString());
				updateSemesterData(newValue.toString());
				return true;
			}
		});

		ListPreference lpSemester = (ListPreference) findPreference("semester");
		lpSemester.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		ListPreference lpTarif = (ListPreference) findPreference("speiseplan_tarif");
		lpTarif.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( preference instanceof ListPreference ) {
					ListPreference listPreference = (ListPreference) preference;
					int index = listPreference.findIndexOfValue(newValue.toString());
					if ( index >= 0 ) {
						listPreference.setSummary(listPreference.getEntries()[ index ]);
						return true;
					}
				}
				return false;
			}
		});

		ListPreference lpTermTime = (ListPreference) findPreference("term_time");
		lpTermTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( preference instanceof ListPreference ) {
					ListPreference listPreference = (ListPreference) preference;
					int index = listPreference.findIndexOfValue(newValue.toString());
					if ( index >= 0 ) {
						listPreference.setSummary(listPreference.getEntries()[ index ]);
						updateCourseListPreference(newValue.toString(), true);
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Aktuallisiert die Studiengänge aus der Datenbank mit der ListPreference
	 */
	private void updateCourseListPreference(String term, boolean forceRefresh) {

		if ( term.isEmpty() ) {
			ListPreference lpTermTime = (ListPreference) findPreference("term_time");
			term = lpTermTime.getValue();
		}

		//Cancel, if no termTime is set!
		if ( term == null ) {
			Toast.makeText(getActivity(), getString(R.string.noTermTimeSelected), Toast.LENGTH_LONG).show();
			return;
		}

		// Beim ersten Start braucht er noch keine Abfrage zu machen weil noch keine termTime ausgewählt wurde.
		if (term.isEmpty()) {
			return;
		}

		String[] params = new String[ 2 ];
		params[ 0 ] = term;
		params[ 1 ] = String.valueOf(forceRefresh);

		SettingsFragment.GetSemesterTask getSemesterTask = new SettingsFragment.GetSemesterTask();
		getSemesterTask.execute(params);

	}


	/**
	 * @param menu
	 * @param inflater
	 */
	@Override
	public final void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.settings_main, menu);
	}

	/**
	 * @param item
	 * @return
	 */
	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		if ( item.getItemId() == R.id.action_refresh ) {
			updateCourseListPreference("", true);
			return super.onOptionsItemSelected(item);
		} else {
			return super.onOptionsItemSelected(item);
		}
	}


	/**
	 * Öffnet Prozessdialog und aktuallisiert die Semester zum dem zuvor ausgewählten Studiengang
	 *
	 * @param courseStr Studiengang dessen Semester geladen werden
	 */
	private void updateSemesterData(final String courseStr) {
		ListPreference lpSemester = (ListPreference) findPreference("semester");
		if ( (studyCourseList == null) || courseStr.isEmpty() ) {
			lpSemester.setEntries(new CharSequence[]{});
			lpSemester.setEntryValues(new CharSequence[]{});
			return;
		}

		for ( StudyCourse studyCourse : studyCourseList ) {
			if ( studyCourse.getTag().equals(courseStr) ) {
				CharSequence[] entries = new CharSequence[ studyCourse.getTerms().size() ];
				CharSequence[] entryValues = new CharSequence[ studyCourse.getTerms().size() ];
				for ( int j = 0; j < studyCourse.getTerms().size(); ++j ) {
					entries[ j ] = studyCourse.getTerms().get(j);
					entryValues[ j ] = studyCourse.getTerms().get(j);
				}

				if ( lpSemester != null ) {
					if ( entries.length > 0 ) {
						lpSemester.setEntries(entries);
						lpSemester.setEntryValues(entryValues);
						lpSemester.setEnabled(true);
					} else {
						lpSemester.setEnabled(false);
					}
				}
			}
		}
	}


	private class GetSemesterTask extends AsyncTask<String, Void, Void> {

		CharSequence[] entries = null;
		CharSequence[] entryValues = null;

		@Override
		protected final void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setCancelable(true);
			progressDialog.setMessage(getString(R.string.onclick_refresh));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(false);
			progressDialog.show();

		}

		@Override
		protected final Void doInBackground(String... params) {
			String termTime = params[ 0 ];
			boolean pForceRefresh = Boolean.valueOf(params[ 1 ]);

			studyCourseList = DataManager.getInstance().getCourses(getActivity().getBaseContext(), getString(R.string.language), termTime, pForceRefresh);

			if (studyCourseList != null) {
				entries = new CharSequence[studyCourseList.size()];
				entryValues = new CharSequence[studyCourseList.size()];

				StudyCourse studyCourse;
				for (int i = 0; i < studyCourseList.size(); ++i) {
					if (studyCourseList.get(i) instanceof StudyCourse) {
						studyCourse = studyCourseList.get(i);
						entries[i] = studyCourse.getName();
						entryValues[i] = studyCourse.getTag();
						//entryValues[i]= String.valueOf(studyCourseList.get(i).getId());
					}
				}
			}
			return null;
		}

		@Override
		protected final void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			ListPreference lpCourse = (ListPreference) findPreference("studiengang");
			if (entries != null) {
				if (entries.length > 0) {
					lpCourse.setEntries(entries);
					lpCourse.setEntryValues(entryValues);
					lpCourse.setEnabled(true);
					updateSemesterData(lpCourse.getValue());
				}
			}

			progressDialog.dismiss();
		}
	}

	public void requestCalendarPermission() {
		if (Build.VERSION.SDK_INT > 23) {
			this.requestPermissions(
					new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
					REQUEST_CODE_ASK_CALENDAR_PERMISSIONS);
		}
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_ASK_CALENDAR_PERMISSIONS:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted
					turnCalendarSyncOn();
				} else {
					// Permission Denied
					Toast.makeText(getActivity(), "Berechtigung für den Kalender verweigert", Toast.LENGTH_SHORT)
							.show();
					// Calendar Sync aus schalten
					findPreference("calendar_synchronization").setEnabled(false);
				}
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void turnCalendarSyncOn() {


		final ArrayList<String> calendars = calendarInterfaceController.getCalendars();

		calendars.add(getString(R.string.newLocalCalendar));

		//final ArrayAdapter<String> calendarsAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, calendars);

		new AlertDialog.Builder(getView().getContext())
				.setTitle("Kalender Sync an")
				.setMessage("Sync an")
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new AlertDialog.Builder(getView().getContext())
								.setTitle("Kalender Sync an 2")
								//.setMessage("Sync an 2")
								.setSingleChoiceItems(calendars.toArray(new String[calendars.size()]), 0, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String calendarName = calendars.get(which);
										if (calendarName.equals(getString(R.string.newLocalCalendar))) {
											calendarInterfaceController.setCalendar(null);
										} else {
											calendarInterfaceController.setCalendar(calendarName);
										}
										calendarInterfaceController.createAllEvents();
									}
								})
								.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										//nothing to do here. Just close the message
									}
								})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
}
