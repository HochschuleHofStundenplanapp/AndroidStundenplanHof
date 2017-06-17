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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.data.DataManager;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarInterface {
    public static final String TAG = "CalendarInterface";

    private static CalendarInterface calendarInterface = null;

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                          // 0
            Calendars.ACCOUNT_NAME,                 // 1
            Calendars.CALENDAR_DISPLAY_NAME,        // 2
            Calendars.OWNER_ACCOUNT,                // 3
            Calendars.ACCOUNT_TYPE,                 // 4
            Calendars.NAME                          // 5
    };

    public static final String[] INSTANCE_PROJECTION = new String[]{
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 4;

    // The indices for the projection array above.
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;

    private Context context;
    private long calID = 2;
    private String calendarName = "Hochschule Hof Stundenplan App";
    private CalendarEventIds calendarEventIds = new CalendarEventIds();


    public static CalendarInterface getInstance(Context context) {
        if (CalendarInterface.calendarInterface == null) {
            CalendarInterface.calendarInterface = new CalendarInterface(context);
        }
        return CalendarInterface.calendarInterface;
    }

    /**
     * Constructor for the default local calendar
     *
     * @param context
     */
    private CalendarInterface(Context context) {
        this(context, -1);
    }

    /**
     * Constructor for a existing calendar
     *
     * @param context
     * @param calendarID of a existing calendar in witch the user want to write the Schedule
     */
    private CalendarInterface(Context context, long calendarID) {
        this.context = context;

        // bereits vohandene IDs einlesen
        readIDs();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Wenn -1 dann lokalen Calendar, sonst die übergebene ID nutzen
            if (calendarID == -1) {
                if (!getLocalCalendar()) {
                    createLocalCalendar();
                    getLocalCalendar();
                }
            } else {
                // use existing calendar
                calID = calendarID;
            }
        }
    }

    /**
     * Set the calID and return if the calendar was found
     *
     * @return return if the calendar was found
     */
    private boolean getLocalCalendar() {
        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "(("
                + Calendars.NAME + " = ?) AND ("
                + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?)"
                + ")";
        String[] selectionArgs = new String[]{calendarName, "Hochschule Hof", "androidapps@hof-university.de", CalendarContract.ACCOUNT_TYPE_LOCAL};
        // Submit the query and get a Cursor object back.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        // wenn er alle Calendar liefern soll:
        //cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            // found
            calID = cur.getLong(PROJECTION_ID_INDEX);
            cur.close();
            return true;
        }

        cur.close();
        return false;
    }

    private Uri createLocalCalendar() {
        // TODO

        Uri uri = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        Uri calendarUri = uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, calendarName)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

        ContentValues values = new ContentValues();
        values.put(Calendars.NAME, calendarName);
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(Calendars.CALENDAR_DISPLAY_NAME, calendarName);
        values.put(Calendars.CALENDAR_COLOR, 0xFF0000);
        TimeZone tz = TimeZone.getDefault();
        values.put(Calendars.CALENDAR_TIME_ZONE, tz.getID());
        values.put(Calendars.VISIBLE, 1);
        values.put(Calendars.ACCOUNT_NAME, "Hochschule Hof");
        values.put(Calendars.OWNER_ACCOUNT, "androidapps@hof-university.de");
        //values.put(Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        //values.put(Calendars.SYNC_EVENTS, 1);
        //values.put(Calendars.CAN_PARTIALLY_UPDATE, 1);

        Uri newCalendar = context.getContentResolver().insert(calendarUri, values);

        return newCalendar;
    }

    public void removeCalendar() {
        // TODO
    }

    public void createEvent(String lectureID, String title, String description, Date startTime, Date endTime, String location) {
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startTime.getTime());
        values.put(Events.DTEND, endTime.getTime());
        values.put(Events.TITLE, title);
        values.put(Events.DESCRIPTION, description);
        values.put(Events.EVENT_LOCATION, location);
        values.put(Events.CALENDAR_ID, calID);
        TimeZone tz = TimeZone.getDefault();
        values.put(Events.EVENT_TIMEZONE, tz.getID());

        Long eventID = insertEvent(values);
        // Wenn null dann keine Berechtigung und returnen
        if (eventID == null) return;
        // zu den IDs hinzufügen
        addLecturesEventID(lectureID, eventID);
    }

    private Long insertEvent(ContentValues values) {
        ContentResolver cr = context.getContentResolver();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        return Long.parseLong(uri.getLastPathSegment());
    }

    public void updateEvent(long eventID, String title, String description, Date startTime, Date endTime, String location) {
        // TODO
        ContentValues values = new ContentValues();
        values.put(Events.TITLE, title);
        values.put(Events.DESCRIPTION, description);
        values.put(Events.DTSTART, startTime.getTime());
        values.put(Events.DTEND, endTime.getTime());
        values.put(Events.EVENT_LOCATION, location);

        Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);

        ContentResolver cr = context.getContentResolver();
        int rows = cr.update(updateUri, values, null, null);
    }

    public Boolean doEventExits(Long eventID, String title, Date startDate, Date endDate) {
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[]{eventID.toString()};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startDate.getTime());
        ContentUris.appendId(builder, endDate.getTime());

        // Submit the query
        cur = cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        while (cur.moveToNext()) {
            String eventTitle = null;
            long eventEventID = 0;
            long beginVal = 0;

            // Get the field values
            eventEventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            eventTitle = cur.getString(PROJECTION_TITLE_INDEX);

            // Do something with the values.
            Log.d(TAG, "Event:  " + eventTitle);
            Log.d(TAG, "EventID: " + eventEventID);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i(TAG, "Date: " + formatter.format(calendar.getTime()));

            if (eventTitle.equals(title)) {
                return true;
            }
        }
        cur.close();
        return false;
    }

    private void deleteEvent(long eventID) {
        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);

        ContentResolver cr = context.getContentResolver();
        int rows = cr.delete(deleteUri, null, null);
    }

    public void deleteAllEvents(String lectureID) {
        ArrayList<Long> eventIDs = calendarEventIds.getLecturesEventIDs().get(lectureID);
        for (Long eventID :
                eventIDs) {
            deleteEvent(eventID);
        }
        removeAllLectureEventIDs(lectureID);
    }

    public void deleteAllEvents() {
        for (String lectureID :
                calendarEventIds.getLecturesEventIDs().keySet()) {
            deleteAllEvents(lectureID);
        }
    }

    private void addLecturesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarEventIds.getLecturesEventIDs().get(lectureID);
        if (eventIDs == null) {
            eventIDs = new ArrayList<>();
            eventIDs.add(eventID);
        } else {
            eventIDs.add(eventID);
        }
        calendarEventIds.getLecturesEventIDs().put(lectureID, eventIDs);
    }

    private void removeLectureEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarEventIds.getLecturesEventIDs().get(lectureID);
        if (eventIDs != null) {
            eventIDs.remove(eventID);
            calendarEventIds.getLecturesEventIDs().put(lectureID, eventIDs);
        }
    }

    private void removeAllLectureEventIDs(String lectureID) {
        calendarEventIds.getLecturesEventIDs().put(lectureID, new ArrayList<Long>());
    }

    private void addChangesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarEventIds.getChangesEventIDs().get(lectureID);
        if (eventIDs == null) {
            eventIDs = new ArrayList<>();
            eventIDs.add(eventID);
        } else {
            eventIDs.add(eventID);
        }
        calendarEventIds.getChangesEventIDs().put(lectureID, eventIDs);
    }

    private void removeChangesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarEventIds.getChangesEventIDs().get(lectureID);
        if (eventIDs != null) {
            eventIDs.remove(eventID);
            calendarEventIds.getChangesEventIDs().put(lectureID, eventIDs);
        }
    }

    public void saveIDs() {
        DataManager.getInstance().saveObject(context, calendarEventIds, Define.calendarIDsFilename);
    }

    public void readIDs() {
        Object tmpCalendarEventIds = DataManager.getInstance().readObject(context, Define.calendarIDsFilename);
        if (tmpCalendarEventIds != null && tmpCalendarEventIds instanceof CalendarEventIds) {
            calendarEventIds = (CalendarEventIds) tmpCalendarEventIds;
        }
    }
}
