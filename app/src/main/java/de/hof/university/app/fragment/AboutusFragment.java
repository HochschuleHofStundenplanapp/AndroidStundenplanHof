/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

package de.hof.university.app.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.util.Define;

/**
 * Created by larsg on 28.06.2016.
 */
public class AboutusFragment extends Fragment {

	private final static String TAG = "AboutusFragment";

	@Override
	public final void onResume() {
		super.onResume();
		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.aboutus);

		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_aboutus).setChecked(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		final MainActivity mainActivity = (MainActivity) getActivity();
		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);

		navigationView.getMenu().findItem(R.id.nav_aboutus).setChecked(false);
	}

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
	                               final Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View v = inflater.inflate(R.layout.fragment_aboutus, container, false);

		final Button btnRate = (Button) v.findViewById(R.id.btnRate);
		btnRate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Define.PLAYSTOREURL));
				if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivity(browserIntent);
				} else {
					Toast.makeText(getActivity().getApplicationContext(), R.string.noBrowserApp, Toast.LENGTH_SHORT).show();
				}
			}
		});

		final Button btnFeedback = (Button) v.findViewById(R.id.btnFeedback);
		btnFeedback.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
				mailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
				mailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {Define.FEEDBACKEMAILADDRESS});
				mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackSubject));
                if (mailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mailIntent);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.noEmailApp, Toast.LENGTH_SHORT).show();
                }
			}
		});

		return v;
	}
}
