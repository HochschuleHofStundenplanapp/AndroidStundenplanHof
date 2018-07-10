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


package de.hof.university.app.onboarding.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.preference.CheckBoxPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.hof.university.app.GDrive.GoogleDriveController;
import de.hof.university.app.GDrive.NetworkUtil;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.calendar.CalendarSynchronization;
import de.hof.university.app.communication.RegisterLectures;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.data.SettingsController;
import de.hof.university.app.data.SettingsController.SettingsKeys;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.onboarding.OnboardingController;

public class OnboardingExperimentalFragment extends Fragment {



    private Button finishOnboardingBtn, loginBtn;
    private CheckBox featuresCb, synchronizationCb, gDriveCb;

    private final int REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION =  2;
    private final int REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION =  3;

    private SettingsController settingsCtrl;
    private GoogleDriveController gDriveCtrl= null;
    private BroadcastReceiver networkChangeReceiver = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsCtrl = new SettingsController(getActivity(), this);
        gDriveCtrl = GoogleDriveController.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_onboarding_experimental, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLayout();
        setupClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.onboarding_experimental);
    }

    private void setupLayout() {
        finishOnboardingBtn = getActivity().findViewById(R.id.onboarding_experimental_finish_button);
        loginBtn = getActivity().findViewById(R.id.onboarding_experimental_login_button);

        featuresCb = getActivity().findViewById(R.id.onboarding_experimental_features_checkbox);
        synchronizationCb = getActivity().findViewById(R.id.onboarding_experimental_synchronization_checkbox);
        gDriveCb = getActivity().findViewById(R.id.onboarding_experimental_gDrive_checkbox);
    }

    private void setupClickListener() {

        finishOnboardingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishOnboarding();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        featuresCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                final MainActivity activity = (MainActivity) getActivity();

                if(b) {
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

                    activity.displayExperimentalFeaturesMenuEntries(true);
                }
                else {
                    activity.displayExperimentalFeaturesMenuEntries(false);
                }

                settingsCtrl.saveBooleanSettings(SettingsKeys.EXPERIMENTAL, b);
            }
        });

        synchronizationCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (synchronizationCb.isChecked()) {

                    // an schalten
                    turnCalendarSyncOn();
                }
                else {

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
                                    settingsCtrl.turnCalendarSyncOff();
                                }
                            })
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .create();
                    d.show();
                }
                settingsCtrl.saveBooleanSettings(SettingsKeys.CALENDAR_SYNC, b);
            }
        });
        gDriveCb.setEnabled(NetworkUtil.isNetworkAvailable(getContext()));
        networkChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = !(NetworkUtil.getConnectivityStatus(getContext())==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED);
                gDriveCb.setEnabled(isConnected);
            }
        };
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        getContext().registerReceiver(networkChangeReceiver, intentFilter);
        gDriveCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsCtrl.saveBooleanSettings(SettingsKeys.GDRIVE, isChecked);
            gDriveCtrl.sync(isChecked);
        });
    }

    private void requestCalendarPermission(int requestCode) {
        // From MARSHMALLOW (OS 6) on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            this.requestPermissions(
                    new String[]{Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR},
                    requestCode);
        }
    }

    private void turnCalendarSyncOn() {
        final ArrayList<String> calendars = settingsCtrl.turnCalendarSyncOn();
        final CalendarSynchronization calendarSync = settingsCtrl.getCalendarSynchronization();

        if (calendars == null) {
            return;
        }

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
                                            calendarSync.setCalendar(null);
                                        } else {
                                            calendarSync.setCalendar(calendarName);
                                        }
                                        calendarSync.createAllEvents();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Kalender Synchronisation ausschalten
                                        synchronizationCb.setChecked(false);
                                        //((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        // Kalender Synchronisation ausschalten
                                        synchronizationCb.setChecked(false);
                                        //((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
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
                        synchronizationCb.setChecked(false);
                        //((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        d.show();

        // Make the textview clickable. Must be called after show()
        ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
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
                    synchronizationCb.setChecked(false);
                    //((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
                }
                break;
            case REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    settingsCtrl.getCalendarSynchronization().stopCalendarSynchronization();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), R.string.calendar_synchronization_permissionNotGranted, Toast.LENGTH_SHORT)
                            .show();
                    // Calendar Sync ein schalten
                    synchronizationCb.setChecked(true);
                    //((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(true);
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void finishOnboarding() {

        new OnboardingController().onboardingFinished(getActivity());

        MainActivity mainActivity = (MainActivity) getActivity();
        Log.v("MainActivity", "" + mainActivity);

        FragmentManager manager = getFragmentManager();
        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        mainActivity.checkStartingScreen();
    }


    @Override
    public void onPause() {
        super.onPause();
        try {
            getContext().unregisterReceiver(networkChangeReceiver);
        }catch (IllegalArgumentException e){
            Log.i("OnbardingExpFragment","Receiver wasn't registered");
        }
    }
}
