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


package de.hof.university.app.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import de.hof.university.app.data.SettingsController;

/**
 * Created by patrickniepel on 25.12.17.
 */

public class OnboardingController {

    private final String PREFERENCES_ONBOARDING = "OnboardingPrefs";
    private final String ONBOARDING_KEY = "Onboarding";
    private Activity mActivity;

    public  OnboardingController() {}

    public OnboardingController(Activity activity) {
        this.mActivity = activity;
    }

    public boolean shouldStartOnboaringIfNeeded(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_ONBOARDING, Context.MODE_PRIVATE);

        boolean startedOnce = prefs.getBoolean(ONBOARDING_KEY, false);

        // Onboarding hasnt been started once but schedule is available -> no need to start onboarding
        if(new SettingsController(mActivity).areStudySettingsAvailable()) {
            startedOnce = true;
            onboardingFinished(mActivity);
        }

        //Returns true if onboarding hasn't been started yet -> start onboarding
        return !startedOnce;
    }

    public void onboardingFinished(Context context) {
        setOnboardingStart(true, context);
    }

    public void resetOnboarding(Context context) {
        setOnboardingStart(false, context);
    }

    private void setOnboardingStart(boolean startedOnce, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_ONBOARDING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();

        editor.putBoolean(ONBOARDING_KEY, startedOnce);
        editor.apply();
    }
}
