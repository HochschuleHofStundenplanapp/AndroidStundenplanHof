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
    private Context context;
    private CalendarInterface calendarInterface;

    public CalendarInterfaceController(Context context) {
        // TODO
        this.context = context;
        calendarInterface = CalendarInterface.getInstance(context);
    }

    public void createAllEvents() {
        // TODO
        ArrayList<LectureItem> lectureItems = DataManager.getInstance().getSelectedLectures(context);

        for (LectureItem li :
                lectureItems) {
            Date tmpStartDate = li.getStartDate();
            do {
                // TODO für jeden Termin das Datum ermitteln
                Calendar startDateCalendar = GregorianCalendar.getInstance();
                Calendar endDateCalendar = GregorianCalendar.getInstance();
                startDateCalendar.setTime(tmpStartDate);
                endDateCalendar.setTime(li.getEndDate());

                startDateCalendar.set(Calendar.HOUR_OF_DAY, endDateCalendar.get(Calendar.HOUR_OF_DAY));
                startDateCalendar.set(Calendar.MINUTE, endDateCalendar.get(Calendar.MINUTE));

                calendarInterface.createEvent(li.getId(), li.getLabel(), "", tmpStartDate, endDateCalendar.getTime(), "");

                startDateCalendar.setTime(tmpStartDate);

                // Eine Woche dazu
                startDateCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            } while (tmpStartDate.before(li.getEndDate()));
        }
    }

    public void updateAllEvents() {
        // TODO
    }

    public void deleteAllEvents() {
        // TODO
        calendarInterface.deleteAllEvents();
    }

    public void removeCalendar() {
        calendarInterface.removeCalendar();
    }
}
