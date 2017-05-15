package de.hof.university.app.calendar;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

import de.hof.university.app.MainActivity;
import de.hof.university.app.fragment.settings.SettingsFragment;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarInterface {

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

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 4;

    private Context context;
    private long calID = 2;
    private String calendarName = "Hochschule Hof Stundenplan App";

    public static CalendarInterface getInstance(Context context) {
        if (CalendarInterface.calendarInterface == null) {
            CalendarInterface.calendarInterface = new CalendarInterface(context);
        }
        return CalendarInterface.calendarInterface;
    }

    private CalendarInterface(Context context) {
        // TODO
        this.context = context;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            if (!getCalendar()) {
                createLocalCalendar();
            }
        }
    }

    private boolean getCalendar() {
        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "(("
                + Calendars.NAME + " = ?) AND ("
                + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?)"
                + ")";
        String[] selectionArgs = new String[]{calendarName, "androidapps@hof-university.de", CalendarContract.ACCOUNT_TYPE_LOCAL};
        // Submit the query and get a Cursor object back.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        //cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            calID = cur.getLong(PROJECTION_ID_INDEX);
            cur.close();
            return true;
        }

        return false;
    }

    private Uri createLocalCalendar() {
        // TODO

        Uri uri = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        Uri calendarUri = uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
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
        values.put(Calendars.ACCOUNT_NAME, "androidapps@hof-university.de");
        values.put(Calendars.OWNER_ACCOUNT, "androidapps@hof-university.de");
        //values.put(Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        //values.put(Calendars.SYNC_EVENTS, 1);
        //values.put(Calendars.CAN_PARTIALLY_UPDATE, 1);

        Uri newCalendar = context.getContentResolver().insert(calendarUri,values);

        return newCalendar;
    }

    public void removeCalendar() {
        // TODO
    }

    public void createEvent() {
        // TODO

        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 4, 15, 14, 00);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 4, 15, 15, 30);
        endMillis = endTime.getTimeInMillis();


        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.TITLE, "Test Event");
        values.put(Events.DESCRIPTION, "Dies ist ein Test Event");
        values.put(Events.CALENDAR_ID, calID);
        TimeZone tz = TimeZone.getDefault();
        values.put(Events.EVENT_TIMEZONE, tz.getID());
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Uri uri = cr.insert(Events.CONTENT_URI, values);

        // get the event ID that is the last element in the Uri
        long eventID = Long.parseLong(uri.getLastPathSegment());
        //
        // ... do something with event ID
        //
        //
    }

    public void updateEvent() {
        // TODO
    }

    public void deleteEvent() {
        // TODO
    }
}
