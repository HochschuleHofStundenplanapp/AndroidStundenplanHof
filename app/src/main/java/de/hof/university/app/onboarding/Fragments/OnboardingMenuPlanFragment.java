package de.hof.university.app.onboarding.Fragments;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;


public class OnboardingMenuPlanFragment extends Fragment {

    private Button continueBtn, tariffBtn;
    private CheckBox mainCourseCb, sideDishesCb, pastaCb, dessertsCb, saladCb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_onboarding_menu_plan, container, false);
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
        mainActivity.getSupportActionBar().setTitle(R.string.onboarding_menu_plan);
    }

    private void setupLayout() {
        continueBtn = getActivity().findViewById(R.id.onboarding_menu_plan_continue_button);
        tariffBtn = getActivity().findViewById(R.id.onboarding_menu_plan_tariff_button);

        mainCourseCb = getActivity().findViewById(R.id.onboarding_menu_plan_main_course_checkbox);
        sideDishesCb = getActivity().findViewById(R.id.onboarding_menu_plan_side_dishes_checkbox);
        pastaCb = getActivity().findViewById(R.id.onboarding_menu_plan_pasta_checkbox);
        dessertsCb = getActivity().findViewById(R.id.onboarding_menu_plan_desserts_checkbox);
        saladCb = getActivity().findViewById(R.id.onboarding_menu_plan_salad_checkbox);
    }

    private void setupClickListener() {

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOnboardingNotifications();
            }
        });

        tariffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mainCourseCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (mainCourseCb.isChecked()) {

                }
                else {

                }
            }
        });

        sideDishesCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (sideDishesCb.isChecked()) {

                }
                else {

                }
            }
        });

        pastaCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (pastaCb.isChecked()) {

                }
                else {

                }
            }
        });

        dessertsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (dessertsCb.isChecked()) {

                }
                else {

                }
            }
        });

        saladCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (saladCb.isChecked()) {

                }
                else {

                }
            }
        });
    }

    private void startOnboardingNotifications() {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.addToBackStack(OnboardingNotificationsFragment.class.getName());
        trans.replace(android.R.id.content, new OnboardingNotificationsFragment());
        trans.commit();
    }
}
