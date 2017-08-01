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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureChange;

/**
 * Created by Daniel on 13.05.2017.
 */

class CalendarInterface {
    public static final String TAG = "CalendarInterface";

    private static final CalendarInterface instance = new CalendarInterface();

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                          // 0
            Calendars.ACCOUNT_NAME,                 // 1
            Calendars.CALENDAR_DISPLAY_NAME,        // 2
            Calendars.OWNER_ACCOUNT,                // 3
            Calendars.ACCOUNT_TYPE,                 // 4
            Calendars.NAME                          // 5
    };

    private static final String[] INSTANCE_PROJECTION = new String[]{
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

    private String localCalendarName = "";
    private CalendarData calendarData = new CalendarData();

    public static CalendarInterface getInstance() {
        return instance;
    }

    /**
     * Constructor for the default local calendar
     */
    private CalendarInterface() {
        Context context = MainActivity.getAppContext().getApplicationContext();
        localCalendarName = context.getString(R.string.stundenplan) + " " + context.getString(R.string.app_name);

        // bereits vohandene IDs einlesen
        readCalendarData();

        if (calendarData.getCalendarID() == null) {
            // TODO CalendarID leer
            Log.d(TAG, "CalendarID is empty in constructor");
        }
    }

    public void setCalendar(Long calendarID) {
        Context context = MainActivity.getAppContext().getApplicationContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
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

    HashMap<String, Long> getCalendars() {
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

        // wenn er alle Calendar liefern soll:
        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        if (cur == null) {
            return result;
        }

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            // found
            result.put(cur.getString(PROJECTION_DISPLAY_NAME_INDEX), cur.getLong(PROJECTION_ID_INDEX));
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
        String[] selectionArgs = new String[]{localCalendarName, context.getString(R.string.app_name), Define.NOREPLYEMAILADDRESS, CalendarContract.ACCOUNT_TYPE_LOCAL};

        // Submit the query and get a Cursor object back.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        // wenn er alle Calendar liefern soll:
        //cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        if (cur == null) {
            return null;
        }

        // Use the cursor to step through the returned records
        if (cur.moveToNext()) {
            // found
            result = cur.getLong(PROJECTION_ID_INDEX);
            cur.close();
            return result;
        }

        cur.close();
        return null;
    }

    /**
     * creates the local calendar
     *
     * @return returns the Uri to the created calendar
     */
    private Uri createLocalCalendar() {
        Context context = MainActivity.getAppContext().getApplicationContext();
        // TODO

        Uri uri = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        Uri calendarUri = uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

        ContentValues values = new ContentValues();
        values.put(Calendars.NAME, localCalendarName);
        values.put(Calendars.CALENDAR_DISPLAY_NAME, localCalendarName);
        values.put(Calendars.CALENDAR_COLOR, 0xFF0000);
        TimeZone tz = TimeZone.getDefault();
        values.put(Calendars.CALENDAR_TIME_ZONE, tz.getID());
        values.put(Calendars.VISIBLE, 1);
        values.put(Calendars.OWNER_ACCOUNT, Define.NOREPLYEMAILADDRESS);
        values.put(Calendars.SYNC_EVENTS, 1);
        values.put(Calendars.ACCOUNT_NAME, context.getString(R.string.app_name));
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        //values.put(Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        //values.put(Calendars.CAN_PARTIALLY_UPDATE, 1);

        // return Uri of the new created calendar
        return context.getContentResolver().insert(calendarUri, values);
    }

    /**
     * removes the local calendar
     *
     * @return returns if the removing was successful
     */
    boolean removeLocalCalendar() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        // TODO
        Long localCalendarID = getLocalCalendar();

        if (localCalendarID == null) {
            return false;
        }

        Uri.Builder builder = Calendars.CONTENT_URI.buildUpon();
        Uri calendarToRemoveUri = builder.appendPath(localCalendarID.toString())
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

        context.getContentResolver().delete(calendarToRemoveUri, null, null);

        //Alle IDs löschen
        removeAllLectruesEventIDs();
        saveCalendarData();
        return true;
    }

    /**
     * creates a event
     *
     * @param title the title of the event
     * @param description the description of the event
     * @param startDate the start Date of the event
     * @param endDate the end Date of the event
     * @param location the location of the event
     * @return eventID or null if no permission
     */
    private Long createEvent(String title, String description, Date startDate, Date endDate, String location) {
        if (calendarData.getCalendarID() == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startDate.getTime());
        values.put(Events.DTEND, endDate.getTime());
        values.put(Events.TITLE, title);
        values.put(Events.DESCRIPTION, description);
        values.put(Events.EVENT_LOCATION, location);
        values.put(Events.CALENDAR_ID, calendarData.getCalendarID());
        TimeZone tz = TimeZone.getDefault();
        values.put(Events.EVENT_TIMEZONE, tz.getID());

        // die EventID kommt zurück
        return insertEvent(values);
    }

    void createLectureEvent(String lectureID, String title, String description, Date startTime, Date endTime, String location) {
        Long eventID = createEvent(title, description, startTime, endTime, location);

        // Wenn null dann keine Berechtigung oder keine CalendarID und returnen
        if (eventID == null) return;

        // zu den IDs hinzufügen
        addLecturesEventID(lectureID, eventID);
    }

    private void createChangeEvent(String lectureID, String title, String description, Date startTime, Date endTime, String location) {
        Long eventID = createEvent(title, description, startTime, endTime, location);

        // Wenn null dann keine Berechtigung oder keine CalendarID und returnen
        if (eventID == null) return;

        // zu den IDs hinzufügen
        addChangesEventID(lectureID, eventID);
    }

    private Long insertEvent(ContentValues values) {
        Context context = MainActivity.getAppContext().getApplicationContext();

        ContentResolver cr = context.getContentResolver();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        if (uri == null) {
            return null;
        }

        // get the event ID that is the last element in the Uri
        return Long.parseLong(uri.getLastPathSegment());
    }

    void updateChange(LectureChange change) {
        Context context = MainActivity.getAppContext().getApplicationContext();

        // TODO
        String lectureID;

        lectureID = change.getId().substring(0, change.getId().indexOf(Define.CHANGES_SUBSTRING));

        ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);

        // wenn leer dann zurück
        if (eventIDs == null) return;

        for (Long eventID :
                eventIDs) {
            // TODO vielleicht endDatum ändern
            if (doEventExits(eventID, change.getLabel(), change.getBegin_old(), change.getBegin_old())) {

                if (change.getBegin_new() == null) {
                    // Entfällt
                    instance.updateEvent(eventID, context.getString(R.string.changeCancelled) + " " + change.getLabel(), null, null, null, "");
                } else {
                    // Verschoben oder Raumänderung
                    if (change.getBegin_new().equals(change.getBegin_old())) {
                        // Raumänderung
                        instance.updateEvent(eventID, context.getString(R.string.changeRoomchange) + " " + change.getLabel(), null, null, null, "");
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

    private void updateEvent(long eventID, String title, String description, Date startTime, Date endTime, String location) {
        Context context = MainActivity.getAppContext().getApplicationContext();

        // TODO
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

    private Boolean doEventExits(Long eventID, String title, Date startDate, Date endDate) {
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
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        if (cur == null) {
            return false;
        }

        while (cur.moveToNext()) {
            String eventTitle;
            //long eventEventID;
            //long beginVal;

            // Get the field values
            //eventEventID = cur.getLong(PROJECTION_ID_INDEX);
            //beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            eventTitle = cur.getString(PROJECTION_TITLE_INDEX);

            // Do something with the values.
            //Log.d(TAG, "Event:  " + eventTitle);
            //Log.d(TAG, "EventID: " + eventEventID);
            /*
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            Log.i(TAG, "Date: " + formatter.format(calendar.getTime()));
            */

            // TODO vielleicht ohne title da dieser bearbeitet werden kann
            if (eventTitle.equals(title)) {
                return true;
            }
        }
        cur.close();
        return false;
    }

    private void deleteEvent(long eventID) {
        Context context = MainActivity.getAppContext().getApplicationContext();

        Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);

        ContentResolver cr = context.getContentResolver();
        cr.delete(deleteUri, null, null);
    }

    void deleteAllEvents(String lectureID) {
        ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);

        if (eventIDs == null) {
            return;
        }

        for (Long eventID :
                eventIDs) {
            deleteEvent(eventID);
        }
        removeAllLectureEventIDs(lectureID);
    }

    void deleteAllEvents() {
        for (String lectureID :
                calendarData.getLecturesEventIDs().keySet()) {
            deleteAllEvents(lectureID);
        }
        removeAllLectruesEventIDs();
    }

    private void addLecturesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);
        if (eventIDs == null) {
            eventIDs = new ArrayList<>();
            eventIDs.add(eventID);
        } else {
            eventIDs.add(eventID);
        }
        calendarData.getLecturesEventIDs().put(lectureID, eventIDs);
    }

    private void removeLectureEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarData.getLecturesEventIDs().get(lectureID);
        if (eventIDs != null) {
            eventIDs.remove(eventID);
            calendarData.getLecturesEventIDs().put(lectureID, eventIDs);
        }
    }

    private void removeAllLectureEventIDs(String lectureID) {
        calendarData.getLecturesEventIDs().put(lectureID, new ArrayList<Long>());
    }

    private void removeAllLectruesEventIDs() {
        calendarData.getLecturesEventIDs().clear();
    }

    private void addChangesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarData.getChangesEventIDs().get(lectureID);
        if (eventIDs == null) {
            eventIDs = new ArrayList<>();
            eventIDs.add(eventID);
        } else {
            eventIDs.add(eventID);
        }
        calendarData.getChangesEventIDs().put(lectureID, eventIDs);
    }

    private void removeChangesEventID(String lectureID, Long eventID) {
        ArrayList<Long> eventIDs = calendarData.getChangesEventIDs().get(lectureID);
        if (eventIDs != null) {
            eventIDs.remove(eventID);
            calendarData.getChangesEventIDs().put(lectureID, eventIDs);
        }
    }

    void saveCalendarData() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        DataManager.getInstance().saveObject(context, calendarData, Define.calendarIDsFilename);
    }

    private void readCalendarData() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        Object tmpCalendarEventIds = DataManager.getInstance().readObject(context, Define.calendarIDsFilename);
        if (tmpCalendarEventIds != null && tmpCalendarEventIds instanceof CalendarData) {
            calendarData = (CalendarData) tmpCalendarEventIds;
        }
    }
}
