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

            Calendar endDateCalendar = GregorianCalendar.getInstance();
            endDateCalendar.setTime(li.getEndDate());

            do {
                // TODO für jeden Termin das Datum ermitteln
                Calendar newEndDateCalendar = GregorianCalendar.getInstance();
                newEndDateCalendar.setTime(tmpStartDate);

                newEndDateCalendar.set(Calendar.HOUR_OF_DAY, endDateCalendar.get(Calendar.HOUR_OF_DAY));
                newEndDateCalendar.set(Calendar.MINUTE, endDateCalendar.get(Calendar.MINUTE));

                calendarInterface.createEvent(li.getId(), li.getLabel(), "", tmpStartDate, newEndDateCalendar.getTime(), "");

                Calendar startDateCalendar = GregorianCalendar.getInstance();
                startDateCalendar.setTime(tmpStartDate);

                // Eine Woche dazu
                startDateCalendar.add(Calendar.WEEK_OF_YEAR, 1);
                tmpStartDate = startDateCalendar.getTime();
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
