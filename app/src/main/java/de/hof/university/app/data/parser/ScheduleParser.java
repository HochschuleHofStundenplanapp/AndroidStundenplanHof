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

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.schedule.Schedule;

/**
 * Created by larsg on 17.06.2016.
 */
public class ScheduleParser implements Parser<Schedule> {
    protected String language;

    @Override
    public ArrayList<Schedule> parse(String[] params) {
        if (params.length != 2) {
            return null;
        }
        ArrayList<Schedule> result = new ArrayList<Schedule>();

        //Escape, if String is empty
        if (params[0].isEmpty()) {
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
        } catch (final JSONException ignored) {

        }
        return result;
    }

    protected int parseDayOfWeek(String day, Locale locale)
            throws ParseException {
        SimpleDateFormat dayFormat = new SimpleDateFormat("E", locale);
        Date date = dayFormat.parse(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }

    protected final Schedule convertJsonObject(JSONObject jsonObject) {

        String weekday = jsonObject.optString("day");
        // Wenn Sprache der App auf Englisch gestellt ist englische Wochentage nehmen
        // Vom Webservice kommen nur deutsche Texte. Also suchen wir erst Mal den Wochentag
        // dann geben wir den fremdsprachlichen Text aus.
        if (!language.equals("de")) {
            try {
                weekday = new DateFormatSymbols().getWeekdays()[parseDayOfWeek(weekday, Locale.GERMANY)];
            } catch (ParseException e) {
                /* wir konnten den fremdsprachlichen Tag nicht finden, dann bleibt es beim deutschen Tag. */
            }
        }

        final int id = jsonObject.optInt("id", 0);
        final String label = jsonObject.optString(Define.SCHEDULE_PARSER_LABEL);
        final String type = jsonObject.optString("type");
        final String group = jsonObject.optString(Define.SCHEDULE_PARSER_GROUP);
        final String begin = jsonObject.optString("starttime");
        final String end = jsonObject.optString("endtime");
        final String startdate = jsonObject.optString("startdate");
        final String enddate = jsonObject.optString("enddate");
        final String room = jsonObject.optString("room");
        final String lecturer = jsonObject.optString("docent").replace("§§", ",");
        final String comment = jsonObject.optString("comment");

        return new Schedule(id, weekday, label, type, group, begin, end, startdate, enddate, room, lecturer, comment);
    }
}
