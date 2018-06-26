package de.hof.university.app.onboarding.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.hof.university.app.R;

public class OnboardingWelcomeFragment extends Fragment {

    private Button startButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_welcome, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLayout();
    }

    private void setupLayout() {
        startButton = getActivity().findViewById(R.id.onboarding_welcome_start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction trans = manager.beginTransaction();
                trans.addToBackStack(OnboardingStudyFragment.class.getName());
                trans.replace(R.id.content_main, new OnboardingStudyFragment());
                trans.commit();
            }
        });
    }
}
