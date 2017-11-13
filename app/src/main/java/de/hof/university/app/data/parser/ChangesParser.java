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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;
import de.hof.university.app.model.schedule.LectureChange;

/**
 * Created by larsg on 17.06.2016.
 */
public final class ChangesParser implements Parser<LectureChange> {

    private final static String TAG = "ChangesParser";

    @Override
    public final ArrayList<LectureChange> parse(final String[] params) {

        ArrayList<LectureChange> result = new ArrayList<>();

        if (1 == params.length) {
            final String jsonString = params[0];
            //Escape, if String is empty
            if (jsonString.isEmpty()) {
                return result;
            }

            JSONArray jsonArray = null;
            try {
                final JSONObject jsonObject = new JSONObject(jsonString);
                jsonArray = jsonObject.optJSONArray(Define.PARSER_CHANGES);
            } catch (final JSONException e) {
                Log.e(TAG, "JSONException", e);
                return result;
            }

            for (int i = 0; i < jsonArray.length(); ++i) {
                // Gibt es Änderungen überhaupt
                final LectureChange change = convertJsonObject(jsonArray.optJSONObject(i));
                if (null != change) {
                    // schauen ob diese Änderung bereits enthalten ist.
                    boolean contains = false;
                    for (LectureChange c : result) {
                        if (c.toString().equals(change.toString())) {
                            contains = true;
                        }
                    }
                    if ( !contains ) {
                        result.add(change);
                    }
                }
            }
        }
        return result;
    }

    // Wozu brauchen wir diese Methode
    private static LectureChange convertJsonObject(JSONObject jsonObject) {

        // Die Antwort vom Server enthält die folgenden Objekte und werden weparat in Teil.Strings zerleigt
        // optSting: wirft keine Exception, wenn das JSON Element NICHT vorhanden ist.
        // Der splusname ist die neue ID
        final String id = jsonObject.optString(Define.PARSER_SPLUSNAME);
        final String label = jsonObject.optString(Define.SCHEDULE_PARSER_LABEL);
        final String comment = jsonObject.optString(Define.SCHEDULE_PARSER_COMMENT);
        final String group = jsonObject.optString(Define.SCHEDULE_PARSER_GROUP);
        final String reason = jsonObject.optString(Define.SCHEDULE_PARSER_REASON);
        final String text = jsonObject.optString(Define.SCHEDULE_PARSER_TEXT);
        final String orginalDateString = jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_DATE);
        final String orginalTimeString = jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_TIME);

        String alternativeDateString = "";
        String alternativeTimeString = "";
        String room_new = "";
        if(!jsonObject.isNull( Define.PARSER_ALTERNATIVE )){
            alternativeDateString = jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_DATE);
            alternativeTimeString = jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_TIME);
            room_new = jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_ROOM);
        }
        final String room_old = jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_ROOM);
        final String lecturer = jsonObject.optString(Define.PARSER_DOCENT);
		//TODO Beispielzeile ergänzen
        int orginalHours      = Integer.parseInt(orginalTimeString.substring(0, 2));
        int orginalMinutes    = Integer.parseInt(orginalTimeString.substring(3, 5));
        int orginalDay        = Integer.parseInt(orginalDateString.substring(0, 2));
        int orginalMonth      = Integer.parseInt(orginalDateString.substring(3, 5));
        int orginalYear       = Integer.parseInt(orginalDateString.substring(6, 10));

        Calendar calendar = GregorianCalendar.getInstance();
        //Startzeit
        calendar.set(orginalYear, orginalMonth - 1, orginalDay, orginalHours, orginalMinutes, 0);

        Date orginalDate = calendar.getTime();

        Date alternativeDate = null;
		//TODO Beispielzeile ergänzen
        if (!alternativeTimeString.equals("") && !alternativeDateString.equals("")) {
            int alternativeHours        = Integer.parseInt(alternativeTimeString.substring(0, 2));
            int alternativeMinutes      = Integer.parseInt(alternativeTimeString.substring(3, 5));
            int alternativeDay          = Integer.parseInt(alternativeDateString.substring(0, 2));
            int alternativeMonth        = Integer.parseInt(alternativeDateString.substring(3, 5));
            int alternativeYear         = Integer.parseInt(alternativeDateString.substring(6, 10));

            calendar.set(alternativeYear, alternativeMonth - 1, alternativeDay, alternativeHours, alternativeMinutes, 0);
            alternativeDate = calendar.getTime();
        }

        return new LectureChange(id,label,comment,text,group,reason,orginalDate,alternativeDate,room_old,room_new,lecturer);
    }
}
