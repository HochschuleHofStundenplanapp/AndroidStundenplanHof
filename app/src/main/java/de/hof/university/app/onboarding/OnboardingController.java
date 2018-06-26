package de.hof.university.app.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by patrickniepel on 25.12.17.
 */

public class OnboardingController {

    private final String PREFERENCES_ONBOARDING = "OnboardingPrefs";
    private final String ONBOARDING_KEY = "Onboarding";

    public  OnboardingController() {}

    public boolean shouldStartOnboaringIfNeeded(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_ONBOARDING, Context.MODE_PRIVATE);

        boolean startedOnce = prefs.getBoolean(ONBOARDING_KEY, false);

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
        editor.commit();
    }
}
