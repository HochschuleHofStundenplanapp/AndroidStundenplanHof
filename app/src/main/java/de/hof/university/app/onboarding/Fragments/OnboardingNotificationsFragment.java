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


import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.communication.RegisterLectures;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.data.SettingsController;
import de.hof.university.app.data.SettingsKeys;
import de.hof.university.app.util.Define;

public class OnboardingNotificationsFragment extends Fragment {

    private Button continueBtn;
    private CheckBox changesCb;
    private SettingsController settingsCtrl;
    private boolean userReceivedInfo = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsCtrl = new SettingsController(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_notifications, container, false);
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
        mainActivity.getSupportActionBar().setTitle(R.string.onboarding_notifications);
    }

    private void setupLayout() {
        continueBtn = getActivity().findViewById(R.id.onboarding_notifications_continue_button);
        changesCb = getActivity().findViewById(R.id.onboarding_notifications_changes_checkbox);
    }

    private void setupClickListener() {

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOnboardingExperimental();
            }
        });

        changesCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                // Checked 
                if (b) {

                    if (Define.PUSH_NOTIFICATIONS_ENABLED && !userReceivedInfo) {

                        settingsCtrl.registerFCMServerForce(getActivity().getApplicationContext());
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

                        userReceivedInfo = true;
                    }
                }
                else {
                    // von Push-Notifications abmelden
                    settingsCtrl.deregisterPushNotifications();
                    userReceivedInfo = false;
                }

                settingsCtrl.saveBooleanSettings(SettingsKeys.NOTIFICATIONS, b);
            }
        });
    }

    private void startOnboardingExperimental() {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.addToBackStack(OnboardingExperimentalFragment.class.getName());
        trans.replace(R.id.content_main, new OnboardingExperimentalFragment());
        trans.commit();
    }
}
