package de.hof.university.app.onboarding.Fragments;


import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.SettingsController;
import de.hof.university.app.data.SettingsKeys;


public class OnboardingMenuPlanFragment extends Fragment {

    private Button continueBtn, tariffBtn, canteenBtn;
    private CheckBox mainCourseCb, sideDishesCb, pastaCb, dessertsCb, saladCb;
    private ArrayList<String> tariffList, canteenList, canteenShort, tariffShort;
    private String selectedTariff = "";
    private String selectedCanteen = "";
    private SettingsController settingsCtrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tariffList = new ArrayList<>();
        tariffShort = new ArrayList<>();
        canteenList = new ArrayList<>();
        canteenShort = new ArrayList<>();
        settingsCtrl = new SettingsController(getActivity(), this);
    }

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
        fillLayoutIfPossible();
        setPresets();
    }

    @Override
    public void onStart() {
        super.onStart();

        fillTariffList();
        fillCanteenList();
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(Html.fromHtml("<font color='"+ ContextCompat.getColor(MainActivity.getAppContext(), R.color.colorBlack)+"'>"+ getString(R.string.onboarding_menu_plan)+"</font>"));
        mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_accent_24dp);
    }

    private void setupLayout() {
        continueBtn = getActivity().findViewById(R.id.onboarding_menu_plan_continue_button);
        tariffBtn = getActivity().findViewById(R.id.onboarding_menu_plan_tariff_button);
        canteenBtn = getActivity().findViewById(R.id.onboarding_menu_plan_cateen_button);

        mainCourseCb = getActivity().findViewById(R.id.onboarding_menu_plan_main_course_checkbox);
        sideDishesCb = getActivity().findViewById(R.id.onboarding_menu_plan_side_dishes_checkbox);
        pastaCb = getActivity().findViewById(R.id.onboarding_menu_plan_pasta_checkbox);
        dessertsCb = getActivity().findViewById(R.id.onboarding_menu_plan_desserts_checkbox);
        saladCb = getActivity().findViewById(R.id.onboarding_menu_plan_salad_checkbox);
    }

    // Preselect
    private void setPresets() {
        mainCourseCb.setChecked(true);
        sideDishesCb.setChecked(true);
        pastaCb.setChecked(true);
        dessertsCb.setChecked(true);
        saladCb.setChecked(true);

        //Save settings
        settingsCtrl.saveBooleanSettings(SettingsKeys.MAIN_COURSE, true);
        settingsCtrl.saveBooleanSettings(SettingsKeys.SIDE_DISHES, true);
        settingsCtrl.saveBooleanSettings(SettingsKeys.PASTA, true);
        settingsCtrl.saveBooleanSettings(SettingsKeys.DESSERT, true);
        settingsCtrl.saveBooleanSettings(SettingsKeys.SALAD, true);
    }

    private void setupClickListener() {

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Tariff must be selected to continue
                if (selectedTariff.isEmpty() || selectedCanteen.isEmpty()) {
                    new AlertDialog.Builder(getView().getContext())
                            .setTitle(R.string.onboarding_error_text)
                            .setMessage(R.string.onboarding_error_not_selected_message_menu)
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
            }
        });

        canteenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog("canteen");
            }
        });

        tariffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog("tariff");
            }
        });

        mainCourseCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsCtrl.saveBooleanSettings(SettingsKeys.MAIN_COURSE, b);
            }
        });

        sideDishesCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsCtrl.saveBooleanSettings(SettingsKeys.SIDE_DISHES, b);
            }
        });

        pastaCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsCtrl.saveBooleanSettings(SettingsKeys.PASTA, b);
            }
        });

        dessertsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsCtrl.saveBooleanSettings(SettingsKeys.DESSERT, b);
            }
        });

        saladCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                settingsCtrl.saveBooleanSettings(SettingsKeys.SALAD, b);
            }
        });
    }

    private void fillLayoutIfPossible() {
        if (!selectedTariff.isEmpty()) {
            tariffBtn.setText(selectedTariff);
        }

        if (!selectedCanteen.isEmpty()) {
            canteenBtn.setText(selectedCanteen);
        }
    }

    private void fillTariffList() {
        String[] tariffArray = MainActivity.getAppContext().getResources().getStringArray(R.array.speiseplan_tarife);
        for (String t : tariffArray) {
            tariffList.add(t);
        }

        String[] tariffShortArray = MainActivity.getAppContext().getResources().getStringArray(R.array.speiseplan_tarife_values);
        for (String t : tariffShortArray) {
            tariffShort.add(t);
        }
    }

    private void fillCanteenList() {
        String[] canteenArray = MainActivity.getAppContext().getResources().getStringArray(R.array.canteen);
        for (String t : canteenArray) {
            canteenList.add(t);
        }

        String[] canteenShortArray = MainActivity.getAppContext().getResources().getStringArray(R.array.canteen_values);
        for (String t : canteenShortArray) {
            canteenShort.add(t);
        }
    }

    private void createDialog(final String valueKey) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item);

        if (valueKey.equals("tariff")) {
            valueAdapter.addAll(tariffList);
        }
        else if (valueKey.equals("canteen")) {
            valueAdapter.addAll(canteenList);
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(valueAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (valueKey.equals("tariff")) {
                    selectedTariff = valueAdapter.getItem(which);
                    tariffBtn.setText(selectedTariff);
                    settingsCtrl.saveStringSettings(SettingsKeys.TARIFF, tariffShort.get(which));

                }
                else if (valueKey.equals("canteen")) {
                    selectedCanteen = valueAdapter.getItem(which);
                    canteenBtn.setText(selectedCanteen);
                    settingsCtrl.saveStringSettings(SettingsKeys.CANTEEN, canteenShort.get(which));
                }
            }
        });
        builderSingle.show();
    }

    private void startOnboardingNotifications() {

        resetLists();

        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.addToBackStack(OnboardingNotificationsFragment.class.getName());
        trans.replace(R.id.content_main, new OnboardingNotificationsFragment());
        trans.commit();
    }

    private void resetLists() {
        tariffList.clear();
        tariffShort.clear();

        canteenList.clear();
        canteenShort.clear();
    }
}
