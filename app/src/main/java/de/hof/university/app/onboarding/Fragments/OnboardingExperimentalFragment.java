package de.hof.university.app.onboarding.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.CheckBoxPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.calendar.CalendarSynchronization;
import de.hof.university.app.experimental.LoginController;
import de.hof.university.app.onboarding.OnboardingController;

public class OnboardingExperimentalFragment extends Fragment {

    private Button finishOnboardingBtn, loginBtn;
    private CheckBox featuresCb, synchronizationCb;

    private LoginController loginController = null;
    private CalendarSynchronization calendarSynchronization = null;

    private final int REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION =  2;
    private final int REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION =  3;

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

        this.loginController = LoginController.getInstance(getActivity());
        this.calendarSynchronization = CalendarSynchronization.getInstance();
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

                if (featuresCb.isChecked()) {

                }
                else {

                }
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
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .create();
                    d.show();
                }
            }
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
                    calendarSynchronization.stopCalendarSynchronization();
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

        //new OnboardingController().onboardingFinished(getActivity());

        MainActivity mainActivity = (MainActivity) getActivity();
        Log.v("MainActivity", "" + mainActivity);

        FragmentManager manager = getFragmentManager();
        manager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);


        mainActivity.checkStartingScreen();
    }
}
