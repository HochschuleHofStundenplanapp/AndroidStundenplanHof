package de.hof.university.app.calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

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
        CreateAllEventsTask task = new CreateAllEventsTask();
        task.execute();
    }

    public void createAllEvents(LectureItem lecture) {
        CreateAllEventsTask task = new CreateAllEventsTask();
        task.execute(lecture);
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

    public Boolean deleteAllEvents() {
        DeleteAllEventsTask task = new DeleteAllEventsTask();
        task.execute();
        try {
            return task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteAllEvents(String lectureID) {
        DeleteAllEventsTask task = new DeleteAllEventsTask();
        task.execute(lectureID);
    }

    public void updateCalendar() {
        new Thread() {
            @Override
            public void run() {
                if (deleteAllEvents()) {
                    createAllEvents();
                }
            }
        }.start();
    }

    public void removeCalendar() {
        calendarInterface.removeCalendar();
        calendarInterface.saveIDs();
    }

    private class CreateAllEventsTask extends AsyncTask<LectureItem, Void, Boolean> {
        protected Boolean doInBackground(LectureItem... p_lectureItems) {
            if (p_lectureItems == null || p_lectureItems.length == 0) {
                ArrayList<LectureItem> lectureItems = DataManager.getInstance().getSelectedLectures(context);

                if (lectureItems == null) return false;

                for (LectureItem lectureItem :
                        lectureItems) {
                    createEventsForLecture(lectureItem);
                }
            } else {
                createEventsForLecture(p_lectureItems[0]);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Toast.makeText(context, "CreateAllEventsTask fertig. Result: " + result, Toast.LENGTH_SHORT).show();
            if (result) {
                calendarInterface.saveIDs();
            }
        }
    }

    private class DeleteAllEventsTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... p_lectureItems) {
            if (p_lectureItems == null || p_lectureItems.length == 0) {
                calendarInterface.deleteAllEvents();
            } else {
                calendarInterface.deleteAllEvents(p_lectureItems[0]);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Toast.makeText(context, "DeleteAllEventsTask fertig. Result: " + result, Toast.LENGTH_SHORT).show();
            if (result) {
                calendarInterface.saveIDs();
            }
        }
    }
}
