package de.hof.university.app.calendar;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarInterfaceController {
    private static CalendarInterfaceController calendarInterfaceController = null;

    private Context context;
    private CalendarInterface calendarInterface;

    public static CalendarInterfaceController getInstance(Context context) {
        if (CalendarInterfaceController.calendarInterfaceController == null) {
            CalendarInterfaceController.calendarInterfaceController = new CalendarInterfaceController(context);
        }
        return CalendarInterfaceController.calendarInterfaceController;
    }

    private CalendarInterfaceController(Context context) {
        // TODO
        this.context = context;
        calendarInterface = CalendarInterface.getInstance(context);
    }

    public void createAllEvents() {
        // TODO
        ArrayList<LectureItem> lectureItems = DataManager.getInstance().getSelectedLectures(context);

        if (lectureItems == null) return;

        for (LectureItem lectureItem :
                lectureItems) {
            createEventsForLecture(lectureItem);
        }
        calendarInterface.saveIDs();
    }

    public void createAllEvents(LectureItem lecture) {
        createEventsForLecture(lecture);
        calendarInterface.saveIDs();
    }

    private void createEventsForLecture(LectureItem lectureItem) {
        Date tmpStartDate = lectureItem.getStartDate();

        Calendar endDateCalendar = GregorianCalendar.getInstance();
        endDateCalendar.setTime(lectureItem.getEndDate());

        do {
            // TODO für jeden Termin das Datum ermitteln
            Calendar newEndDateCalendar = GregorianCalendar.getInstance();
            newEndDateCalendar.setTime(tmpStartDate);

            newEndDateCalendar.set(Calendar.HOUR_OF_DAY, endDateCalendar.get(Calendar.HOUR_OF_DAY));
            newEndDateCalendar.set(Calendar.MINUTE, endDateCalendar.get(Calendar.MINUTE));

            calendarInterface.createEvent(lectureItem.getId(), lectureItem.getLabel(), "", tmpStartDate, newEndDateCalendar.getTime(), "");

            Calendar startDateCalendar = GregorianCalendar.getInstance();
            startDateCalendar.setTime(tmpStartDate);

            // Eine Woche dazu
            startDateCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            tmpStartDate = startDateCalendar.getTime();
        } while (tmpStartDate.before(lectureItem.getEndDate()));
    }

    public void updateAllEvents() {
        // TODO
    }

    public void deleteAllEvents() {
        // TODO
        calendarInterface.deleteAllEvents();
        calendarInterface.saveIDs();
    }

    public void deleteAllEvents(String lectureID) {
        calendarInterface.deleteAllEvents(lectureID);
        calendarInterface.saveIDs();
    }

    public void updateCalendar() {
        deleteAllEvents();
        createAllEvents();
    }

    public void removeCalendar() {
        calendarInterface.removeCalendar();
        calendarInterface.saveIDs();
    }
}
