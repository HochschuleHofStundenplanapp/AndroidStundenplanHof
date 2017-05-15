package de.hof.university.app.calendar;

import android.content.Context;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarInterfaceController {

    private CalendarInterface calendarInterface;

    public CalendarInterfaceController(Context context) {
        // TODO
        calendarInterface = CalendarInterface.getInstance(context);
    }

    public void createAllEvents() {
        // TODO
    }

    public void updateAllEvents() {
        // TODO
    }

    public void removeAllEvents() {
        // TODO
    }

    public void removeCalendar() {
        calendarInterface.removeCalendar();
    }
}
