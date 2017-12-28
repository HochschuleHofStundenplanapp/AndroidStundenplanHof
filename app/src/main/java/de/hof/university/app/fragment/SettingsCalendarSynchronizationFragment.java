package de.hof.university.app.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.calendar.CalendarSynchronization;

/**
 * Created by Daniel on 22.11.2017.
 */

public class SettingsCalendarSynchronizationFragment extends PreferenceFragment {

	public final static String TAG = "SettingCalendarSyncFrag";

	private final int REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION =  2;
	private final int REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION =  3;

	private CalendarSynchronization calendarSynchronization = null;

	private SharedPreferences sharedPreferences;

	@Override
	public void onCreate( @Nullable Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		sharedPreferences = MainActivity.getSharedPreferences();

		this.calendarSynchronization = CalendarSynchronization.getInstance();

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_calendar_synchronization);

		// Calendar synchronization
		final CheckBoxPreference calendar_syncronization = (CheckBoxPreference) findPreference(getString( R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION));

		calendar_syncronization.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ( (Boolean) newValue ) {
					// an schalten
					turnCalendarSyncOn();
				} else {
					// aus schalten

					// mit einem Dialog nachfragen ob der Nutzer die Kalendereinträge behalten möchte
					final AlertDialog d = new AlertDialog.Builder(getView().getContext())
							.setTitle(R.string.calendar_syncronization_keep_events_title)
							.setMessage(R.string.calendar_syncronization_keep_events_message)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// behalten, mache nichts
								}
							})
							.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// löschen
									if (( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
											|| (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
										// keine Berechtigung, hole erst Berechtigung
										requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION);
									} else {
										// lösche die Kalendereinträge oder den lokalen Kalender
										calendarSynchronization.stopCalendarSynchronization();
									}
								}
							})
							.setCancelable(false)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.create();
					d.show();
				}
				return true;
			}
		});

		final Preference calendarReminderPref = findPreference(getString(R.string.PREF_KEY_CALENDAR_REMINDER));

		calendarReminderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {

				// Get the layout inflater
				LayoutInflater inflater = getActivity().getLayoutInflater();

				View view = inflater.inflate(R.layout.dialog_calendar_reminder, null);

				final EditText calendarReminderEditText = view.findViewById(R.id.calendarReminderEditText);

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
				dialogBuilder.setView(view)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String minutesString = calendarReminderEditText.getText().toString();

								int minutes;

								try {
									minutes = Integer.parseInt(minutesString);
								} catch (NumberFormatException e) {
									Log.e(TAG, "NumberFormatException", e);
									return;
								}

								sharedPreferences.edit()
										.putInt(getString(R.string.PREF_KEY_CALENDAR_REMINDER), minutes)
										.apply();

								updateSummary();
							}
						})
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// cancel the dialog
							}
						});

				dialogBuilder.create().show();

				// andere Methode mit TimerPickerDialog, aber dort geht nur bis 24 Stunden
				/*TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						Log.d(TAG, "TimerSet: Hours: " + hourOfDay + " Minutes: " + minute);
					}
				};

				TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT, onTimeSetListener, 0, 0, true);

				timePickerDialog.show();*/


				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		final MainActivity mainActivity = (MainActivity) getActivity();
		mainActivity.getSupportActionBar().setTitle(R.string.calendar_synchronization);

		final NavigationView navigationView = mainActivity.findViewById(R.id.nav_view);
		navigationView.getMenu().findItem(R.id.nav_einstellungen).setChecked(true);


		final CheckBoxPreference calendar_synchronization = (CheckBoxPreference) findPreference(getString( R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION));

		// aktiviere oder deaktiviere die Kalender Synchronisation je nachdem ob die experimentellen Funktionen aktiviert sind oder nicht
		boolean experimentalFeaturesEnabled = sharedPreferences.getBoolean(getString(R.string.PREF_KEY_EXPERIMENTAL_FEATURES_ENABLED), false);

		calendar_synchronization.setEnabled( experimentalFeaturesEnabled );

		// update summary
		updateSummary();
	}

	private void updateSummary() {
		final Preference calendarReminderPref = findPreference(getString(R.string.PREF_KEY_CALENDAR_REMINDER));
		int minutes = sharedPreferences.getInt(getString(R.string.PREF_KEY_CALENDAR_REMINDER), R.integer.CALENDAR_REMINDER_DEFAULT_VALUE);
		if (minutes == 1) {
			// Einzahl
			calendarReminderPref.setSummary("" + minutes + " " + getString(R.string.minute));
		} else {
			// Mehrzahl
			calendarReminderPref.setSummary("" + minutes + " " + getString(R.string.minutes));
		}
	}

	private void requestCalendarPermission( int requestCode) {

		// From MARSHMELLOW (OS 6) on
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			this.requestPermissions(
					new String[]{Manifest.permission.READ_CALENDAR,
							Manifest.permission.WRITE_CALENDAR},
					requestCode);
		}
	}

	@Override
	public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted
					turnCalendarSyncOn();
				} else {
					// Permission Denied
					Toast.makeText(getActivity(), R.string.calendar_synchronization_permissionNotGranted, Toast.LENGTH_SHORT)
							.show();
					// Calendar Sync aus schalten
					((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
				}
				break;
			case REQUEST_CODE_CALENDAR_TURN_OFF_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission granted
					calendarSynchronization.stopCalendarSynchronization();
				} else {
					// Permission Denied
					Toast.makeText(getActivity(), R.string.calendar_synchronization_permissionNotGranted, Toast.LENGTH_SHORT)
							.show();
					// Calendar Sync ein schalten
					((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(true);
				}
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private void turnCalendarSyncOn() {
		// check for permission
		// wenn keine Berechtigung dann requeste sie und falls erfolgreich komme hier her zurück
		if ((ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
				|| (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
			// keine Berechtigung
			requestCalendarPermission(REQUEST_CODE_CALENDAR_TURN_ON_PERMISSION);
			return;
		}

		final ArrayList<String> calendars = new ArrayList<>();

		// Den localen Kalender als erstes
		calendars.add(getString(R.string.calendar_synchronitation_ownLocalCalendar));

		// Die weiteren Kalender danach
		calendars.addAll(calendarSynchronization.getCalendarsNames());

		final AlertDialog d = new AlertDialog.Builder(getView().getContext())
				.setTitle(R.string.calendar_synchronization)
				.setMessage(R.string.calendar_synchronization_infoText)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new AlertDialog.Builder(getView().getContext())
								.setTitle(R.string.calendar_synchronization_chooseCalendar)
								.setItems(calendars.toArray(new String[calendars.size()]), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String calendarName = calendars.get(which);
										if (calendarName.equals(getString(R.string.calendar_synchronitation_ownLocalCalendar))) {
											// lokaler Kalender
											calendarSynchronization.setCalendar(null);
										} else {
											calendarSynchronization.setCalendar(calendarName);
										}
										calendarSynchronization.createAllEvents();
									}
								})
								.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Kalender Synchronisation ausschalten
										((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
									}
								})
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										// Kalender Synchronisation ausschalten
										((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
									}
								})
								.setIcon(android.R.drawable.ic_dialog_alert)
								.show();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// Kalender Synchronisation ausschalten
						((CheckBoxPreference) findPreference(getString(R.string.PREF_KEY_CALENDAR_SYNCHRONIZATION))).setChecked(false);
					}
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.create();
		d.show();

		// Make the textview clickable. Must be called after show()
		((TextView)d.findViewById(android.R.id.message)).setMovementMethod( LinkMovementMethod.getInstance());
	}


}
