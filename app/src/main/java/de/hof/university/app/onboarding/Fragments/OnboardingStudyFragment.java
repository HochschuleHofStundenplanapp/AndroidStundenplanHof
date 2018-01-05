package de.hof.university.app.onboarding.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.fragment.SettingsFragment;
import de.hof.university.app.model.settings.StudyCourse;

/**
 * Created by patrickniepel on 03.01.18.
 */

public class OnboardingStudyFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Button studyTermBtn, degreeProgramBtn, semesterBtn, continueBtn;

    private ProgressDialog progressDialog;
    private List<StudyCourse> studyCourseList;

    //ArrayAdapter for dialogs
    private ArrayList<String> termList, degreeProgramList, semesterList;
    private String selectedTerm, selectedDegreeProgram, selectedSemester;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_study, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLayout();
        setupClickListener();

        termList = new ArrayList<>();
        degreeProgramList = new ArrayList<>();
        semesterList = new ArrayList<>();

        selectedTerm = selectedDegreeProgram = selectedSemester = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(R.string.onboarding_study);
    }

    private void setupLayout() {
        studyTermBtn = getActivity().findViewById(R.id.onboarding_study_study_term_button);
        degreeProgramBtn = getActivity().findViewById(R.id.onboarding_study_degree_program_button);
        semesterBtn = getActivity().findViewById(R.id.onboarding_study_semester_button);
        continueBtn = getActivity().findViewById(R.id.onboarding_study_continue_button);

        degreeProgramBtn.setEnabled(false);
        semesterBtn.setEnabled(false);
    }

    private void setupClickListener() {

        studyTermBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                termList.add("WS");
                termList.add("SS");
                //termList.addAll(new ArrayList<String>(R.array.term_time_values));
                createDialog("term");
            }
        });

        degreeProgramBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final OnboardingStudyFragment.GetSemesterTask getSemesterTask = new OnboardingStudyFragment.GetSemesterTask();

                final String[] params = new String[ 2 ];
                params[ 0 ] = selectedSemester;
                params[ 1 ] = String.valueOf(false);
                getSemesterTask.execute(params);
            }
        });

        semesterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog("term");
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Everything must be selected to continue
                if (selectedTerm.isEmpty() || selectedDegreeProgram.isEmpty() || selectedSemester.isEmpty()) {
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
                    startOnboardingMenuPlan();
                }

            }
        });
    }

    private void startOnboardingMenuPlan() {

        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.addToBackStack(OnboardingMenuPlanFragment.class.getName());
        trans.replace(android.R.id.content, new OnboardingMenuPlanFragment());
        trans.commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    private class GetSemesterTask extends AsyncTask<String, Void, Void> {

        ArrayList<String> entries = null;
        ArrayList<String> entryValues = null;

        @Override
        protected final void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setCancelable(true);
            progressDialog.setMessage(getString(R.string.onclick_refresh));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(false);
            progressDialog.show();

        }

        @Override
        protected final Void doInBackground(String... params) {
            final String termTime = params[ 0 ];
            final boolean pForceRefresh = Boolean.valueOf(params[ 1 ]);

            studyCourseList = DataManager.getInstance().getCourses(getActivity().getApplicationContext(),
                    getString(R.string.language), termTime, pForceRefresh);

            if (studyCourseList != null) {
                entries = new ArrayList<>();
                entryValues = new ArrayList<>();

                StudyCourse studyCourse;
                for (int i = 0; i < studyCourseList.size(); ++i) {
                    if (studyCourseList.get(i) instanceof StudyCourse) {
                        studyCourse = studyCourseList.get(i);
                        entries.add(studyCourse.getName());
                        entryValues.add(studyCourse.getTag());
                    }
                }
            }
            return null;
        }

        @Override
        protected final void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (entries != null) {
                if (entries.size() > 0) {
                    //lpCourse.setEntries(entries);
                    //lpCourse.setEntryValues(entryValues);
                    //lpCourse.setEnabled(true);
                    degreeProgramBtn.setEnabled(true);
                    degreeProgramList.addAll(entryValues);
                    createDialog("degreeProgram");
                    Log.v("OLALALLLLLALALALAL", "asdddasdaskdashdaskdghasökdjfaskjdfhaöskdfhaskdjfhasödkhfaöksjdhfaöskdhföaskdjhföasjkdhfökjhföasdfsd");
                }
            }

            progressDialog.dismiss();
        }
    }

    /**
     * Öffnet Prozessdialog und aktualisiert die Semester zu dem zuvor ausgewählten Studiengang
     *
     *
     */
    private void updateSemesterData() {

        if ( (studyCourseList == null) || selectedDegreeProgram.isEmpty() ) {
            //Leave list empty
            return;
        }

        for ( final StudyCourse studyCourse : studyCourseList ) {
            if ( studyCourse.getTag().equals(selectedDegreeProgram) ) {
                //final CharSequence[] entries = new CharSequence[ studyCourse.getTerms().size() ];
                final ArrayList<String> entryValues = new ArrayList<>();

                for ( int j = 0; j < studyCourse.getTerms().size(); ++j ) {
                    //entries[ j ] = studyCourse.getTerms().get(j);
                    //entryValues[ j ] = studyCourse.getTerms().get(j);
                    entryValues.add(studyCourse.getTerms().get(j));
                }

                if ( semesterList != null ) {
                    if ( semesterList.size() > 0 ) {
                        //lpSemester.setEntries(entries);
                        //lpSemester.setEntryValues(entryValues);
                        //lpSemester.setEnabled(true);
                        semesterBtn.setEnabled(true);
                        semesterList.addAll(entryValues);
                    } else {
                        semesterBtn.setEnabled(false);
                        //lpSemester.setEnabled(false);
                    }
                }
            }
        }
    }

    private void createDialog(final String valueKey) {

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        final ArrayAdapter<String> valueAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_singlechoice);

        if(valueKey.equals("term")) {
            valueAdapter.addAll(termList);
        }
        if(valueKey.equals("degreeProgram")) {

        }
        if(valueKey.equals("semester")) {
            valueAdapter.addAll(semesterList);
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

                if(valueKey.equals("term")) {
                    selectedTerm = valueAdapter.getItem(which);
                    degreeProgramBtn.setEnabled(true);
                }
                if(valueKey.equals("degreeProgram")) {
                    selectedDegreeProgram = valueAdapter.getItem(which);
                    updateSemesterData();
                }
                if(valueKey.equals("semester")) {
                    selectedSemester = valueAdapter.getItem(which);
                }


                String strName = valueAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }
}
