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

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.hof.university.app.calendar.DateCorrection;
import de.hof.university.app.model.schedule.LectureItem;
import de.hof.university.app.util.Define;
import de.hof.university.app.util.HelpMethods;

/**
 * Created by larsg on 17.06.2016.
 */
public class ScheduleParser implements Parser<LectureItem> {

    private final static String TAG = "ScheduleParser";

    String language;

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
            Log.e(TAG, "JSONException", e);
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

    private int parseDayOfWeek(String day, Locale locale)
            throws ParseException {
        final SimpleDateFormat dayFormat = new SimpleDateFormat("E", locale);
        final Date date = dayFormat.parse(day);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

	final LectureItem convertJsonObject( JSONObject jsonObject ) {

		String weekday = jsonObject.optString( Define.PARSER_DAY );
		// Wenn Sprache der App auf Englisch gestellt ist englische Wochentage nehmen
		// Vom Webservice kommen nur deutsche Texte. Also suchen wir erst Mal den Wochentag
		// dann geben wir den fremdsprachlichen Text aus.
		if ( !language.equals( "de" ) ) {
			try {
				weekday = new DateFormatSymbols().getWeekdays()[parseDayOfWeek( weekday, Locale.GERMANY )];
			} catch ( ParseException e ) {
	            /* wir konnten den fremdsprachlichen Tag nicht finden, dann bleibt es beim deutschen Tag. */
			}
		}

		// Der splusname ist die neue ID
		final String id = jsonObject.optString( Define.PARSER_SPLUSNAME );
		final String label = jsonObject.optString( Define.SCHEDULE_PARSER_LABEL );
		String sp = jsonObject.optString( Define.PARSER_SP );
		if ( !sp.equals( "-" ) ) {
			sp = sp.substring( 3 );
		} else {
			sp = "";
		}
		final String type = jsonObject.optString( Define.PARSER_TYPE );
		final String style = jsonObject.optString( Define.PARSER_STYLE );
		final String group = jsonObject.optString( Define.SCHEDULE_PARSER_GROUP );
		final String beginTimeString = jsonObject.optString( Define.PARSER_STARTTIME );
		final String endTimeString = jsonObject.optString( Define.PARSER_ENDTIME );
		final String beginDateString = jsonObject.optString( Define.PARSER_STARTDATE );
		final String endDateString = jsonObject.optString( Define.PARSER_ENDDATE );
		final String room = jsonObject.optString( Define.PARSER_ROOM );
		//Entferne alle Sonderzeichen bei den Dozenten, eingetragen durch SPLUS
		final String lecturer = jsonObject.optString( Define.PARSER_DOCENT ).replace( "§§", "," );
		final String comment = jsonObject.optString( Define.SCHEDULE_PARSER_COMMENT );

		// Beispiele
		// beginTimeString: 14:00
		// beginDateString: 11.12.2017

		Date startDate = HelpMethods.getDateFromString( beginTimeString + " " + beginDateString );
		Date endDate = HelpMethods.getDateFromString( endTimeString + " " + endDateString );

		// Falls es kein Einzeltermin ist
		if ( !startDate.equals( endDate ) ) {
			// Date Correction
			startDate = DateCorrection.getInstance().getCorrectStartDate( startDate, endDate );
			endDate = DateCorrection.getInstance().getCorrectEndDate( startDate, endDate );
		}

		return new LectureItem( id, weekday, label, type, sp, group, startDate, endDate, room, lecturer, comment );
	}
}
