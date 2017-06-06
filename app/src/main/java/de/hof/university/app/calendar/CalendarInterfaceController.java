package de.hof.university.app.calendar;

import android.content.Context;

import java.util.ArrayList;

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
            // TODO für jeden Termin das Datum ermitteln
            calendarInterface.createEvent(li.getId(), li.getLabel(), "", li.getStartDate(), li.getEndDate(), "");
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
