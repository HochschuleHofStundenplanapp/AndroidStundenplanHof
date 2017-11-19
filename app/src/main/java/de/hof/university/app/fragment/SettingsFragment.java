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
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hof.university.app.Communication.RegisterLectures;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.calendar.CalendarSynchronization;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.model.settings.StudyCourse;

import static android.os.Build.VERSION_CODES;

/**
 * Created by Lukas on 24.11.2015.
 */
public class SettingsFragment extends PreferenceFragment {

	public final static String TAG = "SettingsFragment";

	private ProgressDialog progressDialog;
	private List<StudyCourse> studyCourseList;
	private LoginController loginController = null;
	private CalendarSynchronization calendarSynchronization = null;

    private final int REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION =  2;
    private final int REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION =  3;

	/**
	 * @param savedInstanceState
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		this.loginController = LoginController.getInstance(getActivity());
		this.calendarSynchronization = CalendarSynchronization.getInstance();

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_SEMESTER));

		if ( lpSemester != null ) {
			lpSemester.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					return ((ListPreference) preference).getEntries().length > 0;
				}
			});
			lpSemester.setEnabled(false);
		}


		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_STUDIENGANG));
		if ( lpCourse != null ) {
			lpCourse.setEnabled(false);
		}

		// Benachrichtigungen
		final CheckBoxPreference changes_notifications = (CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CHANGES_NOTIFICATION));

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
			final PreferenceScreen preferenceScreen = getPreferenceScreen();
			final PreferenceCategory category_notification = (PreferenceCategory) findPreference(getString(R.string.PREFERENCE_KEY_CATEGORY_NOTIFICATION));

			preferenceScreen.removePreference(category_notification);
			preferenceScreen.removePreference(changes_notifications);
		}

		// Calendar synchronization
		final CheckBoxPreference calendar_syncronization = (CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION));

		calendar_syncronization.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( (Boolean) newValue ) {
					// an schalten
					turnCalendarSyncOn();
				} else {
					// aus schalten

					// mit einem Dialog nachfragen ob der Nutzer die Kalendereinträge behalten möchte
					final AlertDialog d = new AlertDialog.Builder(getView().getContext())
							.setTitle(R.string.calendar_syncronization_keep_events_title)
							.setMessage(R.string.calendar_syncronization_keep_events_message)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// behalten, mache nichts
								}
							})
							.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// löschen
									if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
											|| (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
										// keine Berechtigung, hole erst Berechtigung
										requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION);
									} else {
										// lösche die Kalendereinträge oder den lokalen Kalender
										calendarSynchronization.stopCalendarSynchronization();
									}
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.create();
					d.show();
				}
				return true;
			}
		});

		//Login für die experimentellen Funktionen
		final Preference edtLogin = findPreference(getString(R.string.PREFERENCE_KEY_LOGIN));
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

		final CheckBoxPreference experimentalFeatures = (CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_EXPERIMENTAL_FEATURES));

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
							.setTitle(getString(R.string.experimental_features))
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

		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.einstellungen);

		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(true);

		if ( studyCourseList == null ) {
			updateCourseListPreference("", false);
		}

		updateSemesterData(PreferenceManager.getDefaultSharedPreferences(getView().getContext()).getString(getString(R.string.PREFERENCE_KEY_STUDIENGANG), ""));
//        updateSemesterListPreference();
		refreshSummaries();
	}

	@Override
	public void onPause() {
		super.onPause();

		final MainActivity mainActivity = (MainActivity) getActivity();
		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);

		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(false);
	}

	private void refreshSummaries() {
	    /*
        EditTextPreference edtName = (EditTextPreference) findPreference("primuss_user");
        edtName.setSummary(edtName.getText());
        */

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());

		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_STUDIENGANG));
		lpCourse.setSummary(sharedPreferences.getString(getString(R.string.PREFERENCE_KEY_STUDIENGANG), ""));

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_SEMESTER));
		lpSemester.setSummary(sharedPreferences.getString(getString(R.string.PREFERENCE_KEY_SEMESTER), ""));

		final ListPreference lpTarif = (ListPreference) findPreference("speiseplan_tarif");
		lpTarif.setSummary(lpTarif.getEntry());

		final ListPreference lpTermTime = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_TERM_TIME));
		lpTermTime.setSummary(lpTermTime.getEntry());
	}


	/**
	 * Alle eingestellten Werte werden nun unterhalb des jeweiligen Punktes angezeigt.
	 */
	private void enableSettingsSummary() {
		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_STUDIENGANG));
		lpCourse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, final Object newValue) {
				preference.setSummary(newValue.toString());
				updateSemesterData(newValue.toString());
				return true;
			}
		});

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_SEMESTER));
		lpSemester.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		final ListPreference lpTarif = (ListPreference) findPreference("speiseplan_tarif");
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

		final ListPreference lpTermTime = (ListPreference) findPreference("term_time");
		lpTermTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( preference instanceof ListPreference ) {
					final ListPreference listPreference = (ListPreference) preference;
					final int index = listPreference.findIndexOfValue(newValue.toString());
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

		final String[] params = new String[ 2 ];
		params[ 0 ] = term;
		params[ 1 ] = String.valueOf(forceRefresh);

		final SettingsFragment.GetSemesterTask getSemesterTask = new SettingsFragment.GetSemesterTask();
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
	 * Öffnet Prozessdialog und aktualisiert die Semester zu dem zuvor ausgewählten Studiengang
	 *
	 * @param courseStr Studiengang dessen Semester geladen werden
	 */
	private void updateSemesterData(final String courseStr) {

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_SEMESTER));
		if ( (studyCourseList == null) || courseStr.isEmpty() ) {
			lpSemester.setEntries(new CharSequence[]{});
			lpSemester.setEntryValues(new CharSequence[]{});
			return;
		}

		for ( final StudyCourse studyCourse : studyCourseList ) {
			if ( studyCourse.getTag().equals(courseStr) ) {
				final CharSequence[] entries = new CharSequence[ studyCourse.getTerms().size() ];
				final CharSequence[] entryValues = new CharSequence[ studyCourse.getTerms().size() ];

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
			final String termTime = params[ 0 ];
			final boolean pForceRefresh = Boolean.valueOf(params[ 1 ]);

			studyCourseList = DataManager.getInstance().getCourses(getActivity().getBaseContext(),
					getString(R.string.language), termTime, pForceRefresh);

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
			ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREFERENCE_KEY_STUDIENGANG));
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

	private void requestCalendarPermission(int requestCode) {

		// From MARSHMELLOW (OS 6) on
		if (Build.VERSION.SDK_INT >= VERSION_CODES.M ) {
			this.requestPermissions(
					new String[]{Manifest.permission.READ_CALENDAR,
							Manifest.permission.WRITE_CALENDAR},
					requestCode);
		}
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted
					turnCalendarSyncOn();
				} else {
					// Permission Denied
					Toast.makeText(getActivity(), R.string.calendar_synchronization_permissionNotGranted, Toast.LENGTH_SHORT)
							.show();
					// Calendar Sync aus schalten
					((CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
				}
				break;
			case REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted
					calendarSynchronization.stopCalendarSynchronization();
				} else {
					// Permission Denied
					Toast.makeText(getActivity(), R.string.calendar_synchronization_permissionNotGranted, Toast.LENGTH_SHORT)
							.show();
					// Calendar Sync ein schalten
					((CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(true);
				}
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void turnCalendarSyncOn() {
		// check for permission
		// wenn keine Berechtigung dann requeste sie und falls erfolgreich komme hier her zurück
		if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
				|| (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
			// keine Berechtigung
			requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION);
			return;
		}

		final ArrayList<String> calendars = new ArrayList<>();

		// Den localen Kalender als erstes
		calendars.add(getString(R.string.calendar_synchronitation_ownLocalCalendar));

		// Die weiteren Kalender danach
		calendars.addAll(calendarSynchronization.getCalendarsNames());

		final AlertDialog d = new AlertDialog.Builder(getView().getContext())
				.setTitle(R.string.calendar_synchronization)
				.setMessage(R.string.calendar_synchronization_infoText)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new AlertDialog.Builder(getView().getContext())
								.setTitle(R.string.calendar_synchronization_chooseCalendar)
								.setItems(calendars.toArray(new String[calendars.size()]), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String calendarName = calendars.get(which);
										if (calendarName.equals(getString(R.string.calendar_synchronitation_ownLocalCalendar))) {
											// lokaler Kalender
											calendarSynchronization.setCalendar(null);
										} else {
											calendarSynchronization.setCalendar(calendarName);
										}
										calendarSynchronization.createAllEvents();
									}
								})
								.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Kalender Synchronisation ausschalten
										((CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
									}
								})
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										// Kalender Synchronisation ausschalten
										((CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
									}
								})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// Kalender Synchronisation ausschalten
						((CheckBoxPreference) findPreference(getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.create();
		d.show();

		// Make the textview clickable. Must be called after show()
		((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
}
