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



package de.hof.university.app.data;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.calendar.CalendarSynchronization;
import de.hof.university.app.communication.RegisterLectures;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.model.settings.StudyCourse;

/**
 * Created by patrickniepel on 20.01.18.
 */

public class SettingsController  {

    private final int REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION =  2;
    private final int REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION =  3;
    private LoginController loginController = null;
    private CalendarSynchronization calendarSynchronization = null;
    private Activity mActivity;
    private Fragment mFragment;
    private ProgressDialog progressDialog;
    private List<StudyCourse> studyCourseList;
    private SharedPreferences sharedPreferences;
	
    public SettingsController(Activity activity) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        this.mActivity = activity;
    }

    public SettingsController(Activity activity, Fragment fragment) {
        this.mActivity = activity;
        this.mFragment = fragment;
        this.loginController = LoginController.getInstance(mActivity);
        this.calendarSynchronization = CalendarSynchronization.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    }

    public CalendarSynchronization getCalendarSynchronization() {
        return this.calendarSynchronization;
    }

    public LoginController getLoginController() {
        return this.loginController;
    }

    public List<StudyCourse> getStudyCourseList() {
        return this.studyCourseList;
    }

    /**
     * Für Push-Benachrichtigungen anmelden, falls schon ein Stundenplan angelegt wurde
     * @param context
     */
    public void registerFCMServerForce(Context context) {
        DataManager.getInstance().registerFCMServerForce(context);
    }

    /**
     * Von Push-Notifications abmelden
     */
    public void deregisterPushNotifications() {
        new RegisterLectures().deRegisterLectures();
    }

    public ArrayList<String> turnCalendarSyncOn() {
        // check for permission
        // wenn keine Berechtigung dann requeste sie und falls erfolgreich komme hier her zurück
        if ((ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
            // keine Berechtigung
            requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION);
            return null;
        }

        final ArrayList<String> calendars = new ArrayList<>();

        // Den localen Kalender als erstes
        calendars.add(mActivity.getString(R.string.calendar_synchronitation_ownLocalCalendar));

        // Die weiteren Kalender danach
        calendars.addAll(calendarSynchronization.getCalendarsNames());

        return calendars;
    }

    public void turnCalendarSyncOff() {
        if ((ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
            // keine Berechtigung, hole erst Berechtigung
            requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION);
        } else {
            // lösche die Kalendereinträge oder den lokalen Kalender
            calendarSynchronization.stopCalendarSynchronization();
        }
    }

    private void requestCalendarPermission(int requestCode) {

        // From MARSHMELLOW (OS 6) on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            mFragment.requestPermissions(
                    new String[]{Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR},
                    requestCode);
        }
    }

    public void executeSemesterTask(Fragment context, String[] params) {
        final SettingsController.GetSemesterTask getSemesterTask = new GetSemesterTask(context);
        getSemesterTask.execute(params);
    }

    public void saveStringSettings(final int keyRessourceId, final String value) {

        final String prefKey = mActivity.getString(keyRessourceId) ;
        sharedPreferences.edit().putString(prefKey, value).apply();
    }

    public void saveBooleanSettings(final int keyRessourceId, final boolean value) {

		final String prefKey = mActivity.getString(keyRessourceId) ;
		sharedPreferences.edit().putBoolean(prefKey, value).apply();
    }

    /** Reset settings when onboarding gets restarted in settings */
    public void resetSettings() {

        sharedPreferences.edit().putString( mActivity.getString(R.string.PREF_KEY_TERM_TIME), "").apply();
        sharedPreferences.edit().putString( mActivity.getString(R.string.PREF_KEY_STUDIENGANG), "").apply();
        sharedPreferences.edit().putString( mActivity.getString(R.string.PREF_KEY_SEMESTER), "").apply();
        sharedPreferences.edit().putString( mActivity.getString(R.string.PREF_KEY_MEAL_TARIFF), "").apply();
        sharedPreferences.edit().putString( mActivity.getString(R.string.PREF_KEY_SELECTED_CANTEEN), "").apply();

        //Meals
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_MAIN_COURSE), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_SIDE_DISHES), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_PASTA), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_DESSERTS), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_SALAD), false).apply();

        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_EXPERIMENTAL_FEATURES), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_CHANGES_NOTIFICATION), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION), false).apply();
        sharedPreferences.edit().putBoolean( mActivity.getString(R.string.PREF_KEY_GDRIVE_SYNC), false).apply();

        final MainActivity activity = (MainActivity) mActivity;
        activity.displayExperimentalFeaturesMenuEntries(false);
    }

    public boolean areStudySettingsAvailable() {
        return !sharedPreferences.getString(mActivity.getString(R.string.PREF_KEY_TERM_TIME), "").isEmpty()
                && !sharedPreferences.getString(mActivity.getString(R.string.PREF_KEY_STUDIENGANG), "").isEmpty()
                && !sharedPreferences.getString(mActivity.getString(R.string.PREF_KEY_SEMESTER), "").isEmpty();
    }

    private class GetSemesterTask extends AsyncTask<String, Void, Void> {

        CharSequence[] entries = null;
        CharSequence[] entryValues = null;
        private TaskComplete mCallback;

        public GetSemesterTask(Fragment context) {
            this.mCallback = (TaskComplete) context;
        }

        @Override
        protected final void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(mActivity.getString(R.string.onclick_refresh));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(false);
            progressDialog.show();

        }

        @Override
        protected final Void doInBackground(String... params) {
            final String termTime = params[ 0 ];
            final boolean pForceRefresh = Boolean.valueOf(params[ 1 ]);

            studyCourseList = DataManager.getInstance().getCourses(mActivity.getApplicationContext(),
                    mFragment.getString(R.string.language), termTime, pForceRefresh);

            if (studyCourseList != null) {
                entries = new CharSequence[studyCourseList.size()];
                entryValues = new CharSequence[studyCourseList.size()];

                for (int i = 0; i < studyCourseList.size(); ++i) {
                    if (studyCourseList.get(i) instanceof StudyCourse) {
						final StudyCourse studyCourse = studyCourseList.get(i);
                        entries[i] = studyCourse.getName();
                        //Log.d("---------------------", entries  + ""));
                        entryValues[i] = studyCourse.getTag();
                        //entryValues[i]= String.valueOf(studyCourseList.get(i).getId());
                    }
                }
            }

            /* TODO MS
            (this.getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Es gibt Probleme mit der Netzwerkverbindung. Bitte verbinde Dich neu, überprüfe die Zertifikate.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            */


            return null;
        }

        @Override
        protected final void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            HashMap<String, CharSequence[]> data = new HashMap<>();
            data.put("entries", entries);
            data.put("entryValues", entryValues);

            progressDialog.dismiss();
            mCallback.onTaskComplete(data);
        }
    }
}
