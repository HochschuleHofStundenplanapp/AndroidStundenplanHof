package de.hof.university.app.data;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;

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
    private MainActivity mActivity;
    private Fragment mFragment;
    private ProgressDialog progressDialog;
    private List<StudyCourse> studyCourseList;


    public SettingsController(Activity activity, Fragment fragment) {
        this.mActivity = (MainActivity) activity;
        this.mFragment = fragment;
        this.loginController = LoginController.getInstance(mActivity);
        this.calendarSynchronization = CalendarSynchronization.getInstance();
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
                    mActivity.getString(R.string.language), termTime, pForceRefresh);

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

            HashMap<String, CharSequence[]> data = new HashMap<>();
            data.put("entries", entries);
            data.put("entryValues", entryValues);

            progressDialog.dismiss();
            mCallback.onTaskComplete(data);
        }
    }
}
