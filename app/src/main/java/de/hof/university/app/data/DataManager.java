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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hof.university.app.data.parser.Parser;
import de.hof.university.app.data.parser.ParserFactory;
import de.hof.university.app.data.parser.ParserFactory.EParser;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.model.settings.StudyCourse;

/**
 * Created by Lukas on 14.06.2016.
 */
public class DataManager {

    public final String TAG = "DataManager";

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
        SCHEDULE("https://www.hof-university.de/soap/client.php?f=LectureItem&stg=%s&sem=%s&tt=%s", 60 * 24),
        CHANGES("https://www.hof-university.de/soap/client.php?f=LectureChange", 60 * 3),
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

    private Set<String> mySchedule;
    private static final String myScheduleFilename = "mySchedule";
    private static final DataManager dataManager = new DataManager();

    public static DataManager getInstance() {
        return DataManager.dataManager;
    }

    private DataManager() {
    }

    public final ArrayList<Object> getMeals(Context context, boolean forceRefresh) {
        final Parser parser = ParserFactory.create(EParser.MENU);
        final Calendar calendar = Calendar.getInstance();
        final String url = DataManager.CONNECTION.MEAL.getUrl() + calendar.get(Calendar.YEAR) + '-' + (calendar.get(Calendar.MONTH) + 1) + '-' + calendar.get(Calendar.DAY_OF_MONTH);
        final String xmlString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.MEAL.getCache());

        if (xmlString.equals("")) {
            return null;
        }

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String[] params = {xmlString, sharedPreferences.getString("speiseplan_tarif", "1")};
        assert parser != null;

        return (ArrayList<Object>) parser.parse(params);
    }

    public final ArrayList<Object> getSchedule(Context context, String language, String course, String semester,
                                               String termTime, boolean forceRefresh) {
        final Parser parser = ParserFactory.create(EParser.SCHEDULE);
        final String jsonString = this.getData(context, forceRefresh, String.format(DataManager.CONNECTION.SCHEDULE.getUrl(), DataManager.replaceWhitespace(course), DataManager.replaceWhitespace(semester), DataManager.replaceWhitespace(termTime)), DataManager.CONNECTION.SCHEDULE.getCache());

        if (jsonString.equals("")) {
            return null;
        }

        final String[] params = {jsonString, language};
        assert parser != null;

        return (ArrayList<Object>) parser.parse(params);
    }


    public final ArrayList<Object> getMySchedule(Context context, String language, String course, String semester,
                                                 String termTime, boolean forceRefresh) {
        // myScheudle leeren damit es noch mal frisch aus der Datei gelesen wird.
        // Weil es dort in einer anderen Reihenfolge steht.
        this.mySchedule = null;
        final Iterator<String> iterator = this.getMySchedule(context).iterator();
        String url = DataManager.CONNECTION.MYSCHEDULE.getUrl();
        while (iterator.hasNext()) {
            url += "&id[]=" + iterator.next();
        }

        final Parser parser = ParserFactory.create(EParser.MYSCHEDULE);

        final String jsonString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.MYSCHEDULE.getCache());

        if (jsonString.equals("")) {
            return null;
        }

        final String[] params = {jsonString, language};

        return (ArrayList<Object>) parser.parse(params);
    }

    public final ArrayList<Object> getChanges(Context context, String course, String semester,
                                              String termTime, boolean forceRefresh) {
        // myScheudle leeren damit es noch mal frisch aus der Datei gelesen wird.
        // Weil es dort in einer anderen Reihenfolge steht.
        this.mySchedule = null;
        final Iterator<String> iterator = this.getMySchedule(context).iterator();

        String url = DataManager.CONNECTION.CHANGES.getUrl();

        if (!iterator.hasNext()) {
            url += "&stg=" + DataManager.replaceWhitespace(course);
            url += "&sem=" + DataManager.replaceWhitespace(semester);
            url += "&tt=" + DataManager.replaceWhitespace(termTime);
        } else {
            // Fügt die ID's der Vorlesungen hinzu die in Mein Stundenplan sind
            // dadurch werden nur Änderungen von Mein Stundenplan geholt
            while (iterator.hasNext()) {
                url += "&id[]=" + iterator.next();
            }
        }

        Parser parser = ParserFactory.create(EParser.CHANGES);
        String jsonString = this.getData(context, forceRefresh, url, DataManager.CONNECTION.CHANGES.getCache());

        if (jsonString.equals("")) {
            return null;
        }

        final String[] params = {jsonString};
        assert parser != null;

        return (ArrayList<Object>) parser.parse(params);
    }

    public final ArrayList<StudyCourse> getCourses(Context context, String language, String termTime, boolean forceRefresh) {
        final Parser parser = ParserFactory.create(EParser.COURSES);

        final String jsonString = this.getData(context, forceRefresh, String.format(DataManager.CONNECTION.COURSE.getUrl(), DataManager.replaceWhitespace(termTime)), DataManager.CONNECTION.COURSE.getCache());
        final String[] params = {jsonString, language};
        assert parser != null;

        return (ArrayList<StudyCourse>) parser.parse(params);
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
        this.getMySchedule(context).add(String.valueOf(s.getId()));
        this.saveMySchedule(context);
    }

    public final boolean myScheduleContains(final Context context, final LectureItem s) {
        return this.getMySchedule(context).contains(String.valueOf(s.getId()));
    }

    public final void deleteFromMySchedule(final Context context, final LectureItem s) {
        this.getMySchedule(context).remove(String.valueOf(s.getId()));
        this.saveMySchedule(context);
    }

    public final void addAllToMySchedule(final Context context, final Set<String> schedulesIds) {
        this.getMySchedule(context).addAll(schedulesIds);
        this.saveMySchedule(context);
    }

    public final void deleteAllFromMySchedule(final Context context) {
        this.getMySchedule(context).clear();
        this.saveMySchedule(context);
    }

    private void saveMySchedule(final Context context) {
        // alte Variante
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //sharedPreferences.edit().putStringSet("myScheduleIds", this.getMySchedule(context)).apply();

        try {
            final File file = new File(context.getFilesDir(), myScheduleFilename);
            final FileOutputStream fos = new FileOutputStream(file);
            final ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this.getMySchedule(context));
            os.close();
            fos.close();
        } catch (IOException e) {
            // TODO Fehlermeldung
            e.printStackTrace();
        }
    }

    private static Set<String> readMySchedule(final Context context) {
        // alte Variante
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //final Set<String> result = sharedPreferences.getStringSet("myScheduleIds", new HashSet<String>());

        Set<String> result = new HashSet<>();
        try {
            final File file = new File(context.getFilesDir(), myScheduleFilename);
            if (file.exists()) {
                final FileInputStream fis = new FileInputStream(file);
                final ObjectInputStream is = new ObjectInputStream(fis);
                result = (Set<String>) is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            // TODO Fehlermeldung
            e.printStackTrace();
        }
        return result;
    }

    private Set<String> getMySchedule(final Context context) {
        if (this.mySchedule == null) {
            this.mySchedule = DataManager.readMySchedule(context);
        }
        return this.mySchedule;
    }

    public final int getMyScheduleSize(final Context context) {
        return this.getMySchedule(context).size();
    }


}
