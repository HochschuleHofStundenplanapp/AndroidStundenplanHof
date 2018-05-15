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


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.communication.RegisterLectures;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.model.settings.StudyCourse;
import de.hof.university.app.util.Define;

/**
 * Created by Lukas on 24.11.2015.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	public final static String TAG = "SettingsFragment";

	private ProgressDialog progressDialog;
	private List<StudyCourse> studyCourseList;
	private LoginController loginController = null;

	/**
	 * @param savedInstanceState
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setHasOptionsMenu(true);

		this.loginController = LoginController.getInstance(getActivity());

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREF_KEY_SEMESTER));

		if ( lpSemester != null ) {
			lpSemester.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					return ((ListPreference) preference).getEntries().length > 0;
				}
			});
			lpSemester.setEnabled(false);
		}


		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREF_KEY_STUDIENGANG));
		if ( lpCourse != null ) {
			lpCourse.setEnabled(false);
		}

		final ListPreference lpCanteen = (ListPreference) findPreference(getString(R.string.PREF_KEY_SELECTED_CANTEEN));
		final CharSequence[] entries = {"Bayreuth","Coburg","Amberg", "Hof", "Weiden", "Münchberg"};
		final CharSequence[] entryValues = {"310", "320", "330", "340", "350","370"};
		//"310", "320", "330", "340", "350","370"
		if (lpCanteen != null){
			lpCanteen.setEntries(entries);
			lpCanteen.setEntryValues(entryValues);
			// Set default value (setDefaultValue-Method not function!)
			if (lpCanteen.getValue() == null) {
				lpCanteen.setValue("" + entryValues[3]);
			}
			lpCanteen.setEnabled(true);

			lpCanteen.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Define.mensa_changed = true;
					Log.d("Settings: ", "new Canteen selected! Invalidating Cache!");
					refreshCanteenSummary((String) newValue);
					return true;
				}
			});
		}
		else {
			lpCanteen.setEnabled(false);
		}



		// Benachrichtigungen
		final CheckBoxPreference changes_notifications = (CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CHANGES_NOTIFICATION));

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
			final PreferenceCategory category_notification = (PreferenceCategory) findPreference(getString(R.string.PREF_KEY_CATEGORY_NOTIFICATION));

			preferenceScreen.removePreference(category_notification);
			preferenceScreen.removePreference(changes_notifications);
		}


		// Calendar synchronization
		final Preference calendar_synchronzation_screen = findPreference( getString( R.string.PREF_KEY_SCREEN_CALENDAR_SYNCHRONIZATION ) );

		calendar_synchronzation_screen.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick( Preference preference ) {

				SettingsCalendarSynchronizationFragment settingsCalendarSynchronizationFragment = new SettingsCalendarSynchronizationFragment();
				getFragmentManager().beginTransaction()
						.addToBackStack( null )
						.replace( R.id.content_main, settingsCalendarSynchronizationFragment )
						.commit();

				return true;
			}
		} );


		//Login für die experimentellen Funktionen
		final Preference edtLogin = findPreference(getString(R.string.PREF_KEY_LOGIN));
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

		final CheckBoxPreference experimentalFeatures = (CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_EXPERIMENTAL_FEATURES));

		if ( experimentalFeatures.isChecked() ) {
			edtLogin.setEnabled(true);
			//changes_notifications.setEnabled(true);
		} else {
			edtLogin.setEnabled(false);
			//changes_notifications.setEnabled(false);
		}

		experimentalFeatures.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final Preference edtLogin = findPreference( getString( R.string.PREF_KEY_LOGIN) );
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
							.setCancelable(false)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
					edtLogin.setEnabled(true);
					activity.displayExperimentalFeaturesMenuEntries(true);

				} else {
					edtLogin.setEnabled(false);
					activity.displayExperimentalFeaturesMenuEntries(false);
				}
				return true;
			}
		});

		enableSettingsSummary();

		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 *
	 */
	@Override
	public final void onResume() {
		super.onResume();
		Log.i( TAG, "onResume" );

		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.einstellungen);

		final NavigationView navigationView = mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(true);

		if ( studyCourseList == null ) {
			updateCourseListPreference("", false);
		}


		updateSemesterData(PreferenceManager.getDefaultSharedPreferences(getView().getContext()).getString(getString(R.string.PREF_KEY_STUDIENGANG), ""));
//        updateSemesterListPreference();
		refreshSummaries();
	}

	@Override
	public boolean onPreferenceTreeClick( PreferenceScreen preferenceScreen, Preference preference ) {
		return super.onPreferenceTreeClick( preferenceScreen, preference );
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i( TAG, "onPause" );

		final MainActivity mainActivity = (MainActivity) getActivity();
		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);

		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i( TAG, "onDestroy" );

		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void refreshCanteenSummary(String newCanteen){
		String selectedMensa;
		switch (newCanteen){
			case "310":
				selectedMensa = "Bayreuth";
				break;
			case "320":
				selectedMensa = "Coburg";
				break;
			case "330":
				selectedMensa = "Amberg";
				break;
			case "340":
				selectedMensa = "Hof";
				break;
			case "350":
				selectedMensa = "Weiden";
				break;
			case "370":
				selectedMensa = "Münchberg";
				break;
			default:
				selectedMensa = "Hof";
		}

		final ListPreference lpCanteen = (ListPreference) findPreference(getString(R.string.PREF_KEY_SELECTED_CANTEEN));
		lpCanteen.setSummary(selectedMensa);

	}

	private void refreshSummaries() {
	    /*
        EditTextPreference edtName = (EditTextPreference) findPreference("primuss_user");
        edtName.setSummary(edtName.getText());
        */
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getView().getContext());
	    String selectedMensa;

	    switch (sharedPreferences.getString("selected_canteen", "340")){
			case "310":
				selectedMensa = "Bayreuth";
				break;
			case "320":
				selectedMensa = "Coburg";
				break;
			case "330":
				selectedMensa = "Amberg";
				break;
			case "340":
				selectedMensa = "Hof";
				break;
			case "350":
				selectedMensa = "Weiden";
				break;
			case "370":
				selectedMensa = "Münchberg";
				break;
			default:
				selectedMensa = "Hof";
		}


		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREF_KEY_STUDIENGANG));
		lpCourse.setSummary(sharedPreferences.getString(getString(R.string.PREF_KEY_STUDIENGANG), ""));

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREF_KEY_SEMESTER));
		lpSemester.setSummary(sharedPreferences.getString(getString(R.string.PREF_KEY_SEMESTER), ""));

		final ListPreference lpCanteen = (ListPreference) findPreference(getString(R.string.PREF_KEY_SELECTED_CANTEEN));
		lpCanteen.setSummary(selectedMensa);

		final ListPreference lpTarif = (ListPreference) findPreference(getString(R.string.PREF_KEY_MEAL_TARIFF));
		lpTarif.setSummary(lpTarif.getEntry());

		final ListPreference lpTermTime = (ListPreference) findPreference(getString(R.string.PREF_KEY_TERM_TIME));
		lpTermTime.setSummary(lpTermTime.getEntry());
	}


	/**
	 * Alle eingestellten Werte werden nun unterhalb des jeweiligen Punktes angezeigt.
	 */
	private void enableSettingsSummary() {
		final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREF_KEY_STUDIENGANG));
		lpCourse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, final Object newValue) {
				preference.setSummary(newValue.toString());
				updateSemesterData(newValue.toString());
				return true;
			}
		});

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREF_KEY_SEMESTER));
		lpSemester.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(newValue.toString());
				return true;
			}
		});

		final ListPreference lpTarif = (ListPreference) findPreference(getString( R.string.PREF_KEY_MEAL_TARIFF ));
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

		final ListPreference lpTermTime = (ListPreference) findPreference(getString( R.string.PREF_KEY_TERM_TIME ));
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
			ListPreference lpTermTime = (ListPreference) findPreference(MainActivity.getAppContext().getString( R.string.PREF_KEY_TERM_TIME ));
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

	private void updateCanteenData(){
		final  ListPreference lpCanteen = (ListPreference) findPreference(getString((R.string.PREF_KEY_SELECTED_CANTEEN)));
		final CharSequence[] entries = {"Bayreuth","Coburg","Amberg", "Hof", "Weiden", "Münchberg"};
		final CharSequence[] entryValues = {"310", "320", "330", "340", "350","370"};
		//"310", "320", "330", "340", "350","370"

		if ( lpCanteen != null ) {
			if ( entries.length > 0 ) {
				lpCanteen.setEntries(entries);
				lpCanteen.setEntryValues(entryValues);
				lpCanteen.setEnabled(true);
			} else {
				lpCanteen.setEnabled(false);
			}
		}


	}

	/**
	 * Öffnet Prozessdialog und aktualisiert die Semester zu dem zuvor ausgewählten Studiengang
	 *
	 * @param courseStr Studiengang dessen Semester geladen werden
	 */
	private void updateSemesterData(final String courseStr) {

		final ListPreference lpSemester = (ListPreference) findPreference(getString(R.string.PREF_KEY_SEMESTER));
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

	// Einstellungen aktualisieren wenn sie geändert werden
	@Override
	public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key ) {
		Log.i( TAG, "onSharedPreferenceChanged" );
		if ( getString( R.string.PREF_KEY_CHANGES_NOTIFICATION).equals( key ) ) {
			Log.i( TAG, "CHANES_NOTIFICATION has changed" );
			CheckBoxPreference changes_notification = (CheckBoxPreference) findPreference( key );
			changes_notification.setChecked( sharedPreferences.getBoolean( key, false ) );
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

			studyCourseList = DataManager.getInstance().getCourses(getActivity().getApplicationContext(),
					getString(R.string.language), termTime, pForceRefresh);

			if (studyCourseList != null) {
				final int length = studyCourseList.size();
				
				entries = new CharSequence[length];
				entryValues = new CharSequence[length];

				for (int i = 0; i < length; ++i) {
					StudyCourse studyCourse;
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
			final ListPreference lpCourse = (ListPreference) findPreference(getString(R.string.PREF_KEY_STUDIENGANG));
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
}
