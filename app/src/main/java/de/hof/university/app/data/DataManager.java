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
import de.hof.university.app.model.settings.Course;
import de.hof.university.app.model.schedule.Schedule;

/**
 * Created by Lukas on 14.06.2016.
 */
public class DataManager {

    enum CONNECTION {
        // Testserver: http://sh-web02.hof-university.de
        COURSE("https://www.hof-university.de/soap/client.php?f=Courses&tt=%s", 60*24),
        SCHEDULE("https://www.hof-university.de/soap/client.php?f=Schedule&stg=%s&sem=%s&tt=%s",60*24),
        CHANGES("https://www.hof-university.de/soap/client.php?f=Changes",60*3),
        MYSCHEDULE("https://www.hof-university.de/soap/client.php?f=MySchedule",60*24),
        MENU("https://www.studentenwerk-oberfranken.de/?eID=bwrkSpeiseplanRss&tx_bwrkspeiseplan_pi2%5Bbar%5D=340&tx_bwrkspeiseplan_pi2%5Bdate%5D=",60*24);

        private final String url;
        private final int cache;
        CONNECTION(String url, int cache){
            this.url = url;
            this.cache=cache;
        }

        public int getCache(){
            return this.cache;
        }

        public String getUrl(){
            return this.url;
        }
    }

    private static final int MAX_CACHE_TIME = 60*24*2;


    private final ParserFactory parserFactory = new ParserFactory();
    private final DataConnector dataConnector = new DataConnector() ;
    private Set<String> mySchedule;
    private static final String myScheduleFilename = "mySchedule";

    private static final DataManager dataManager = new DataManager();

    public static final DataManager getInstance(){
        return DataManager.dataManager;
    }

    public DataManager() {
    }

    public final ArrayList<Object> getMeals(Context context, boolean forceRefresh){
        Parser parser = ParserFactory.create(EParser.MENU);
        Calendar calendar = Calendar.getInstance();
        String url = DataManager.CONNECTION.MENU.getUrl()+ calendar.get(Calendar.YEAR) + '-' + (calendar.get(Calendar.MONTH) + 1) + '-' + calendar.get(Calendar.DAY_OF_MONTH);
        String xmlString = this.getData(context,forceRefresh,url,DataManager.CONNECTION.MENU.getCache());

        if (xmlString == ""){
            return null;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] params ={xmlString, sharedPreferences.getString("speiseplan_tarif", "1")};
        assert parser != null;

        ArrayList<Object> meals = parser.parse(params);

        return meals;
    }

    public final ArrayList<Object> getSchedule(Context context, String language, String course, String semester,
                                               String termTime, boolean forceRefresh){
        final Parser parser = ParserFactory.create(EParser.SCHEDULE);
        final String jsonString = this.getData(context, forceRefresh, String.format(DataManager.CONNECTION.SCHEDULE.getUrl(), DataManager.replaceWhitespace(course), DataManager.replaceWhitespace(semester), DataManager.replaceWhitespace(termTime)) ,DataManager.CONNECTION.SCHEDULE.getCache());

        if (jsonString == ""){
            return null;
        }

        String[] params ={jsonString, language};
        assert parser != null;

        ArrayList<Object> schedule = parser.parse(params) ;

        return schedule;
    }


    public final ArrayList<Object> getMySchedule(Context context, String language, String course, String semester,
                                                 String termTime, boolean forceRefresh){
        Iterator<String> iterator = this.getMySchedule(context).iterator();
        String url = DataManager.CONNECTION.MYSCHEDULE.getUrl();
        while(iterator.hasNext()) {
            url+="&id[]="+iterator.next();
        }

        Parser parser = ParserFactory.create(EParser.MYSCHEDULE);
        // TODO Anscheinend ohne comment
        String jsonString = this.getData(context,forceRefresh,url,DataManager.CONNECTION.SCHEDULE.getCache());

        if (jsonString == "") {
            return null;
        }

        String[] params ={jsonString, language};

        ArrayList<Object> mySchedule = parser.parse(params);

        return mySchedule;
    }

    public final ArrayList<Object> getChanges(Context context, String course, String semester,
                                              String termTime, boolean forceRefresh){
        // nur Änderungen von Mein Stundenplan holen
        Iterator<String> iterator = this.getMySchedule(context).iterator();
    //  TODO Wenn Server angepasst ist wieder einkommentieren
        String url = "";
    //    if (!iterator.hasNext()) {
            url = DataManager.CONNECTION.CHANGES.getUrl();
            url+="&stg="+DataManager.replaceWhitespace(course);
            url+="&sem="+DataManager.replaceWhitespace(semester);
            url+="&tt="+DataManager.replaceWhitespace(termTime);
    //    } else {
    //        url = DataManager.CONNECTION.CHANGES.getUrl();

            // Fügt die ID's der Vorlesungen hinzu die in Mein Stundenplan sind
            // dadurch werden nur Änderungen davon geholt
            while(iterator.hasNext()){
                url+="&id[]="+iterator.next();
            }
    //    }

        Parser parser = ParserFactory.create(EParser.CHANGES);
        String jsonString = this.getData(context,forceRefresh,url,DataManager.CONNECTION.CHANGES.getCache());

        if (jsonString == ""){
            return null;
        }

        String[] params ={jsonString};
        assert parser != null;

        ArrayList<Object> myScheduleChanges = parser.parse(params);

        return myScheduleChanges;
    }

    public final ArrayList<Course> getCourses(Context context, String language, String termTime, boolean forceRefresh){
        Parser parser = ParserFactory.create(EParser.COURSES);

        String jsonString = this.getData(context,forceRefresh,String.format(DataManager.CONNECTION.COURSE.getUrl(), DataManager.replaceWhitespace(termTime)), DataManager.CONNECTION.COURSE.getCache());
        String[] params ={jsonString, language};
        assert parser != null;

        final ArrayList<Course> courses = parser.parse(params) ;

        return courses;
    }


    private String getData(Context context, boolean forceRefresh, String url, int cache) {
        if(forceRefresh) {
            return this.dataConnector.getStringFromUrl(context, url, -1);
        } else {
            return this.dataConnector.getStringFromUrl(context, url, cache);
        }

    }

    private static String replaceWhitespace(String str) {
        return str.replace(" ", "%20");
    }

    public final void addToMySchedule(Context context, Schedule s){
        this.getMySchedule(context).add(String.valueOf(s.getId()));
        this.saveMySchedule(context);
    }

    public final boolean myScheduleContains(Context context, Schedule s){
        return this.getMySchedule(context).contains(String.valueOf(s.getId()));
    }

    public final void deleteFromMySchedule(Context context, Schedule s){
        this.getMySchedule(context).remove(String.valueOf(s.getId()));
        this.saveMySchedule(context);
    }

    public final void addAllToMySchedule(Context context, ArrayList<String> schedulesIds) {
        this.getMySchedule(context).addAll(schedulesIds);
        this.saveMySchedule(context);
    }

    public final void deleteAllFromMySchedule(Context context) {
        this.getMySchedule(context).clear();
        this.saveMySchedule(context);
    }

    private void saveMySchedule(Context context){
        // alte Variante
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //sharedPreferences.edit().putStringSet("myScheduleIds", this.getMySchedule(context)).apply();

        try {
            File file = new File(context.getFilesDir(), myScheduleFilename);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this.getMySchedule(context));
            os.close();
            fos.close();
        } catch (IOException e) {
            // TODO Fehlermeldung
            e.printStackTrace();
        }
    }

    private static Set<String> readMySchedule(Context context){
        // alte Variante
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //final Set<String> result = sharedPreferences.getStringSet("myScheduleIds", new HashSet<String>());

        Set<String> result = new HashSet<String>();
        try {
            File file = new File(context.getFilesDir(), myScheduleFilename);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream is = new ObjectInputStream(fis);
                result = (Set<String>) is.readObject();
                is.close();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Set<String> getMySchedule(Context context) {
        if( this.mySchedule == null){
            this.mySchedule = DataManager.readMySchedule(context);
        }
        return this.mySchedule;
    }

    public final int getMyScheduleSize(Context context){
        return this.getMySchedule(context).size();
    }


    public final void cleanCache(Context context){
        this.dataConnector.cleanCache(context, DataManager.MAX_CACHE_TIME);
    }

}
