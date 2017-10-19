/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

package de.hof.university.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import de.hof.university.app.Communication.RegisterLectures;
import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.Util.MyString;
import de.hof.university.app.calendar.CalendarSynchronization;
import de.hof.university.app.data.parser.Parser;
import de.hof.university.app.data.parser.ParserFactory;
import de.hof.university.app.data.parser.ParserFactory.EParser;
import de.hof.university.app.model.HofObject;
import de.hof.university.app.model.meal.Meal;
import de.hof.university.app.model.meal.Meals;
import de.hof.university.app.model.schedule.Changes;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.model.schedule.MySchedule;
import de.hof.university.app.model.schedule.Schedule;
import de.hof.university.app.model.settings.StudyCourse;
import de.hof.university.app.model.settings.StudyCourses;

/**
 *
 */
public class DataManager {
    public static final String TAG = "DataManager";

    private static final DataManager instance = new DataManager();

    // single instance of the Factories
    private static final DataConnector dataConnector = new DataConnector();

    private Schedule schedule;
    private MySchedule mySchedule;
    private Changes changes;
    private Meals meals;
    private StudyCourses studyCourses;

    private final SharedPreferences sharedPreferences;

    public static DataManager getInstance() {
        return instance;
    }

    private DataManager() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public final ArrayList<Meal> getMeals(Context context, boolean forceRefresh) {
        Meals meals = this.getMeals(context);

        if (forceRefresh
                || (meals.getMeals().isEmpty())
                || (meals.getLastSaved() == null)
                || !cacheStillValid(meals, Define.MEAL_CACHE_TIME)) {
            final Parser parser = ParserFactory.create(EParser.MENU);
            final Calendar calendar = Calendar.getInstance();
            final String url = Define.URL_MEAL + calendar.get(Calendar.YEAR) + '-' + (calendar.get(Calendar.MONTH) + 1) + '-' + calendar.get(Calendar.DAY_OF_MONTH);
            final String xmlString = dataConnector.readStringFromUrl(url);


            // falls der String leer ist war ein Problem mit dem Internet
            if (xmlString.isEmpty()) {
                // prüfen ob es kein ForceRefreseh war, dann kann gecachtes zurück gegeben werden
                if (!forceRefresh && !meals.getMeals().isEmpty()) {
                    return meals.getMeals();
                } else {
                    // anderen falls null, damit dann die Fehlermeldung "Aktualisierung fehlgeschlagen" kommt
                    return null;
                }
            }

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String[] params = {xmlString, sharedPreferences.getString("speiseplan_tarif", "1")};
            assert parser != null;

            ArrayList<Meal> tmpMeals = (ArrayList<Meal>) parser.parse(params);

            this.getMeals(context).setMeals(tmpMeals);

            this.getMeals(context).setLastSaved(new Date());
            saveObject(context, this.getMeals(context), Define.mealsFilename);
        }

        return this.getMeals(context).getMeals();
    }

    public final ArrayList<LectureItem> getSchedule(Context context, String language, String course, String semester,
                                                    String termTime, boolean forceRefresh) {
        Schedule schedule = this.getSchedule(context);

        if (forceRefresh
                || (schedule.getLectures().isEmpty())
                || (schedule.getLastSaved() == null)
                || !cacheStillValid(schedule, Define.SCHEDULE_CACHE_TIME)
                || !schedule.getCourse().equals(course)
                || !schedule.getSemester().equals(semester)
                || !schedule.getTermtime().equals(termTime)) {

            final Parser parser = ParserFactory.create(EParser.SCHEDULE);
            final String aString = String.format(Define.URL_SCHEDULE, MyString.URLReplaceWhitespace(course), MyString.URLReplaceWhitespace(semester), MyString.URLReplaceWhitespace(termTime));
            final String jsonString = dataConnector.readStringFromUrl(aString);

            // falls der String leer ist war ein Problem mit dem Internet
            if (jsonString.isEmpty()) {
                // prüfen ob es kein ForceRefreseh war, dann kann gecachtes zurück gegeben werden
                if (!forceRefresh && !schedule.getLectures().isEmpty()) {
                    return schedule.getLectures();
                } else {
                    // anderen falls null, damit dann die Fehlermeldung "Aktualisierung fehlgeschlagen" kommt
                    return null;
                }
            }

            final String[] params = {jsonString, language};
            assert parser != null;

            ArrayList<LectureItem> tmpScheduleLectureItems = (ArrayList<LectureItem>) parser.parse(params);

            // Wenn der Server einen unvollständigen Stundenplan (nur halb so groß oder kleiner) liefert bringe die Fehlermedlung "Aktualisierung fehlgeschlagen"
            if (course.equals(schedule.getCourse()) && semester.equals(schedule.getSemester()) && termTime.equals(schedule.getTermtime()) && (tmpScheduleLectureItems.size() < (schedule.getLectures().size() / 2))) {
                return null;
            }

            // Falls die neuen LectureItems nicht gleich der bereits vorhandenen sind
            // Bedeutet: Etwas hat sich geändert
            if (!isScheduleEqualsToNewSchedule(this.getSchedule(context).getLectures(), tmpScheduleLectureItems)) {

                ArrayList<LectureItem> newLectureItems = getNewLectureItems(this.getSchedule(context).getLectures(), tmpScheduleLectureItems);

                // Neue Vorlesungen setzen
                this.getSchedule(context).setLectures(tmpScheduleLectureItems);

                this.getSchedule(context).setCourse(course);
                this.getSchedule(context).setSemester(semester);
                this.getSchedule(context).setTermtime(termTime);

                // Wenn kein "Mein Stundenplan" vorhanden ist
                if (getMyScheduleSize(context) == 0) {
                    // Kalender aktualisieren
                    this.updateCalendar(newLectureItems);
                }
            }
            // Zuletzt aktualisert setzen
            this.getSchedule(context).setLastSaved(new Date());

            // Speichern
            saveObject(context, this.getSchedule(context), Define.scheduleFilename);
        }

        return this.getSchedule(context).getLectures();
    }

    public final ArrayList<LectureItem> getMySchedule(Context context, String language,
                                                      boolean forceRefresh) {
        MySchedule mySchedule = this.getMySchedule(context);

        if (forceRefresh
                || (mySchedule.getLectures().isEmpty())
                || (mySchedule.getLastSaved() == null)
                || !cacheStillValid(mySchedule, Define.MYSCHEDULE_CACHE_TIME)
                || (mySchedule.getIds().size() != mySchedule.getLectures().size())
                ) {

            final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();
            String url = Define.URL_MYSCHEDULE;
            while (iterator.hasNext()) {
                try {
                    url += "&id[]=" + URLEncoder.encode(iterator.next(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Encoding not supported", e);
                }
            }

            final Parser parser = ParserFactory.create(EParser.MYSCHEDULE);

            final String jsonString = dataConnector.readStringFromUrl(url);

            // falls der String leer ist war ein Problem mit dem Internet
            if (jsonString.isEmpty()) {
                // prüfen ob es kein ForceRefreseh war, dann kann gecachtes zurück gegeben werden
                if (!forceRefresh && !mySchedule.getLectures().isEmpty()) {
                    return mySchedule.getLectures();
                } else {
                    // anderen falls null, damit dann die Fehlermeldung "Aktualisierung fehlgeschlagen" kommt
                    return null;
                }
            }

            final String[] params = {jsonString, language};

            junit.framework.Assert.assertTrue(parser != null );
            ArrayList<LectureItem> tmpMyScheduleLectureItems = (ArrayList<LectureItem>) parser.parse(params);

            // Wenn der Server einen unvollständigen Stundenplan (nur halb so groß oder kleiner) liefert bringe die Fehlermedlung "Aktualisierung fehlgeschlagen"
            if (tmpMyScheduleLectureItems.size() < (getMyScheduleSize(context) / 2)) {
                return null;
            }

            // Falls die neuen LectureItems nicht gleich der bereits vorhandenen sind
            // Bedeutet: Etwas hat sich geändert
            if (!isScheduleEqualsToNewSchedule(this.getMySchedule(context).getLectures(), tmpMyScheduleLectureItems)) {

                ArrayList<LectureItem> newLectureItems = getNewLectureItems(this.getMySchedule(context).getLectures(), tmpMyScheduleLectureItems);

                // Neue Vorlesungen setzen
                this.getMySchedule(context).setLectures(tmpMyScheduleLectureItems);

                // Kalender aktualisieren
                this.updateCalendar(newLectureItems);
            }
            // Zuletzt aktualisiert setzen
            this.getMySchedule(context).setLastSaved(new Date());

            // Speichern
            this.saveObject(context, getMySchedule(context), Define.myScheduleFilename);
        }

        return this.getMySchedule(context).getLectures();
    }

    public final ArrayList<Object> getChanges(Context context, String course, String semester,
                                              String termTime, boolean forceRefresh) {
        Changes changes = this.getChanges(context);

        if (forceRefresh
                || (changes.getChanges().isEmpty())
                || (changes.getLastSaved() == null)
                || !cacheStillValid(changes, Define.CHANGES_CACHE_TIME)
                ) {
            final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();

            String url = Define.URL_CHANGES;

            if (!iterator.hasNext()) {
                url += "&stg=" + MyString.URLReplaceWhitespace(course);
                url += "&sem=" + MyString.URLReplaceWhitespace(semester);
                url += "&tt=" + MyString.URLReplaceWhitespace(termTime);
            } else {
                // Fügt die ID's der Vorlesungen hinzu die in Mein Stundenplan sind
                // dadurch werden nur Änderungen von Mein Stundenplan geholt
                while (iterator.hasNext()) {
                    try {
                        url += "&id[]=" + URLEncoder.encode(iterator.next(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Encoding not supported", e);
                    }
                }
            }

            final Parser parser = ParserFactory.create(EParser.CHANGES);
            final String jsonString = dataConnector.readStringFromUrl(url);

            // falls der String leer ist war ein Problem mit dem Internet
            if (jsonString.isEmpty()) {
                // prüfen ob es kein ForceRefreseh war, dann kann gecachtes zurück gegeben werden
                if (!forceRefresh && !changes.getChanges().isEmpty()) {
                    return changes.getChanges();
                } else {
                    // anderen falls null, damit dann die Fehlermeldung "Aktualisierung fehlgeschlagen" kommt
                    return null;
                }
            }

            final String[] params = {jsonString};
            assert parser != null;

            ArrayList<Object> tmpChanges = (ArrayList<Object>) parser.parse(params);

            // Neue Änderungen setzen
            this.getChanges(context).setChanges(tmpChanges);

            // Zuletzt aktualisiert setzen
            this.getChanges(context).setLastSaved(new Date());
            // Speichern
            saveObject(context, this.getChanges(context), Define.changesFilename);
            // Kalender aktualisieren
            this.updateChangesInCalendar();
        }

        return this.getChanges(context).getChanges();
    }

    // es muss immer ein StudyCourse zurückgegeben werden.
    // wenn es aber
    public final ArrayList<StudyCourse> getCourses(final Context context, final String language,
                                                   final String termTime, boolean forceRefresh) {
        StudyCourses studyCourses = this.getStudyCourses(context);

        if (forceRefresh
                || (studyCourses.getCourses().isEmpty())
                || (studyCourses.getLastSaved() == null)
                || !cacheStillValid(studyCourses, Define.COURSES_CACHE_TIME)
                ) {
            // Änderungen sollen neu geholt werden
            resetChangesLastSave(context);

            final Parser parser = ParserFactory.create(EParser.COURSES);

            final String sTermType = MyString.URLReplaceWhitespace(termTime);
            final String sURL = String.format(Define.URL_STUDYCOURSE, sTermType);
            final String jsonString = dataConnector.readStringFromUrl(sURL);


            // falls der String leer ist war ein Problem mit dem Internet
            if (jsonString.isEmpty()) {
                // prüfen ob es kein ForceRefreseh war, dann kann gecachtes zurück gegeben werden
                if (!forceRefresh) {
                    return studyCourses.getCourses();
                } else {
                    // anderen falls null, damit dann die Fehlermeldung "Aktualisierung fehlgeschlagen" kommt
                    return null;
                }
            }

            final String[] params = {jsonString, language};
            assert parser != null;

            ArrayList<StudyCourse> tmpCourses = (ArrayList<StudyCourse>) parser.parse(params);

            this.getStudyCourses(context).setCourses(tmpCourses);

            this.getStudyCourses(context).setLastSaved(new Date());
            saveObject(context, this.getStudyCourses(context), Define.coursesFilename);
        }

        return this.getStudyCourses(context).getCourses();
    }

    // Änderungen sollen neu geholt werden
    private void resetChangesLastSave(Context context) {
        if (getChangesLastSaved() != null) {
            // LastSaved zurücksetzten damit Änderungen neu geholt werden
            changes.setLastSaved(null);
            saveObject(context, changes, Define.changesFilename);
        }
    }

    public final boolean myScheduleContains(final Context context, final LectureItem s) {
        return this.getMySchedule(context).getIds().contains(String.valueOf(s.getId()));
    }

    public final void addToMySchedule(final Context context, final LectureItem lectureItem) {
        this.getMySchedule(context).getIds().add(String.valueOf(lectureItem.getId()));
        this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
        this.addLectureToCalendar(context, lectureItem);
    }

    public final void deleteFromMySchedule(final Context context, final LectureItem lectureItem) {
        this.getMySchedule(context).getIds().remove(String.valueOf(lectureItem.getId()));
        LectureItem lectureToRemove = null;
        for (LectureItem li : this.getMySchedule(context).getLectures()) {
            if (li.getId().equals(lectureItem.getId())) {
                lectureToRemove = li;
            }
        }
        if (lectureToRemove != null) {
            this.getMySchedule(context).getLectures().remove(lectureToRemove);
        }
        this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
        this.deleteLectureFromCalendar(context, lectureItem.getId());

        // falls MySchedule leer füge den Schedule zum Kalender hinzu
        if (this.getMySchedule(context).getLectures().size() == 0) {
            this.updateCalendar();
        }
    }

    public final void addAllToMySchedule(final Context context, final Set<String> schedulesIds) {
        this.getMySchedule(context).getIds().addAll(schedulesIds);
        this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
        this.addAllToCalendar(context, this.getSchedule(context).getLectures());
    }

    public final void deleteAllFromMySchedule(final Context context) {
        this.getMySchedule(context).getIds().clear();
        this.getMySchedule(context).getLectures().clear();
        this.saveObject(context, this.getMySchedule(context), Define.myScheduleFilename);
        this.updateCalendar();
    }

    // Getters
    // ---------------------------------------------------------------------------------------------

    private Schedule getSchedule(final Context context) {
        if (this.schedule == null) {
            Object optScheduleObj = readObject(context, Define.scheduleFilename);
            if ((optScheduleObj != null) && (optScheduleObj instanceof Schedule)) {
                this.schedule = (Schedule) optScheduleObj;
            } else {
                this.schedule = new Schedule();
            }
        }
        return this.schedule;
    }

    public final int getScheduleSize(final Context context) {
        return this.getSchedule(context).getLectures().size();
    }

    public Date getScheduleLastSaved() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        return getSchedule(context).getLastSaved();
    }

    private MySchedule getMySchedule(final Context context) {
        if (this.mySchedule == null) {
            Object obtMyScheduleOpj = readObject(context, Define.myScheduleFilename);
            if ((obtMyScheduleOpj != null) && (obtMyScheduleOpj instanceof Set)) {
                this.mySchedule = new MySchedule();
                this.mySchedule.setIds((Set<String>) obtMyScheduleOpj);
            } else if ((obtMyScheduleOpj != null) && (obtMyScheduleOpj instanceof MySchedule)) {
                this.mySchedule = (MySchedule) obtMyScheduleOpj;
            } else {
                this.mySchedule = new MySchedule();
            }
        }
        return this.mySchedule;
    }

    public final int getMyScheduleSize(final Context context) {
        return this.getMySchedule(context).getIds().size();
    }

    public Date getMyScheduleLastSaved() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        return getMySchedule(context).getLastSaved();
    }

    public Changes getChanges(final Context context) {
        if (this.changes == null) {
            Object obtChangesObj = readObject(context, Define.changesFilename);
            if ((obtChangesObj != null) && (obtChangesObj instanceof Changes)) {
                this.changes = (Changes) obtChangesObj;
            } else {
                this.changes = new Changes();
            }
        }
        return this.changes;
    }

    public Date getChangesLastSaved() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        return getChanges(context).getLastSaved();
    }

    private Meals getMeals(final Context context) {
        if (this.meals == null) {
            Object obtMealsObj = readObject(context, Define.mealsFilename);
            if ((obtMealsObj != null) && (obtMealsObj instanceof Meals)) {
                this.meals = (Meals) obtMealsObj;
            } else {
                this.meals = new Meals();
            }
        }
        return this.meals;
    }

    public Date getMealsLastSaved() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        return getMeals(context).getLastSaved();
    }

    private StudyCourses getStudyCourses(final Context context) {
        if (this.studyCourses == null) {
            Object obtStudyCoursesObj = readObject(context, Define.coursesFilename);
            if ((obtStudyCoursesObj != null) && (obtStudyCoursesObj instanceof StudyCourses)) {
                this.studyCourses = (StudyCourses) obtStudyCoursesObj;
            } else {
                this.studyCourses = new StudyCourses();
            }
        }
        return this.studyCourses;
    }

    /**
     * formatiert ein Datum
     * @return dd.MM.yyyy HH:mm formatiertes Date
     */
    public String formatDate(Date date) {
        Locale locale = getLocale();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", locale);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        if (date != null) {
            return dateFormat.format(date) + " " + simpleDateFormat.format(date);
        } else {
            return "";
        }
    }

    public Locale getLocale() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        if (context.getString(R.string.language).equals("de")) {
            return Locale.GERMANY;
        } else if (context.getString(R.string.language).equals("en")) {
            return Locale.ENGLISH;
        } else {
            return Locale.GERMANY;
        }
    }

    // Saving and loading
    // ---------------------------------------------------------------------------------------------

    // this is the general method to serialize an object
    //
    public synchronized void saveObject(final Context context, Object object, final String filename) {
        try {
            final File file = new File(context.getFilesDir(), filename);
            final FileOutputStream fos = new FileOutputStream(file);
            final ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (Exception e) { //TODO Eigentlich nur IOException, aber kann im Moment auch ConcurrentModificationException kommen
            Log.e(TAG, "Fehler beim Speichern des Objektes", e);
        }

        if ((object instanceof Schedule) || (object instanceof MySchedule)) {
            // Änderungen neu holen
            resetChangesLastSave(context);
            // Stundenplan registrieren
            registerFCMServer(context);
        }
    }

    // this is the general method to serialize an object
    public synchronized Object readObject(final Context context, String filename) {
        Object result = null;
        try {
            final File file = new File(context.getFilesDir(), filename);
            if (file.exists()) {
                final FileInputStream fis = new FileInputStream(file);
                final ObjectInputStream is = new ObjectInputStream(fis);
                result = is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim lesen des Objektes", e);
        }
        return result;
    }

    // Caching
    // ---------------------------------------------------------------------------------------------

    /**
     * checks if the cache is still valid
     * @param hofObject the object in witch the time of last savling is saved
     * @param cacheTime time to cahce, in minutes
     * @return returns if the cache is still valid
     */
    private boolean cacheStillValid(HofObject hofObject, final int cacheTime) {
        final Date today = new Date();
        Date lastCached = new Date();

        if (hofObject.getLastSaved() != null) {
            lastCached = hofObject.getLastSaved();

            Calendar cal = Calendar.getInstance();
            cal.setTime(lastCached);
            cal.add(Calendar.MINUTE, cacheTime);
            lastCached = cal.getTime();
        }

        return lastCached.after(today);
    }

    // FCM
    // ---------------------------------------------------------------------------------------------

    public void registerFCMServer(Context context) {
        if (Define.PUSH_NOTIFICATIONS_ENABLED) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean registerForChangesNotifications = sharedPreferences.getBoolean("changes_notifications", false);

            if (registerForChangesNotifications) {
                registerFCMServerForce(context);
            }
        }
    }

    public void registerFCMServerForce(Context context) {
        Set<String> ids = getSelectedLecturesIDs(context);
        if (ids == null) return;

        new RegisterLectures().registerLectures(ids);
    }

    // Calendar
    // ---------------------------------------------------------------------------------------------

    private void addLectureToCalendar(final Context context, final LectureItem lectureItem) {
        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            new Thread() {
                @Override
                public void run() {
                    if (getMySchedule(context).getIds().size() == 1) {
                        // Falls es die erste hinzugefügte Vorlesung ist, alle alten raus löschen
                        CalendarSynchronization.getInstance().deleteAllEvents();
                    }
                    CalendarSynchronization.getInstance().createAllEvents(lectureItem);
                }
            }.start();
        }
    }

    private void addAllToCalendar(Context context, ArrayList<LectureItem> lecturesItems) {
        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            // falls es nicht die ersten Vorlesungen sind die hinzugefügt werden, denn dann stehen sie schon drin.
            if (!getMySchedule(context).getLectures().isEmpty()) {
                CalendarSynchronization.getInstance().createAllEvents(lecturesItems);
            }
        }
    }

    private void deleteLectureFromCalendar(Context context, String lectureID) {
        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            CalendarSynchronization.getInstance().deleteAllEvents(lectureID);
        }
    }

    private void updateCalendar() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            CalendarSynchronization.getInstance().updateCalendar();
        }
    }

    private void updateCalendar(ArrayList<LectureItem> lectureItems) {
        Context context = MainActivity.getAppContext().getApplicationContext();

        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            CalendarSynchronization.getInstance().updateCalendar(lectureItems);
        }
    }

    private void updateChangesInCalendar() {
        Context context = MainActivity.getAppContext().getApplicationContext();

        final boolean calendarSynchronization = sharedPreferences.getBoolean(context.getString(R.string.PREFERENCE_KEY_CALENDAR_SYNCHRONIZATION), false);

        if (calendarSynchronization) {
            CalendarSynchronization.getInstance().updateChanges();
        }
    }

    // getSelectedIDs
    // ---------------------------------------------------------------------------------------------

    private Set<String> getSelectedLecturesIDs(Context context) {
        Set<String> ids = new HashSet<>();

        ArrayList<LectureItem> lectureItems = getSelectedLectures(context);
        if (lectureItems == null) return null;

        for (LectureItem li : lectureItems) {
            ids.add(String.valueOf(li.getId()));
        }
        return ids;
    }

    public ArrayList<LectureItem> getSelectedLectures(Context context) {
        final Schedule schedule = this.getSchedule(context);

        if (!getMySchedule(context).getIds().isEmpty()) {
            return getMySchedule(context).getLectures();
        } else if (!schedule.getLectures().isEmpty()) {
            return schedule.getLectures();
        }
        return null;
    }


    private boolean isScheduleEqualsToNewSchedule(ArrayList<LectureItem> oldSchedule, ArrayList<LectureItem> newSchedule)
    {
        //null checking
        if((oldSchedule == null) && (newSchedule == null)) {
            return true;
        }
        if((oldSchedule == null) || (newSchedule == null)) {
            return false;
        }

        if(oldSchedule.size() != newSchedule.size()) {
            return false;
        }

        for (int i = 0; i < oldSchedule.size(); i++) {
            if (!oldSchedule.get(i).equals(newSchedule.get(i))) {
                return false;
            }
        }

        return true;
    }

    private ArrayList<LectureItem> getNewLectureItems(ArrayList<LectureItem> oldSchedule, ArrayList<LectureItem> newSchedule) {
        ArrayList<LectureItem> newLectureItems = new ArrayList<>();

        for (LectureItem newLecture: newSchedule) {
            boolean contains = false;

            for (LectureItem oldLecture: oldSchedule) {
                if (newLecture.equals(oldLecture)) {
                    contains = true;
                    break;
                }
            }

            // if not containing in old schedule
            if (contains == false) {
                newLectureItems.add(newLecture);
            }
        }

        return newLectureItems;
    }
}
