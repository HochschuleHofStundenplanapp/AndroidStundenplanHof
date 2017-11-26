/*
 * Copyright (c) 2017 Hochschule Hof, Daniel Glaser
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


package de.hof.university.app.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureChange;
import de.hof.university.app.model.schedule.LectureItem;

/*
	reading from https://developer.android.com/reference/android/provider/CalendarContract.EventsColumns.html

ACCESS_CONFIDENTIAL	Confidential is not used by the app.
ACCESS_DEFAULT	Default access is controlled by the server and will be treated as public on the device.
ACCESS_LEVEL	Defines how the event shows up for others when the calendar is shared.
ACCESS_PRIVATE	Private shares the event as a free/busy slot with no details.
ACCESS_PUBLIC	Public makes the contents visible to anyone with access to the calendar.
ALL_DAY	Is the event all day (time zone independent).
AVAILABILITY	If this event counts as busy time or is still free time that can be scheduled over.
AVAILABILITY_BUSY	Indicates that this event takes up time and will conflict with other events.
AVAILABILITY_FREE	Indicates that this event is free time and will not conflict with other events.
AVAILABILITY_TENTATIVE	Indicates that the owner's availability may change, but should be considered busy time that will conflict.
CALENDAR_ID	The _ID of the calendar the event belongs to.
CAN_INVITE_OTHERS	Whether the user can invite others to the event.
CUSTOM_APP_PACKAGE	The package name of the custom app that can provide a richer experience for the event.
CUSTOM_APP_URI	The URI used by the custom app for the event.
DESCRIPTION	The description of the event.
DISPLAY_COLOR	This will be EVENT_COLOR if it is not null; otherwise, this will be CALENDAR_COLOR.
DTEND	The time the event ends in UTC millis since epoch.
DTSTART	The time the event starts in UTC millis since epoch.
DURATION	The duration of the event in RFC2445 format.
EVENT_COLOR	A secondary color for the individual event.
EVENT_COLOR_KEY	A secondary color key for the individual event.
EVENT_END_TIMEZONE	The timezone for the end time of the event.
EVENT_LOCATION	Where the event takes place.
EVENT_TIMEZONE	The timezone for the event.
EXDATE	The recurrence exception dates for the event.
EXRULE	The recurrence exception rule for the event.
GUESTS_CAN_INVITE_OTHERS	Whether guests can invite other guests.
GUESTS_CAN_MODIFY	Whether guests can modify the event.
GUESTS_CAN_SEE_GUESTS	Whether guests can see the list of attendees.
HAS_ALARM	Whether the event has an alarm or not.
HAS_ATTENDEE_DATA	Whether the event has attendee information.
HAS_EXTENDED_PROPERTIES	Whether the event has extended properties or not.
IS_ORGANIZER	Are we the organizer of this event.
LAST_DATE	The last date this event repeats on, or NULL if it never ends.
LAST_SYNCED	Used to indicate that a row is not a real event but an original copy of a locally modified event.
ORGANIZER	Email of the organizer (owner) of the event.
ORIGINAL_ALL_DAY	The allDay status (true or false) of the original recurring event for which this event is an exception.
ORIGINAL_ID	The _ID of the original recurring event for which this event is an exception.
ORIGINAL_INSTANCE_TIME	The original instance time of the recurring event for which this event is an exception.
ORIGINAL_SYNC_ID	The _sync_id of the original recurring event for which this event is an exception.
RDATE	The recurrence dates for the event.
RRULE	The recurrence rule for the event.
SELF_ATTENDEE_STATUS	This is a copy of the attendee status for the owner of this event.
STATUS	The event status.
STATUS_CANCELED
STATUS_CONFIRMED
STATUS_TENTATIVE
SYNC_DATA1	This column is available for use by sync adapters.
SYNC_DATA10	This column is available for use by sync adapters.
SYNC_DATA2	This column is available for use by sync adapters.
SYNC_DATA3	This column is available for use by sync adapters.
SYNC_DATA4	This column is available for use by sync adapters.
SYNC_DATA5	This column is available for use by sync adapters.
SYNC_DATA6	This column is available for use by sync adapters.
SYNC_DATA7	This column is available for use by sync adapters.
SYNC_DATA8	This column is available for use by sync adapters.
SYNC_DATA9	This column is available for use by sync adapters.
TITLE	The title of the event.
UID_2445	The UID for events added from the RFC 2445 iCalendar format.

 */


class CalendarInterface {
	private static final String TAG = "CalendarInterface";
	private static final boolean DEBUG_CALENDAR_INTERFACE = true;

	private static final CalendarInterface instance = new CalendarInterface();

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	private static final String[] CALENDAR_PROJECTION = new String[]{
			Calendars._ID,                          // 0
			Calendars.CALENDAR_DISPLAY_NAME,        // 1
	};

	// The indices for the projection array above.
	private static final int PROJECTION_CALENDAR_ID_INDEX = 0;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;

	private static final String[] EVENT_PROJECTION_EVENT_ID_TITLE_DESCRIPTION = new String[]{
			CalendarContract.Instances.EVENT_ID,         // 0
			CalendarContract.Instances.TITLE,            // 1
			CalendarContract.Instances.DESCRIPTION,      // 2
			CalendarContract.Instances.ORIGINAL_SYNC_ID, // 3
	};

	// The indices for the projection array above.
	private static final int PROJECTION_EVENT_ID = 0;
	private static final int PROJECTION_TITLE_INDEX = 1;
	private static final int PROJECTION_DESCRIPTION_INDEX = 2;
	private static final int PROJECTION_ORIGINAL_SYNC_ID_INDEX = 3;

	private static final String[] EVENT_PROJECTION_DATES = new String[]{
			CalendarContract.Instances.BEGIN,        // 0
			CalendarContract.Instances.END,          // 1
	};

	// The indices for the projection array above.
	private static final int PROJECTION_EVENT_BEGIN = 0;
	private static final int PROJECTION_EVENT_END = 1;
	public static final int HOF_CALENDAR_COLOR = 0x5c8f52;

	private Date eventStartDate;
	private Date eventEndDate;

	private String localCalendarName = "";
	private String accountName = ""; // Google Account
	private String accountType = ""; // Typ z.B.: com.google
	private CalendarData calendarData = new CalendarData();

	public static CalendarInterface getInstance() {
		return instance;
	}

	/**
	 * Constructor for the default local calendar
	 */
	private CalendarInterface() {
		super();
		Log.i(TAG, "Constructor");
		Context context = MainActivity.getAppContext();
		if (context == null) {
			Log.e(TAG, "Context null, make nothing");
			return;
		}

		Context applicationContext = context.getApplicationContext();
		localCalendarName = applicationContext.getString(R.string.stundenplan) + " " + applicationContext.getString(R.string.app_name);
		accountName = applicationContext.getString(R.string.app_name);
		accountType = CalendarContract.ACCOUNT_TYPE_LOCAL;

		// bereits vohandene IDs einlesen
		readCalendarData();

		if (calendarData.getCalendarID() == null) {
			// TODO CalendarID leer
			if (DEBUG_CALENDAR_INTERFACE) Log.d(TAG, "CalendarID is empty in constructor");
		}
	}

	public void setCalendar(Long calendarID) {
		Context context = MainActivity.getAppContext().getApplicationContext();
		if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
				&& (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)) {
			// Wenn -1 dann lokalen Calendar, sonst die übergebene ID nutzen
			if (calendarID == null) {
				if (getLocalCalendar() == null) {
					createLocalCalendar();
					calendarData.setCalendarID(getLocalCalendar());
				}
			} else {
				// use existing calendar
				calendarData.setCalendarID(calendarID);
			}
		}
	}

	// Alle Kalender des Geräts werden eingelesen
	// Google-Kalender, Exchange-Kalender, lokaler Kalender
	final HashMap<String, Long> getCalendars() {
		Context context = MainActivity.getAppContext().getApplicationContext();

		HashMap<String, Long> result = new HashMap<>();
		// Run query
		Cursor cur;
		ContentResolver cr = context.getContentResolver();
		Uri uri = Calendars.CONTENT_URI;

		// Submit the query and get a Cursor object back.
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			return result;
		}

		// Anfrage für alle Kalender
		cur = cr.query(uri,
				CALENDAR_PROJECTION,
				null,           // alle Kalender
				null,
				null);

		if (cur == null) {
			return result;
		}

		// Use the cursor to step through the returned records
		while (cur.moveToNext()) {
			// found
			// put the Name and the ID of the calendar in result
			final String sDisplayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
			final long iIndex = cur.getLong(PROJECTION_CALENDAR_ID_INDEX);
			result.put( sDisplayName, iIndex );

			Log.v( TAG, "Kalender: " + sDisplayName + " Idx:"+iIndex);
		}

		cur.close();
		return result;
	}

	/**
	 * Search after the local calendar an return the calID if the calendar was found or null if not
	 *
	 * @return return if the calendar ID if it was found, else null
	 */
	private Long getLocalCalendar() {
		Context context = MainActivity.getAppContext().getApplicationContext();

		Long result;

		// Run query
		Cursor cur;
		ContentResolver cr = context.getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "(("
				+ Calendars.NAME + " = ?) AND ("
				+ Calendars.ACCOUNT_NAME + " = ?) AND ("
				+ Calendars.OWNER_ACCOUNT + " = ?) AND ("
				+ Calendars.ACCOUNT_TYPE + " = ?)"
				+ ")";
		String[] selectionArgs = new String[]{localCalendarName, accountName, accountName, accountType};

		// Submit the query and get a Cursor object back.
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			return null;
		}

		// Anfrage
		cur = cr.query(uri,
				CALENDAR_PROJECTION,
				selection,
				selectionArgs,
				null);

		if (cur == null) {
			return null;
		}

		// Use the cursor to step through the returned records
		if (cur.moveToNext()) {
			// found
			Log.i(TAG, "getLocalCalendar Name: " + cur.getString(PROJECTION_DISPLAY_NAME_INDEX));

			// put the calendar ID in the result
			result = cur.getLong(PROJECTION_CALENDAR_ID_INDEX);

			cur.close();
			return result;
		}

		cur.close();
		return null;
	}

	/**
	 * creates the local calendar
	 */
	private void createLocalCalendar() {
		Context context = MainActivity.getAppContext().getApplicationContext();

		Uri calendarUri = Uri.parse(Calendars.CONTENT_URI.toString());
		calendarUri = calendarUri.buildUpon()
				.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, accountName)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();

		ContentValues values = new ContentValues();
		values.put(Calendars.OWNER_ACCOUNT, accountName);
		values.put(Calendars.ACCOUNT_NAME, accountName);
		values.put(Calendars.ACCOUNT_TYPE, accountType);
		values.put(Calendars.NAME, localCalendarName);
		values.put(Calendars.CALENDAR_DISPLAY_NAME, localCalendarName);
		values.put(Calendars.CALENDAR_COLOR, HOF_CALENDAR_COLOR);                                                 // COLOR
		values.put(Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
		values.put(Calendars.VISIBLE, 1);
		values.put(Calendars.SYNC_EVENTS, 1);
		// for reminders, seems not necessary
		//values.put(Calendars.ALLOWED_REMINDERS, "" + CalendarContract.Reminders.METHOD_DEFAULT + "," + CalendarContract.Reminders.METHOD_ALERT);

		//TimeZone tz = TimeZone.getDefault();
		//values.put(Calendars.CALENDAR_TIME_ZONE, tz.getID());
		// Deutsche Timezone da Hochschule in Deutschland
		values.put(Calendars.CALENDAR_TIME_ZONE, "Europe/Brussels");

		values.put(Calendars.CAN_PARTIALLY_UPDATE, 1);
	    /*values.put(Calendars.CAL_SYNC1, "https://www.google.com/calendar/feeds/" + accountName + "/private/full");
        values.put(Calendars.CAL_SYNC2, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName);
        values.put(Calendars.CAL_SYNC3, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName);
        values.put(Calendars.CAL_SYNC4, 1);
        values.put(Calendars.CAL_SYNC5, 0);
        values.put(Calendars.CAL_SYNC8, System.currentTimeMillis());*/

		context.getContentResolver().insert(calendarUri, values);
	}

	/**
	 * removes the local calendar
	 *
	 * @return returns if the removing was successful
	 */
	boolean removeLocalCalendar() {
		Context context = MainActivity.getAppContext().getApplicationContext();

		Long localCalendarID = getLocalCalendar();

		if (localCalendarID == null) {
			return false;
		}

		Uri.Builder builder = Calendars.CONTENT_URI.buildUpon();
		Uri calendarToRemoveUri = builder.appendPath(localCalendarID.toString())
				.appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, accountName)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
				.build();

		context.getContentResolver().delete(calendarToRemoveUri, null, null);

		//Alle IDs löschen
		removeAllLectruesEventIDs();
		saveCalendarData();
		return true;
	}

	/**
	 * creates the event for a lecture, and checks if it is already existing
	 * Kalendereintrag = Event
	 *
	 * @param lectureID     SPlusName
	 * @param title         Vorlesungstitel
	 * @param description   Beschreibung - normalerweise leer
	 * @param startTime     Beginn der Vorlesung (Datum und Uhrzeit)
	 * @param endTime       Ende der Vorlesung  (Datum und Uhrzeit)
	 * @param location      Ort/Raum
	 */
	void createLectureEvent(final String lectureID, final String title, final String description,
	                        final Date startTime, final Date endTime, final String location) {

		if (DEBUG_CALENDAR_INTERFACE) Log.d( TAG, "createLectureEvent in CalendarInterface" );

		junit.framework.Assert.assertTrue( !"".equals(lectureID) );
		junit.framework.Assert.assertTrue( !"".equals(title) );
//TODO		Assert.assertTrue( !"".equals(description) );
		junit.framework.Assert.assertTrue( startTime != null );
		junit.framework.Assert.assertTrue( endTime != null );
		junit.framework.Assert.assertTrue( !"".equals(location) );

		// checks if exists, if true than update event otherwise create event
		ArrayList<Long> eventIDs = getEventIDs(lectureID, title, startTime, endTime );
		if (eventIDs != null) {
			if (DEBUG_CALENDAR_INTERFACE) Log.d( TAG, "Event für Lecture " + title + " am " + startTime + " bereits gefunden!" );
			for (Long eventID : eventIDs) {
				if (DEBUG_CALENDAR_INTERFACE) Log.d( TAG, "Event wird geupdatet" );
				updateEvent(eventID, title, null, startTime, endTime, location);
			}
		} else {
			if (DEBUG_CALENDAR_INTERFACE) Log.d( TAG, "Event für Lecture " + title + " am " + startTime + " wird neu angelegt." );
			final Long newEventID = createEvent(title, description, startTime, endTime, location, lectureID);

			// Wenn null dann keine Berechtigung oder keine CalendarID und returnen
			if (newEventID == null) {
				return;
			}
			//Liste ist noch leer
			eventIDs = new ArrayList<>();
			eventIDs.add(newEventID);
		}

		// zu den IDs hinzufügen
		for (Long eventID : eventIDs) {
			addLecturesEventID(lectureID, eventID);
		}
	}

	private void createChangeEvent(final String lectureID, final String title, final String description,
	                               final Date startTime, final Date endTime, final String location) {

		final Long eventID = createEvent(title, description, startTime, endTime, location, lectureID);

		// Wenn null dann keine Berechtigung oder keine CalendarID und returnen
		if (eventID == null)
			return;

		// zu den IDs hinzufügen
		addChangesEventID(lectureID, eventID);
	}

	/**
	 * creates a event
	 *
	 * @param title       the title of the event
	 * @param description the description of the event
	 * @param startDate   the start Date of the event
	 * @param endDate     the end Date of the event
	 * @param location    the location of the event
	 * @return eventID or null if no permission
	 */
	private Long createEvent(final String title, final String description, final Date startDate,
	                         final Date endDate, final String location, final String lectureID) {
		//TODO lectureID not used

		if (calendarData.getCalendarID() == null) {
			return null;
		}

		junit.framework.Assert.assertTrue( !"".equals(title) );
//TODO		Assert.assertTrue( !"".equals(description)) ;
		junit.framework.Assert.assertTrue( startDate != null ) ;  // > 2016 && < 2030
		junit.framework.Assert.assertTrue( endDate != null ) ;
		junit.framework.Assert.assertTrue( !"".equals( location )) ;

		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, startDate.getTime());
		values.put(Events.DTEND, endDate.getTime());
		values.put(Events.TITLE, title);
		values.put(Events.HAS_ALARM, true);
		values.put(Events.EVENT_LOCATION, location);
		values.put(Events.CALENDAR_ID, calendarData.getCalendarID());

		values.put(Events.DESCRIPTION, description);

		// put the ID of the lecutre in the ORIGNAL_SYNC_ID
		// ORIGNAL_SYNC_ID might be used for something else, but we use it for ower own ID
		// currently ower ID is the splusname
		// ORIGINAL_SYNC_ID
		values.put(Events.ORIGINAL_SYNC_ID, lectureID);

		//IDs are from "public static final class Events"
		//TODO
		//EVENT_COLOR
		values.put(Events.EVENT_COLOR, HOF_CALENDAR_COLOR);
		//ORIGINAL_ID
		//UID_2445  ???

		final TimeZone tz = TimeZone.getDefault();
		values.put(Events.EVENT_TIMEZONE, tz.getID());
		// Deutsche Timezone macht keinen Unterschied
		//values.put(Events.EVENT_TIMEZONE, "Europe/Brussels");

		// die EventID kommt zurück
		final Long eventID = insertEvent(values);

		// if to want to add personal reminders
		//TODO in configuration: Let the user decide
		//TODO how many minutes before
        /*if (value) {
            addReminderToEvent(eventID, 15);
            addReminderToEvent(eventID, 60);
        }*/

		return eventID;
	}

	//TODO was für ein Long geben wir hier zurück?
	private Long insertEvent(ContentValues values) {
		final Context context = MainActivity.getAppContext().getApplicationContext();

		final ContentResolver cr = context.getContentResolver();
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			return null;
		}

		final Uri uri = cr.insert(asSyncAdapter(Events.CONTENT_URI, accountName, accountType), values);

		if (uri == null) {
			return null;
		}

		// get the event ID that is the last element in the Uri
		final String lastPathSegment = uri.getLastPathSegment();
		Long anID = 0L;
		try {
			anID = Long.parseLong(lastPathSegment);
		} catch ( final NumberFormatException e )
		{ Log.e( TAG, "getLastPathSegment ", e); }
		return anID;
	}

	private void addReminderToEvent(Long eventID, int minutes) {
		final Context context = MainActivity.getAppContext().getApplicationContext();

		final ContentResolver cr = context.getContentResolver();
		final ContentValues values = new ContentValues();
		values.put(CalendarContract.Reminders.EVENT_ID, eventID);
		values.put(CalendarContract.Reminders.MINUTES, minutes); // METHOD_DEFAULT ist not working
		values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT);   // or METHOD_ALERT

		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		final Uri uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
	}

	/**
	 * updates a lecture with a change
	 * @param change a change with should update the correct lecture
	 */
	void updateChange(LectureChange change) {
		Log.i( TAG, "updateChange: " + change );
		Context context = MainActivity.getAppContext().getApplicationContext();

		String lectureID;

		lectureID = change.getId().substring(0, change.getId().indexOf(Define.CHANGES_SUBSTRING));

		ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);

		// wenn leer dann zurück
		if (eventIDs == null) return;

		for (Long eventID : eventIDs) {
			// TODO vielleicht endDatum ändern
			if (doEventExits(eventID, change.getLabel(), change.getBegin_old(), change.getBegin_old())) {

				if (change.getBegin_new() == null) {
					// Entfällt
					instance.updateEvent(eventID, context.getString(R.string.changeCancelled) + " " + change.getLabel(), null, null, null, "");
				} else {
					// Verschoben oder Raumänderung
					if (change.getBegin_new().equals(change.getBegin_old())) {
						// Raumänderung
						instance.updateEvent(eventID, context.getString(R.string.changeRoomchange) + " " + change.getLabel(), null, null, null, getLocation(change.getRoom_new()));
					} else {
						// Verschoben
						instance.updateEvent(eventID, context.getString(R.string.changeMoved) + " " + change.getLabel(), context.getString(R.string.changeNewDate) + ": " + DataManager.getInstance().formatDate(change.getBegin_new()), null, null, "");
						// TODO richtiges EndDate bekommen, weil 90 Minuten Länge nehmen
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(change.getBegin_new());
						calendar.add(Calendar.MINUTE, 90);
						Date newEndDate = calendar.getTime();
						// Alternativ Vorlesung erzeugen
						createChangeEvent(lectureID, context.getString(R.string.changeNew) + " " + change.getLabel(), "", change.getBegin_new(), newEndDate, getLocation(change.getRoom_new()));
					}
				}
				break;
			}
		}
	}

	/**
	 * updates a lecture
	 * @param lecture a lecture with should get updated
	 */
	void updateLecture(LectureItem lecture) {
		//never used: Context context = MainActivity.getAppContext().getApplicationContext();

		final String lectureID = lecture.getId();

		ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);

		if (eventIDs == null) {
			// Bedeutet: Noch nicht vorhanden
			// Nichts machen da die Events erst noch in einem anderen Thread erzeugt werden
			return;
		}

		for (Long eventID : eventIDs) {
			if ( doEventExits( eventID, lecture.getLabel(), lecture.getStartDate(), lecture.getEndDate() ) ) {
				// null because than the date won't get updated
				Date newStartDate = null;
				Date newEndDate = null;

				getEventDates(eventID, lecture.getStartDate(), lecture.getEndDate());

				if ((eventStartDate != null) && (eventEndDate != null)) {
					Calendar eventCalendar = new GregorianCalendar();
					eventCalendar.setTime(eventStartDate);

					Calendar lectureCalendar = new GregorianCalendar();
					lectureCalendar.setTime(lecture.getStartDate());

					final int eventDayOfWeek = eventCalendar.get(Calendar.DAY_OF_WEEK);
					final int lectureDayOfWeek = lectureCalendar.get(Calendar.DAY_OF_WEEK);

					if (eventDayOfWeek != lectureDayOfWeek) {
						final int diferenceDays = lectureDayOfWeek - eventDayOfWeek;
						eventCalendar.add(Calendar.DAY_OF_YEAR, diferenceDays);
					}

					// set hour and minutes from the lecture
					eventCalendar.set(Calendar.HOUR_OF_DAY, lectureCalendar.get(Calendar.HOUR_OF_DAY));
					eventCalendar.set(Calendar.MINUTE, lectureCalendar.get(Calendar.MINUTE));

					newStartDate = eventCalendar.getTime();

					// end Date
					lectureCalendar.setTime(lecture.getEndDate());

					// set hour and minutes from the lecture
					eventCalendar.set(Calendar.HOUR_OF_DAY, lectureCalendar.get(Calendar.HOUR_OF_DAY));
					eventCalendar.set(Calendar.MINUTE, lectureCalendar.get(Calendar.MINUTE));

					newEndDate = eventCalendar.getTime();
				}

				// update the event with the new date or with null to let the date
				instance.updateEvent(eventID, lecture.getLabel(), null, newStartDate, newEndDate, getLocation(lecture.getRoom()));
			}
		}
	}

	String getLocation(String room) {
		Context context = MainActivity.getAppContext().getApplicationContext();

		if (room.length() < 4) {
			return context.getString(R.string.noLocation);
		}

		if (room.contains(Define.ROOM_MUEB)) {
			// Münchberg
			return Define.LOCATION_MUEB + ", " + room;
		} else {
			// Hof
			return Define.LOCATION_HOF + ", " + room;
		}
	}

	// Dss Event (Kalendereintrag) wird im Kalender aktualisiert
	//
	private void updateEvent(long eventID, String title, String description, Date startTime, Date endTime, String location) {
		Context context = MainActivity.getAppContext().getApplicationContext();

		ContentValues values = new ContentValues();
		values.put(Events.TITLE, title);
		if (description != null) {
			values.put(Events.DESCRIPTION, description);
		}
		if (startTime != null) {
			values.put(Events.DTSTART, startTime.getTime());
		}
		if (endTime != null) {
			values.put(Events.DTEND, endTime.getTime());
		}
		values.put(Events.EVENT_LOCATION, location);

		Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);

		ContentResolver cr = context.getContentResolver();
		cr.update(updateUri, values, null, null);
	}

	private Boolean doEventExits(Long eventID, String lectureTitle, Date startDate, Date endDate) {
		Context context = MainActivity.getAppContext().getApplicationContext();

		Cursor cur;
		ContentResolver cr = context.getContentResolver();

		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
		// TODO vielleicht ohne Event_ID als selection und dafür das ganze Array, und damit später vergleichen ob enthalten
		String selection = CalendarContract.Instances.EVENT_ID + " = ?";
		String[] selectionArgs = new String[]{eventID.toString()};

		// Construct the query with the desired date range.
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startDate.getTime());
		// 5 Millisekunden dazu damit falls es gleich dem Startdatum ist er das Event nimmt
		ContentUris.appendId(builder, endDate.getTime() + 5);

		// Submit the query
		cur = cr.query(builder.build(),
				EVENT_PROJECTION_EVENT_ID_TITLE_DESCRIPTION,
				selection,
				selectionArgs,
				null);

		if (cur == null) {
			return false;
		}

		while (cur.moveToNext()) {
			String eventTitle;

			// Get the field values
			eventTitle = cur.getString(PROJECTION_TITLE_INDEX);

			// überprpfe ob lecture ID gesetzt und gleich ist
			/*if (eventLectureID != null && eventLectureID.equals(lectureID)) {
				resultEventIDs.add(eventID);
			} else*/
			if (eventTitle != null){

				// Der Titel kann manipuliert worden sein, bspw. mit "[Entfällt]"
				final String eventTitleLower = eventTitle.toLowerCase();
				final String lectureTitleLower = lectureTitle.toLowerCase();

				// Der Titel kann manipuliert worden sein, bspw. mit "[Entfällt]"
				if ( eventTitleLower.contains(lectureTitleLower)) {
					// gefunden
					cur.close();
					return true;
				}
			}
		}
		cur.close();
		return false;
	}

	/**
	 * gets the eventIDs of an event retrieved by lectureID (SPlusName) from the calendar
	 *
	 * @param lectureID the ID of the lecture (SPlusName)
	 * @param lectureTitle the title of the lecture
	 * @param startDate the startDate
	 * @param endDate the endDate
	 * @return the eventID if exits, else null if not
	 */
	private ArrayList<Long> getEventIDs(final String lectureID, final String lectureTitle,
	                                    Date startDate, Date endDate) {
		//TODO lectureID not used

		Context context = MainActivity.getAppContext().getApplicationContext();

		ArrayList<Long> resultEventIDs = new ArrayList<>();

		Cursor cur;
		ContentResolver cr = context.getContentResolver();

		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
		// the calendarID that it only search in the right calendar
		String selection = CalendarContract.Instances.CALENDAR_ID + " = ?";
		String[] selectionArgs = new String[]{calendarData.getCalendarID().toString()};

		// Construct the query with the desired date range.
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startDate.getTime());
		// 5 Millisekunden dazu damit falls es gleich dem Startdatum ist er das Event nimmt
		ContentUris.appendId(builder, endDate.getTime() + 5);

		// Submit the query
		cur = cr.query(builder.build(),
				EVENT_PROJECTION_EVENT_ID_TITLE_DESCRIPTION,
				selection,
				selectionArgs,
				null);

		if (cur == null) {
			return null;
		}

		while (cur.moveToNext()) {
			// Get the field values
			final String eventLectureID = cur.getString(PROJECTION_ORIGINAL_SYNC_ID_INDEX);
			final Long eventID = cur.getLong(PROJECTION_EVENT_ID);
			final String eventTitle = cur.getString(PROJECTION_TITLE_INDEX);

			if (DEBUG_CALENDAR_INTERFACE) Log.d(TAG, "getEventIDs: OrginalSyncID: " + eventLectureID);

			// überprpfe ob lecture ID gesetzt und gleich ist
			/* MS
			if (eventLectureID != null && eventLectureID.equals(lectureID)) {
				resultEventIDs.add(eventID);
			} else
			*/
			if (eventTitle != null) {

				// Der Titel kann manipuliert worden sein, bspw. mit "[Entfällt]"
				final String eventTitleLower = eventTitle.toLowerCase();
				final String lectureTitleLower = lectureTitle.toLowerCase();

				// Der Titel kann manipuliert worden sein, bspw. mit "[Entfällt]"
				if ( eventTitleLower.contains(lectureTitleLower)) {
					// falls nicht überprüfe noch den title
					resultEventIDs.add(eventID);
				}
			}
		}
		cur.close();

		// if eventIDs were found return them
		if (!resultEventIDs.isEmpty())
			return resultEventIDs;

		// otherwise return null
		return null;
	}

	private void getEventDates(final Long eventID, final Date startDate, final Date endDate) {
		final Context context = MainActivity.getAppContext().getApplicationContext();
		final ContentResolver cr = context.getContentResolver();

		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
		// TODO vielleicht ohne Event_ID als selection und dafür das ganze Array, und damit später vergleichen ob enthalten
		final String selection = CalendarContract.Instances.EVENT_ID + " = ?";
		final String[] selectionArgs = new String[]{eventID.toString()};

		// Construct the query with the desired date range.
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startDate.getTime());
		// 5 Millisekunden dazu damit falls es gleich dem Startdatum ist er das Event nimmt
		ContentUris.appendId(builder, endDate.getTime() + 5);

		// Submit the query
		final Cursor cur = cr.query(builder.build(),
				EVENT_PROJECTION_DATES,
				selection,
				selectionArgs,
				null);

		if (cur == null) {
			return;
		}

		while (cur.moveToNext()) {
			// Get the field values
			long eventBegin = cur.getLong(PROJECTION_EVENT_BEGIN);

			Calendar calendar = new GregorianCalendar();

			calendar.setTimeInMillis(eventBegin);
			calendar.set(Calendar.MILLISECOND, 0);
			eventStartDate = calendar.getTime();

			final long eventEnd = cur.getLong(PROJECTION_EVENT_END);

			calendar.setTimeInMillis(eventEnd);
			calendar.set(Calendar.MILLISECOND, 0);
			eventEndDate = calendar.getTime();
		}
		cur.close();
	}

	/**
	 * get the event description
	 * @param eventID the eventID of the event
	 * @return the description or null if no permission or the event is not found
	 */
	private String getEventDescription(Long eventID) {
		final Context context = MainActivity.getAppContext().getApplicationContext();
		final ContentResolver cr = context.getContentResolver();

		if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
			// keine Berechtigung
			return null;
		}


		// The ID of the recurring event whose instances you are searching
		// for in the Instances table
		// TODO vielleicht ohne Event_ID als selection und dafür das ganze Array, und damit später vergleichen ob enthalten
		final String selection = CalendarContract.Instances.EVENT_ID + " = ?";
		final String[] selectionArgs = new String[]{eventID.toString()};

		// Construct the query with the desired date range.
		final Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

		long now = new Date().getTime();

		ContentUris.appendId(builder, now - (DateUtils.DAY_IN_MILLIS * 500));
		ContentUris.appendId(builder, now + (DateUtils.DAY_IN_MILLIS * 500));

		// Submit the query
		final Cursor cur = cr.query(builder.build(),
				EVENT_PROJECTION_EVENT_ID_TITLE_DESCRIPTION,
				selection,
				selectionArgs,
				null);

		if (cur == null) {
			return null;
		}

		String eventDescription = "";
		if (cur.moveToNext()) {
			// Get the field values
			eventDescription = cur.getString(PROJECTION_DESCRIPTION_INDEX);

            /*if (!eventDescription.isEmpty()) {
                if (DEBUG_CALENDAR_INTERFACE) Log.d(TAG, "Title: " + cur.getString(PROJECTION_TITLE_INDEX) + "\nDescription: " + eventDescription);
            }*/

		}
		cur.close();
		return eventDescription;
	}

	private void deleteEvent(long eventID) {
		Context context = MainActivity.getAppContext().getApplicationContext();

		if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
			// keine Berechtigung
			return;
		}

		// Events mit Description nicht löschen
		final String eventDescription = getEventDescription(eventID);
		if ((eventDescription != null) && !eventDescription.isEmpty()) {
			Log.i( TAG, "CalendarInterface: deleteEvent: NICHT: " + String.valueOf(eventID) + " " + eventDescription );
			return;
		}

		Log.i( TAG, "CalendarInterface: deleteEvent: " + String.valueOf(eventID) );

		Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);

		ContentResolver cr = context.getContentResolver();
		cr.delete(deleteUri, null, null);
	}

	void deleteAllEvents(final String lectureID) {
		Context context = MainActivity.getAppContext().getApplicationContext();

		if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
			// keine Berechtigung
			return;
		}
		// get the eventIDs
		ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);

		// prüfen ob welche vorhanden sind, falls nein, mache nichts
		if (eventIDs == null) {
			return;
		}

		// delete the events
		for (Long eventID : eventIDs) {
			deleteEvent(eventID);
		}

		// remove the eventIDs
		removeAllLectureEventIDs(lectureID);
	}

	void deleteAllEvents() {
		for (String lectureID : calendarData.getLecturesEventIDs().keySet()) {
			deleteAllEvents(lectureID);
		}
		removeAllLectruesEventIDs();
		for (String changeID : calendarData.getChangesEventIDs().keySet()) {
			deleteAllEvents( changeID );
		}
		removeAllChangesEventIDs();
	}

	private void addLecturesEventID(String lectureID, Long eventID) {

		ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);
		if (eventIDs == null) {
			eventIDs = new ArrayList<>();
		}
		eventIDs.add(eventID);

		calendarData.getLecturesEventIDs().put(lectureID, eventIDs);
	}

	private void removeLectureEventID(final String lectureID, final Long eventID) {
		final ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);
		if (eventIDs != null) {
			eventIDs.remove(eventID);
			calendarData.getLecturesEventIDs().put(lectureID, eventIDs);
		}
	}

	private void removeAllLectureEventIDs(final String lectureID) {
		calendarData.getLecturesEventIDs().put(lectureID, new ArrayList<Long>());
	}

	private void removeAllLectruesEventIDs() {
		calendarData.getLecturesEventIDs().clear();
	}

	private void addChangesEventID(final String lectureID, final Long eventID) {
		ArrayList<Long> eventIDs = calendarData.getChangesEventIDs().get(lectureID);
		if (eventIDs == null) {
			eventIDs = new ArrayList<>();
		}
		eventIDs.add(eventID);

		calendarData.getChangesEventIDs().put(lectureID, eventIDs);
	}

// --Commented out by Inspection START (28.10.2017 23:24):
//	private void removeChangesEventID(final String lectureID, final Long eventID) {
//
//		final ArrayList<Long> eventIDs = calendarData.getChangesEventIDs().get(lectureID);
//		if (eventIDs != null) {
//			eventIDs.remove(eventID);
//			calendarData.getChangesEventIDs().put(lectureID, eventIDs);
//		}
//	}
// --Commented out by Inspection STOP (28.10.2017 23:24)

	private void removeAllChangesEventIDs() {
		calendarData.getChangesEventIDs().clear();
	}

	void saveCalendarData() {
		final Context context = MainActivity.getAppContext().getApplicationContext();

		DataManager.getInstance().saveObject(context, calendarData, Define.calendarIDsFilename);
	}

	private void readCalendarData() {
		final Context context = MainActivity.getAppContext().getApplicationContext();

		final Object tmpCalendarEventIds = DataManager.getInstance().readObject(context, Define.calendarIDsFilename);
		if ((tmpCalendarEventIds != null) && (tmpCalendarEventIds instanceof CalendarData)) {
			this.calendarData = (CalendarData) tmpCalendarEventIds;
		}
	}

	private Uri asSyncAdapter(final Uri uri, final String account, final String accountType) {
		return uri.buildUpon()
				.appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
				.appendQueryParameter(Calendars.ACCOUNT_NAME, account)
				.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
				.build();
	}
}
