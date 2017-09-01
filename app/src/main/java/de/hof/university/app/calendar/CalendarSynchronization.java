package de.hof.university.app.calendar;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.hof.university.app.MainActivity;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureChange;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarSynchronization {
    private static final CalendarSynchronization instance = new CalendarSynchronization();

    private CalendarInterface calendarInterface;
    private HashMap<String, Long> calendars = new HashMap<>();

    public static CalendarSynchronization getInstance() {
        return instance;
    }

    private CalendarSynchronization() {
        // TODO
        calendarInterface = CalendarInterface.getInstance();
    }

    public void createAllEvents() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        final ArrayList<LectureItem> lectureItems = DataManager.getInstance().getSelectedLectures(context);

        if (lectureItems == null) return;

        CreateAllEventsTask task = new CreateAllEventsTask();
        task.execute(lectureItems);
    }

    public void createAllEvents(final ArrayList<LectureItem> lectureItems) {
        if (lectureItems == null) return;

        CreateAllEventsTask task = new CreateAllEventsTask();
        task.execute(lectureItems);
    }

    public void createAllEvents(final LectureItem lecture) {
        CreateAllEventsTask task = new CreateAllEventsTask();
        ArrayList<LectureItem> list = new ArrayList<>();
        list.add(lecture);
        task.execute(list);
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

            calendarInterface.createLectureEvent(lectureItem.getId(), lectureItem.getLabel(), "", tmpStartDate, newEndDateCalendar.getTime(), calendarInterface.getLocation(lectureItem.getRoom()));

            Calendar startDateCalendar = GregorianCalendar.getInstance();
            startDateCalendar.setTime(tmpStartDate);

            // Eine Woche dazu
            startDateCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            tmpStartDate = startDateCalendar.getTime();
        } while (tmpStartDate.before(lectureItem.getEndDate()));
    }

    public void updateChanges() {
        final Context context = MainActivity.getAppContext().getApplicationContext();
        new Thread() {
            @Override
            public void run() {
                ArrayList<Object> changes = DataManager.getInstance().getChanges(context).getChanges();
                for (Object changeObject :
                        changes) {
                    if (changeObject instanceof LectureChange) {
                        LectureChange change = (LectureChange) changeObject;

                        calendarInterface.updateChange(change);
                    }
                }
            }
        }.start();
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
        // TODO Update Methode nutzen
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (deleteAllEvents()) {
                    createAllEvents();
                }
            }
        }.start();
    }

    /**
     * removes the local calendar or removes the events from a selected calendar
     */
    public void stopCalendarSynchronization() {
        new Thread() {
            @Override
            public void run() {
                //lösche den lokalen Kalender
                if (!calendarInterface.removeLocalCalendar()) {
                    // falls es nicht geklappt hat dann lösche die Events
                    // Bedeutet: Nutzer hat einen eigenen Kaledner ausgweählt.
                    deleteAllEvents();
                }
            }
        }.start();
    }

    public ArrayList<String> getCalendars() {
        ArrayList<String> result = new ArrayList<>();
        calendars = calendarInterface.getCalendars();

        // Kalender für Kontakte entfernen
        calendars.remove("Contacts");

        // Noch ein paar Kalender entfernen
        Set<String> keysToRemove = new HashSet<>();

        // Ferien/Feiertage und Sonnenauf- und untergang
        // und persönlicher Kalender (alle mit @googlemail.com)
        for (String key : calendars.keySet()) {
            if (key.contains("Holidays")
                    || key.contains("Feiertage")
                    || key.contains("Sunrise/Sunset")
                    || key.contains("@googlemail.com")) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            calendars.remove(key);
        }

        // Noch ein paar
        calendars.remove("Hebrew Calendar");
        calendars.remove("Phases of the Moon");
        calendars.remove("Stardates");
        calendars.remove("Day of the year");
        calendars.remove("Week Numbers");
        calendars.remove("Geburtstage");

        result.addAll(calendars.keySet());
        return result;
    }

    public void setCalendar(String calendarName) {
        if (calendarName == null || calendarName.isEmpty()) {
            // lokalen Kalender
            calendarInterface.setCalendar(null);
        } else {
            // übergebenden Kalender
            calendarInterface.setCalendar(calendars.get(calendarName));
        }
    }

    private class CreateAllEventsTask extends AsyncTask<ArrayList<LectureItem>, Void, Boolean> {
        protected Boolean doInBackground(final ArrayList<LectureItem>... lectureItems) {
            for (LectureItem lectureItem :
                    lectureItems[0]) {
                createEventsForLecture(lectureItem);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Toast.makeText(context, "CreateAllEventsTask fertig. Result: " + result, Toast.LENGTH_SHORT).show();
            if (result) {
                calendarInterface.saveCalendarData();
            }
        }
    }

    private class DeleteAllEventsTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(final String... p_lectureItems) {
            if (p_lectureItems == null || p_lectureItems.length == 0) {
                calendarInterface.deleteAllEvents();
            } else {
                calendarInterface.deleteAllEvents(p_lectureItems[0]);
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //Toast.makeText(context, "DeleteAllEventsTask fertig. Result: " + result, Toast.LENGTH_SHORT).show();
            if (result) {
                calendarInterface.saveCalendarData();
            }
        }
    }
}
