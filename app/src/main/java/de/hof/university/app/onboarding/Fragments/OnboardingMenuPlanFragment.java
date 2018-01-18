package de.hof.university.app.onboarding.Fragments;


import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;


public class OnboardingMenuPlanFragment extends Fragment {

    private Button continueBtn, tariffBtn;
    private CheckBox mainCourseCb, sideDishesCb, pastaCb, dessertsCb, saladCb;
    private ArrayList<String> tariffList;
    private String selectedTariff = "";

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

        tariffList = new ArrayList<>();

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

        fillTariffList();
    }

    private void setupClickListener() {

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Tariff must be selected to continue
                if (selectedTariff.isEmpty()) {
                    new AlertDialog.Builder(getView().getContext())
                            .setTitle("Error")
                            .setMessage(R.string.onboarding_error_not_selected_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //nothing to do here. Just close the message
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    startOnboardingNotifications();
                }
                startOnboardingNotifications();
            }
        });

        tariffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
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

    private void fillTariffList() {
        String[] tariffArray = MainActivity.getAppContext().getResources().getStringArray(R.array.speiseplan_tarife);
        for (String t : tariffArray) {
            tariffList.add(t);
        }
    }

    private void createDialog() {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);

        valueAdapter.addAll(tariffList);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(valueAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                selectedTariff = valueAdapter.getItem(which);
                tariffBtn.setText(selectedTariff);
            }
        });
        builderSingle.show();
    }

    private void startOnboardingNotifications() {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.addToBackStack(OnboardingNotificationsFragment.class.getName());
        trans.replace(R.id.content_main, new OnboardingNotificationsFragment());
        trans.commit();
    }
}
