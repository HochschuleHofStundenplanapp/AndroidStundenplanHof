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


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
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

	/**
	 * @param savedInstanceState
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		loginController = LoginController.getInstance(getActivity());

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
		} else {
			edtLogin.setEnabled(false);
		}

		experimentalFeatures.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				final Preference edtLogin = findPreference("login");
				final MainActivity activity = (MainActivity) getActivity();
				if ( (Boolean) newValue ) {
					new AlertDialog.Builder(getView().getContext())
							.setTitle(getString(R.string.enableExperimental))
							.setMessage(getString(R.string.enableExperimentalSure))
							.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//nothing to do here. Just close the message
								}
							})
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
	}

	/**
	 *
	 */
	@Override
	public final void onResume() {
		super.onResume();

		if ( studyCourseList == null ) {
			updateCourseListPreference("", false);
		}

		updateSemesterData(PreferenceManager.getDefaultSharedPreferences(getView().getContext()).getString("studiengang", ""));
//        updateSemesterListPreference();
		refreshSummaries();
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
						updateCourseListPreference(newValue.toString(), false);
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


}

