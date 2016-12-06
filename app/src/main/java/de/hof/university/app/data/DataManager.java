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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import de.hof.university.app.Util.Log;
import de.hof.university.app.data.parser.Parser;
import de.hof.university.app.data.parser.ParserFactory;
import de.hof.university.app.data.parser.ParserFactory.EParser;
import de.hof.university.app.model.SaveObject;
import de.hof.university.app.model.meal.Meal;
import de.hof.university.app.model.meal.Meals;
import de.hof.university.app.model.schedule.Changes;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.model.schedule.MySchedule;
import de.hof.university.app.model.schedule.Schedule;
import de.hof.university.app.model.settings.Courses;
import de.hof.university.app.model.settings.StudyCourse;

/**
 * Created by Lukas on 14.06.2016.
 */
public class DataManager {

    public static final String TAG = "DataManager";

    private static final int MAX_CACHE_TIME = 60 * 24 * 2;

    private enum CONNECTION {

        // Essen
        MEAL("https://www.studentenwerk-oberfranken.de/?eID=bwrkSpeiseplanRss&tx_bwrkspeiseplan_pi2%5Bbar%5D=340&tx_bwrkspeiseplan_pi2%5Bdate%5D=", 60 * 24),

        COURSE("https://www.hof-university.de/soap/client.php?f=Courses&tt=%s", MAX_CACHE_TIME),

        //TODO change for tests and release
        // Testserver: http://sh-web02.hof-university.de
        //SCHEDULE("http://sh-web02.hof-university.de/soap/client.php?f=Schedule&stg=%s&sem=%s&tt=%s",60*24),
        //CHANGES("http://sh-web02.hof-university.de/soap/client.php?f=Changes",60*3),
        //MYSCHEDULE("http://sh-web02.hof-university.de/soap/client.php?f=MySchedule",60*24),

        //Produktivserver
        SCHEDULE("https://www.hof-university.de/soap/client.php?f=Schedule&stg=%s&sem=%s&tt=%s", 60 * 24),
        CHANGES("https://www.hof-university.de/soap/client.php?f=Changes", 60 * 3),
        MYSCHEDULE("https://www.hof-university.de/soap/client.php?f=MySchedule", 60 * 24),;

        private final String url;
        private final int cache;

        CONNECTION(final String url, final int cache) {
            this.url = url;
            this.cache = cache;
        }

        public final int getCache() {
            return this.cache;
        }

        public final String getUrl() {
            return this.url;
        }
    }


    // single instance of the Factories
    static final private DataConnector dataConnector = new DataConnector();

    private MySchedule mySchedule;

    private static final String myScheduleFilename = "mySchedule";
    private static final String scheduleFilename = "schedule";
    private static final String changesFilename = "changes";
    private static final String coursesFilename = "courses";
    private static final String mealsFilename = "meals";

    private static final DataManager dataManager = new DataManager();

    public static DataManager getInstance() {
        return DataManager.dataManager;
    }

    private DataManager() {
    }

    public final ArrayList<Meal> getMeals(Context context, boolean forceRefresh) {
        Object object = this.readObject(context, mealsFilename);
        Meals meals = new Meals();

        if (object != null) {
            meals = (Meals) object;
        }

        if (forceRefresh || object == null || meals.getMeals().size() == 0 || meals.getLastSaved() == null || !cacheStillValid(meals, CONNECTION.MEAL.getCache())) {
            final Parser parser = ParserFactory.create(EParser.MENU);
            final Calendar calendar = Calendar.getInstance();
            final String url = DataManager.CONNECTION.MEAL.getUrl() + calendar.get(Calendar.YEAR) + '-' + (calendar.get(Calendar.MONTH) + 1) + '-' + calendar.get(Calendar.DAY_OF_MONTH);
            final String xmlString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.MEAL.getCache());

            if ( xmlString.equals("") ) {
                if (!forceRefresh && object != null) {
                    return meals.getMeals();
                } else {
                    return null;
                }
            }

            final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String[] params = { xmlString, sharedPreferences.getString("speiseplan_tarif", "1") };
            assert parser != null;

            ArrayList<Meal> tmpMeals = (ArrayList<Meal>) parser.parse(params);

            if (tmpMeals.isEmpty()) {
                if (!forceRefresh && meals.getMeals().size() > 0) {
                    return meals.getMeals();
                } else {
                    return null;
                }
            }

            meals.setMeals(tmpMeals);

            meals.setLastSaved(new Date());
            saveObject(context, meals, mealsFilename);
        }

        return meals.getMeals();
    }

    public final ArrayList<LectureItem> getSchedule(Context context, String language, String course, String semester,
                                               String termTime, boolean forceRefresh) {
        Object object = readObject(context, scheduleFilename);
        Schedule schedule = new Schedule();

        if (object != null) {
            schedule = (Schedule) object;
        }

        if (forceRefresh || object == null || schedule.getLectures().size() == 0 || schedule.getLastSaved() == null || !cacheStillValid(schedule, CONNECTION.SCHEDULE.getCache()) || !schedule.getCourse().equals(course) || !schedule.getSemester().equals(semester) || !schedule.getTermtime().equals(termTime)) {
            // Änderungen sollen neu geholt werden
            Object changesObject = this.readObject(context, changesFilename);
            if ( changesObject instanceof Changes ) {
                Changes changes = (Changes) changesObject;
                if ( changes != null ) {
                    changes.setLastSaved(null);
                    saveObject(context, changes, changesFilename);
                }
            }

            final Parser parser = ParserFactory.create(EParser.SCHEDULE);
            final String jsonString = this.getData(context, forceRefresh, String.format(DataManager.CONNECTION.SCHEDULE.getUrl(), DataManager.replaceWhitespace(course), DataManager.replaceWhitespace(semester), DataManager.replaceWhitespace(termTime)), DataManager.CONNECTION.SCHEDULE.getCache());

            if ( jsonString.equals("") ) {
                if (!forceRefresh && object != null) {
                    return schedule.getLectures();
                } else {
                    return null;
                }
            }

            final String[] params = { jsonString, language };
            assert parser != null;

            ArrayList<LectureItem> lectures = (ArrayList<LectureItem>) parser.parse(params);

            if (lectures.isEmpty()) {
                if (!forceRefresh && schedule.getLectures().size() > 0) {
                    return schedule.getLectures();
                } else {
                    return null;
                }
            }

            schedule.setLectures(lectures);

            schedule.setCourse(course);
            schedule.setSemester(semester);
            schedule.setTermtime(termTime);

            schedule.setLastSaved(new Date());
            saveObject(context, schedule, scheduleFilename);
        }
        
        return schedule.getLectures();
    }

    public final ArrayList<LectureItem> getMySchedule(Context context, String language, String course, String semester,
                                                 String termTime, boolean forceRefresh) {
        MySchedule mySchedule = this.getMySchedule(context);

        if (forceRefresh || mySchedule.getLectures().size() == 0 || mySchedule.getLastSaved() == null || !cacheStillValid(mySchedule, CONNECTION.MYSCHEDULE.getCache()) || mySchedule.getIds().size() != mySchedule.getLectures().size()) {
            // Änderungen sollen neu geholt werden
            Object changesObject = this.readObject(context, changesFilename);
            if ( changesObject instanceof Changes ) {
                Changes changes = (Changes) changesObject;
                if ( changes != null ) {
                    changes.setLastSaved(null);
                    saveObject(context, changes, changesFilename);
                }
            }

            final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();
            String url = DataManager.CONNECTION.MYSCHEDULE.getUrl();
            while ( iterator.hasNext() ) {
                url += "&id[]=" + iterator.next();
            }

            final Parser parser = ParserFactory.create(EParser.MYSCHEDULE);

            final String jsonString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.MYSCHEDULE.getCache());

            if ( jsonString.equals("") ) {
                if (!forceRefresh && mySchedule.getLectures().size() > 0) {
                    return mySchedule.getLectures();
                } else {
                    return null;
                }
            }

            final String[] params = { jsonString, language };

            ArrayList<LectureItem> tmpMySchedule = (ArrayList<LectureItem>) parser.parse(params);

            if (tmpMySchedule.isEmpty()) {
                if (!forceRefresh && mySchedule.getLectures().size() > 0) {
                    return mySchedule.getLectures();
                } else {
                    return null;
                }
            }

            getMySchedule(context).setLectures(tmpMySchedule);

            getMySchedule(context).setLastSaved(new Date());
            this.saveObject(context, getMySchedule(context), myScheduleFilename);
        }

        return this.getMySchedule(context).getLectures();
    }

    public final ArrayList<Object> getChanges(Context context, String course, String semester,
                                                     String termTime, boolean forceRefresh) {
        Object object = this.readObject(context, changesFilename);
        Changes changes = new Changes();

        if (object != null) {
            changes = (Changes) object;
        }

        if (forceRefresh || object == null || changes.getChanges().size() == 0 || changes.getLastSaved() == null || !cacheStillValid(changes, CONNECTION.CHANGES.getCache())) {
            final Iterator<String> iterator = this.getMySchedule(context).getIds().iterator();

            String url = DataManager.CONNECTION.CHANGES.getUrl();

            if ( !iterator.hasNext() ) {
                url += "&stg=" + DataManager.replaceWhitespace(course);
                url += "&sem=" + DataManager.replaceWhitespace(semester);
                url += "&tt=" + DataManager.replaceWhitespace(termTime);
            } else {
                // Fügt die ID's der Vorlesungen hinzu die in Mein Stundenplan sind
                // dadurch werden nur Änderungen von Mein Stundenplan geholt
                while ( iterator.hasNext() ) {
                    url += "&id[]=" + iterator.next();
                }
            }

            Parser parser = ParserFactory.create(EParser.CHANGES);
            String jsonString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.CHANGES.getCache());

            if ( jsonString.equals("") ) {
                if (!forceRefresh && object != null) {
                    return changes.getChanges();
                } else {
                    return null;
                }
            }

            final String[] params = { jsonString };
            assert parser != null;

            ArrayList<Object> tmpChanges = (ArrayList<Object>) parser.parse(params);

            // führt hier zum falschen Verhalten
//            if (tmpChanges.isEmpty()) {
//                if (!forceRefresh && changes.getChanges().size() > 0) {
//                    return changes.getChanges();
//                } else {
//                    return new ArrayList<>(); //oder null aber dann sagt er "Aktualisierung fehlgeschlagen"
//                }
//            }

            changes.setChanges(tmpChanges);

            changes.setLastSaved(new Date());
            saveObject(context, changes, changesFilename);
        }

        return changes.getChanges();
    }

    public final ArrayList<StudyCourse> getCourses(Context context, String language, String termTime, boolean forceRefresh) {
        Object object = this.readObject(context, coursesFilename);
        Courses courses = new Courses();

        if (object != null) {
            courses = (Courses) object;
        }

        // Änderungen sollen neu geholt werden
        Object changesObject = this.readObject(context, changesFilename);
        if ( changesObject instanceof Changes ) {
            Changes changes = (Changes) changesObject;
            if ( changes != null ) {
                changes.setLastSaved(null);
                saveObject(context, changes, changesFilename);
            }
        }

        if (forceRefresh || object == null || courses.getCourses().size() == 0 || courses.getLastSaved() == null || !cacheStillValid(courses, CONNECTION.COURSE.getCache())) {
            final Parser parser = ParserFactory.create(EParser.COURSES);

            final String jsonString = this.getData(context, forceRefresh, String.format(DataManager.CONNECTION.COURSE.getUrl(), DataManager.replaceWhitespace(termTime)), DataManager.CONNECTION.COURSE.getCache());

            if ( jsonString.equals("") ) {
                if (!forceRefresh && object != null) {
                    return courses.getCourses();
                } else {
                    // TODO schauen ob das nicht zu einem Problem wird
                    return null;
                }
            }

            final String[] params = { jsonString, language };
            assert parser != null;

            ArrayList<StudyCourse> tmpCourses = (ArrayList<StudyCourse>) parser.parse(params);

            if (tmpCourses.isEmpty()) {
                if (!forceRefresh && courses.getCourses().size() > 0) {
                    return courses.getCourses();
                } else {
                    return null;
                }
            }

            courses.setCourses(tmpCourses);

            courses.setLastSaved(new Date());
            saveObject(context, courses, coursesFilename);
        }

        return courses.getCourses();
    }


    private String getData(Context context, boolean forceRefresh, String url, int cache) {
        if (forceRefresh) {
            return this.dataConnector.getStringFromUrl(context, url, -1);
        } else {
            return this.dataConnector.getStringFromUrl(context, url, cache);
        }
    }

    private static String replaceWhitespace(final String str) {
        return str.replace(" ", "%20");
    }

    public final void addToMySchedule(final Context context, final LectureItem s) {
        this.getMySchedule(context).getIds().add(String.valueOf(s.getId()));
        this.saveObject(context, this.getMySchedule(context), myScheduleFilename);
    }

    public final boolean myScheduleContains(final Context context, final LectureItem s) {
        return this.getMySchedule(context).getIds().contains(String.valueOf(s.getId()));
    }

    public final void deleteFromMySchedule(final Context context, final LectureItem s) {
        this.getMySchedule(context).getIds().remove(String.valueOf(s.getId()));
        // TODO Objekt mit der ID finden und löschen
        //this.getMySchedule(context).getLectures().remove();
        this.saveObject(context, this.getMySchedule(context), myScheduleFilename);
    }

    public final void addAllToMySchedule(final Context context, final Set<String> schedulesIds) {
        this.getMySchedule(context).getIds().addAll(schedulesIds);
        this.saveObject(context, this.getMySchedule(context), myScheduleFilename);
    }

    public final void deleteAllFromMySchedule(final Context context) {
        this.getMySchedule(context).getIds().clear();
        this.getMySchedule(context).getLectures().clear();
        this.saveObject(context, this.getMySchedule(context), myScheduleFilename);
    }

    private MySchedule getMySchedule(final Context context) {
        if (this.mySchedule == null) {
            Object object = DataManager.readObject(context, myScheduleFilename);
            if (object != null && object instanceof Set) {
                this.mySchedule = new MySchedule();
                this.mySchedule.setIds((Set<String>) object);
            } else if (object != null && object instanceof MySchedule) {
                this.mySchedule = (MySchedule) object;
            } else {
                this.mySchedule = new MySchedule();
            }
        }
        return this.mySchedule;
    }

    public final int getMyScheduleSize(final Context context) {
        return this.getMySchedule(context).getIds().size();
    }

    private void saveObject(final Context context, Object object, String filename) {
        try {
            final File file = new File(context.getFilesDir(), filename);
            final FileOutputStream fos = new FileOutputStream(file);
            final ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (IOException e) {
            // TODO Fehlermeldung
            e.printStackTrace();
        }
    }

    private static Object readObject(final Context context, String filename) {
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
            // TODO Fehlermeldung
            e.printStackTrace();
        }
        return result;
    }

    private boolean cacheStillValid(Object object, final int cacheTime) {
        final Date today = new Date();
        Date lastCached = new Date();

        if ( object instanceof SaveObject ) {
            SaveObject saveObject = (SaveObject) object;

            if ( saveObject.getLastSaved() != null ) {
                lastCached = saveObject.getLastSaved();

                Calendar cal = Calendar.getInstance();
                cal.setTime(lastCached);
                cal.add(Calendar.MINUTE, cacheTime);
                lastCached = cal.getTime();
            }

            return lastCached.after(today);
        } else {
            return false;
        }
    }

    public final void cleanCache(final Context context) {
        this.dataConnector.cleanCache(context, DataManager.MAX_CACHE_TIME);
    }
}
