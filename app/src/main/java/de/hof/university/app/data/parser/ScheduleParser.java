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

package de.hof.university.app.data.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.Util.Define;
import de.hof.university.app.model.schedule.Schedule;

/**
 * Created by larsg on 17.06.2016.
 */
public class ScheduleParser implements Parser<Schedule> {
    private String language;

    @Override
    public final ArrayList<Schedule> parse(String[] params) {
        if(params.length != 2){
            return null;
        }
        ArrayList<Schedule> result = new ArrayList<Schedule>();

        //Escape, if String is empty
        if(params[0].isEmpty()) {
            return result;
        }
        language = params[1];
        try {
            JSONArray jsonArray = new JSONObject(params[0]).getJSONArray("schedule");
            for (int i = 0; i < jsonArray.length(); ++i) {
                Schedule schedule = convertJsonObject(jsonArray.getJSONObject(i));
                if (schedule != null) {
                    result.add(schedule);
                }
            }
        } catch (final JSONException ignored ) {

        }
        return result;
    }

    protected final Schedule convertJsonObject(JSONObject jsonObject) {
        try {
            String weekday;
            try {
                weekday = jsonObject.getString("day");
                // Wenn Sprache auf Englisch gestellt ist englische Wochentage nehmen
                if (language.equals("en")) {
                    if (weekday.equals("Montag")) {
                        weekday = "Monday";
                    } else if (weekday.equals("Dienstag")) {
                        weekday = "Tuesday";
                    } else if (weekday.equals("Mittwoch")) {
                        weekday = "Wednesday";
                    } else if (weekday.equals("Donnerstag")) {
                        weekday = "Thursday";
                    } else if (weekday.equals("Freitag")) {
                        weekday = "Friday";
                    } else if (weekday.equals("Samstag")) {
                        weekday = "Saturday";
                    } else if (weekday.equals("Sonntag")) {
                        weekday = "Sunday";
                    }
                }
            } catch (JSONException e) {
                return null;
            }
            final int id = jsonObject.getInt("id");
            final String label = jsonObject.getString(Define.SCHEDULE_PARSER_LABEL);
            final String type = jsonObject.getString("type");
            final String group = jsonObject.getString(Define.SCHEDULE_PARSER_GROUP);
            final String begin = jsonObject.getString("starttime");
            final String end = jsonObject.getString("endtime");
            final String startdate = jsonObject.getString("startdate");
            final String enddate = jsonObject.getString("enddate");
            final String room = jsonObject.getString("room");
            final String lecturer = jsonObject.getString("docent").replace("§§",",");
            final String comment = jsonObject.getString("comment");

            return new Schedule(id, weekday, label, type, group, begin, end, startdate, enddate, room, lecturer, comment);
        } catch (final JSONException e) {
            if ( BuildConfig.DEBUG) e.printStackTrace();
            return null;
        }
    }
}
