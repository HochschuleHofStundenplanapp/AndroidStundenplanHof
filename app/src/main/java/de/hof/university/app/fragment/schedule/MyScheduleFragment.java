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

package de.hof.university.app.fragment.schedule;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.DriveStatusCodes;
import com.google.android.gms.drive.Metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.GDrive.GoogleDriveController;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.util.Define;
import androidx.core.content.ContextCompat;

/**
 * Created by Lukas on 22.06.2016.
 */
public class MyScheduleFragment extends ScheduleFragment {

	public final static String TAG = "MyScheduleFragment";
	private GoogleDriveController gDriveCtrl;


	@Override
	public final void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		if (v.getId() == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

			Object item = listView.getItemAtPosition(info.position);

			if (item instanceof LectureItem) {
				final LectureItem lectureItem = (LectureItem) item;

				final DataManager dm = DataManager.getInstance();

				//Wenn in Mein Stundenplan enthalten -> löschen anzeigen
				if (dm.myScheduleContains(v.getContext(), lectureItem)) {
					menu.setHeaderTitle(R.string.myschedule);
					menu.add(Menu.NONE, 0, 0, R.string.deleteFromMySchedule);
				}
			}
		}
	}


	@Override
	public final void onResume() {
		super.onResume();
		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(Html.fromHtml("<font color='"+ ContextCompat.getColor(MainActivity.getAppContext(), R.color.colorBlack)+"'>"
				+ getString(R.string.myschedule)+"</font>"));
		mainActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_accent_24dp);

		final NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_mySchedule).setChecked(true);

		if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 0) {
			Toast.makeText(getView().getContext(), getString(R.string.myScheduleInfo), Toast.LENGTH_LONG).show();
		}

		// fragen, ob die Push Notifications aktiviert werden sollen
		final MainActivity mActivity = (MainActivity) getActivity();
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
		mActivity.showPushNotificationDialog(sharedPreferences);
	}

	@Override
	public void onPause() {
		super.onPause();
		MainActivity mainActivity = (MainActivity) getActivity();
		NavigationView navigationView = (NavigationView) mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_mySchedule).setChecked(false);
	}

	@Override
	public final boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final LectureItem lectureItem = (LectureItem) listView.getItemAtPosition(info.position);

		if (item.getTitle().equals(getString(R.string.deleteFromMySchedule))) {
			DataManager.getInstance().deleteFromMySchedule(info.targetView.getContext().getApplicationContext(), lectureItem);
			dataList.remove(lectureItem);
			adapter.notifyDataSetChanged();
			Toast.makeText(getView().getContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
			// Stundenplan auf GDrive syncen
			DataManager.getInstance().updateGDrive();
			if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) == 0) {
				Toast.makeText(getView().getContext(), getString(R.string.changesScheduleText), Toast.LENGTH_LONG).show();
			}
		}
		return true;
	}


	/**
	 * @param menu
	 * @param inflater
	 */
	@Override
	public final void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.myschedule_main, menu);
		boolean driveSyncOn = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("drive_sync", false);
		menu.findItem(R.id.action_refresh_from_gdrive).setVisible(driveSyncOn);

	}

	/**
	 * @param item
	 * @return boolean Return false to allow normal menu processing to
	 * proceed, true to consume it here.
	 */
	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {

		//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		// handle item selection
		//we consume this event here and proceed normal
		switch (item.getItemId()) {
			case R.id.action_delete_all:
				DataManager.getInstance().deleteAllFromMySchedule(getActivity().getApplicationContext());
				DataManager.getInstance().updateGDrive();
				dataList.clear();
				adapter.notifyDataSetChanged();
				Toast.makeText(getView().getContext(), getString(R.string.changesScheduleText), Toast.LENGTH_LONG).show();
				break;
			case R.id.action_refresh_from_gdrive:
				refreshFromGDriveIfNeeded();
				break;
		}


		return super.onOptionsItemSelected(item);
	}

	private void refreshFromGDriveIfNeeded() {
		gDriveCtrl = GoogleDriveController.getInstance(getActivity());
		if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("drive_sync", false)) {
			return;
		}
		Log.i(TAG, "Attempt to refresh");
		gDriveCtrl.getAppFolderFileList(metadataBuffer ->
		{
			Metadata metadata = gDriveCtrl.getMetadataNamed(getString(R.string.myschedule), metadataBuffer);
			long savedMyScheduleDate = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(getString(R.string.PREF_KEY_MYSCHEDULE_DATE), new Date().getTime());
			long metadataDate = Long.parseLong(metadata.getDescription());

			gDriveCtrl.getDriveClient().requestSync().addOnSuccessListener(aVoid -> {
						if (savedMyScheduleDate < metadataDate) {
							sync();
						}
					}
			).addOnFailureListener(e -> {
						ApiException apiExcepition = (ApiException) e;
						if (DriveStatusCodes.DRIVE_RATE_LIMIT_EXCEEDED == apiExcepition.getStatusCode()) {
							Log.i(TAG, "Drive Limit exceeeded, performing sync");
							if (savedMyScheduleDate < metadataDate) {
								sync();
							}
						}
					}
			);


		});
	}

	//Synchronisiert den Stundenplan auf dem Gerät mit dem Stundenplan auf Drive
	private void sync() {
		//Lädt den Stundenplan von Drive und speichert ihn im DataManager als den neuen Stundenplan
		//Danach wird die Closure ausgeführt
		gDriveCtrl.loadMyScheduleFromDrive((lectures) -> {
			Snackbar.make(getView(), "It seems that there is a newer Schedule on your Drive", Snackbar.LENGTH_LONG).show();

//            List<LectureItem> lectures = DataManager.getInstance().getMySchedule(getContext(), getString(R.string.language), false);
			if(BuildConfig.DEBUG) {
				for (LectureItem lecture : lectures.getLectures()) {
					Log.i(TAG, lecture.getLabel());
				}
			}

			//TODO: aktualisiert die ListView noch nicht
			getActivity().runOnUiThread(() -> {
				if (lectures != null) {
					//super.updateListView(lectures);
					//adapter.notifyDataSetChanged();
					MyScheduleFragment frag = (MyScheduleFragment)getFragmentManager().findFragmentByTag(Define.myScheduleFragmentName);
					frag.updateListView(lectures.getLectures());
					frag.modifyListViewAfterDataSetChanged();
					frag.listView.invalidateViews();
					frag.adapter.notifyDataSetChanged();

					//listView.invalidateViews();
					//adapter.notifyDataSetChanged();
					//modifyListViewAfterDataSetChanged();
				}
			});
			gDriveCtrl.loadSharedPreferences();

		});


	}

	/**
	 * gibt das Datum zurück wann der Mein Stundenplan zuletzt geholt wurde
	 *
	 * @return lastSaved
	 */
	@Override
	public String getLastSaved() {
		return DataManager.getInstance().formatDate(DataManager.getInstance().getMyScheduleLastSaved());
	}


	@Override
	protected ArrayList<Object> background(final String[] params) {

		if (DataManager.getInstance().getMyScheduleSize(getActivity().getApplicationContext()) > 0) {
            /*
            final String course = params[0];
            final String semester = params[1];
            final String termTime = params[2];
            */
			final String sForceRefresh = params[3];

			boolean bForceRefresh = false;
			try {
				bForceRefresh = Boolean.valueOf(sForceRefresh);
			} catch (NumberFormatException e) {
				Log.e(TAG, "background, wrong boolean:" + sForceRefresh, e);
			}

			List<LectureItem> scheduleList = DataManager.getInstance().getMySchedule(getActivity().getApplicationContext(), getString(R.string.language), bForceRefresh);

			// wenn etwas zurück kommt, dann aktualisiere die View
			if (scheduleList != null) {
				return super.updateListView(scheduleList);
			}
		}
		// wenn nichts zurück kommt returne null, damit ein Toast angezeigt wird
		return null;
	}
}
