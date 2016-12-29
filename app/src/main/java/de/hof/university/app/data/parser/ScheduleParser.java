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

import de.hof.university.app.BuildConfig;
import de.hof.university.app.Util.Define;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by larsg on 17.06.2016.
 */
public class ScheduleParser implements Parser<LectureItem> {

    public final static String TAG = "ScheduleParser";

    protected String language;

    @Override
    public ArrayList<LectureItem> parse(String[] params) {
        if (params.length != 2) {
            return null;
        }
        ArrayList<LectureItem> result = new ArrayList<>();

        // Zerlegen des JSON Strings in die Kurse
        final String jsonString = params[0];
        //Escape, if String is empty
        if (jsonString.isEmpty()) {
            return result;
        }
        language = params[1];

        JSONArray jsonArray = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonArray = jsonObject.optJSONArray(Define.PARSER_SCHEDULE);
        } catch (final JSONException e) {
            if ( BuildConfig.DEBUG) e.printStackTrace();
            return result;
        }

        for (int i = 0; i < jsonArray.length(); ++i) {
            LectureItem lectureItem = convertJsonObject(jsonArray.optJSONObject(i));
            if ( lectureItem != null) {
                result.add(lectureItem);
            }
        }

        return result;
    }

    protected int parseDayOfWeek(String day, Locale locale)
            throws ParseException {
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E", locale);
        final Date date = dayFormat.parse(day);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    protected final LectureItem convertJsonObject(JSONObject jsonObject) {

        String weekday = jsonObject.optString(Define.PARSER_DAY);
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
        final String type = jsonObject.optString(Define.PARSER_TYPE);
        final String group = jsonObject.optString(Define.SCHEDULE_PARSER_GROUP);
        final String begin = jsonObject.optString(Define.PARSER_STARTTIME);
        final String end = jsonObject.optString(Define.PARSER_ENDTIME);
        final String startdate = jsonObject.optString(Define.PARSER_STARTDATE);
        final String enddate = jsonObject.optString(Define.PARSER_ENDDATE);
        final String room = jsonObject.optString(Define.PARSER_ROOM);
            //Entferne alle Sonderzeichen bei den Dozenten, eingetragen durch SPLUS
        final String lecturer = jsonObject.optString(Define.PARSER_DOCENT).replace("§§", ",");
        final String comment = jsonObject.optString(Define.SCHEDULE_PARSER_COMMENT);

        return new LectureItem(id, weekday, label, type, group, begin, end, startdate, enddate, room, lecturer, comment);
    }
}
