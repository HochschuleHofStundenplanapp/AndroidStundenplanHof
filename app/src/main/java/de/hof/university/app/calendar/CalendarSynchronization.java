/*
 * Copyright (c) 2017 Hochschule Hof, Daniel Glaser
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
import de.hof.university.app.Util.Log;
import de.hof.university.app.data.DataManager;
import de.hof.university.app.model.schedule.LectureChange;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Daniel on 13.05.2017.
 */

public class CalendarSynchronization {
    private static final CalendarSynchronization instance = new CalendarSynchronization();
    private static final String TAG = "CalendarSynchronization";

    private final CalendarInterface calendarInterface;
    private HashMap<String, Long> calendars = new HashMap<>();

    public static CalendarSynchronization getInstance() {
        return instance;
    }

    private CalendarSynchronization() {
        calendarInterface = CalendarInterface.getInstance();
    }

    public void createAllEvents() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        final ArrayList<LectureItem> lectureItems = DataManager.getInstance().getSelectedLectures(context);

        if (lectureItems == null) return;

        CreateAllEventsTask task = new CreateAllEventsTask();
        final AsyncTask<ArrayList<LectureItem>, Void, Boolean> execute = task.execute(lectureItems);
    }

    public void createAllEvents(final ArrayList<LectureItem> lectureItems) {
        if (lectureItems == null)
            return;

        final CreateAllEventsTask task = new CreateAllEventsTask();
        task.execute(lectureItems);
    }

    public void createAllEvents(final LectureItem lecture) {
        final CreateAllEventsTask task = new CreateAllEventsTask();
        final ArrayList<LectureItem> list = new ArrayList<>();
        list.add(lecture);
        task.execute(list);
    }

    private void createEventsForLecture(LectureItem lectureItem) {
        Date tmpStartDate = lectureItem.getStartDate();

        Calendar endDateCalendar = GregorianCalendar.getInstance();
        endDateCalendar.setTime(lectureItem.getEndDate());

        do {
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
            Log.e(TAG, "InterruptedException", e);
            return false;
        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException", e);
            return false;
        }
    }

    public void deleteAllEvents(String lectureID) {
        DeleteAllEventsTask task = new DeleteAllEventsTask();
        task.execute(lectureID);
    }

    /**
     * updates the calendar with total new lectures
     */
    // TODO maybe synchronized?
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
     * updates the calendar with only the changed LectureItems
     * @param lectureItems
     */
    // TODO maybe synchronized?
    public void updateCalendar(final ArrayList<LectureItem> lectureItems) {
        // TODO Update Methode nutzen
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (LectureItem lecture: lectureItems) {
                    calendarInterface.updateLecture(lecture);
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
                // versuche den lokalen Kalender zu löschen
                if (!calendarInterface.removeLocalCalendar()) {
                    // falls es nicht geklappt hat dann lösche die Events in dem ausgewählten Kalender
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
        // und persönlicher Kalender (alle mit @googlemail.com oder @gmail.com)
        for (String key : calendars.keySet()) {
            if (key.contains("Holidays")
                    || key.contains("Feiertage")
                    || key.contains("Sunrise/Sunset")
                    || key.contains("@googlemail.com")
                    || key.contains("@gmail.com")) {
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
        if ((calendarName == null) || calendarName.isEmpty()) {
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
            if ((p_lectureItems == null) || (p_lectureItems.length == 0)) {
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
