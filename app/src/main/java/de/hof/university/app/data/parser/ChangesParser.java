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

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.schedule.Changes;

/**
 * Created by larsg on 17.06.2016.
 */
public class ChangesParser implements Parser<Changes> {

    @Override
    public final ArrayList<Changes> parse(String[] params) {

        ArrayList<Changes> result = new ArrayList<>();
        if( 1 == params.length ) {
            String jsonString = params[0];
            //Escape, if String is empty
            if(jsonString.isEmpty()) {
                return result;
            }

            try {
                final JSONArray jsonArray = new JSONObject(jsonString).getJSONArray(Define.PARSER_CHANGES);
                for (int i = 0; i < jsonArray.length(); ++i) {

                    // Gibt es Änderungen überhaupt
                    final Changes change = convertJsonObject(jsonArray.getJSONObject(i));
                    if ( null != change ) {
                        // schauen ob diese Änderung bereits enthalten ist.
                        boolean contains = false;
                        for (Changes c:result) {
                            if (c.toString().equals(change.toString())) {
                                contains = true;
                            }
                        }
                        if (contains == false) {
                            result.add(change);
                        }
                    }
                }
            } catch (final JSONException ignored ) {

            }
        }
        return result;
    }

    protected static Changes convertJsonObject(JSONObject jsonObject) {
        final String label = jsonObject.optString(Define.SCHEDULE_PARSER_LABEL);
        final String comment = jsonObject.optString(Define.SCHEDULE_PARSER_COMMENT);
        final String group = jsonObject.optString(Define.SCHEDULE_PARSER_GROUP);
        final String reason = jsonObject.optString(Define.SCHEDULE_PARSER_REASON);
        final String begin_old = jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_DATE)+ ' ' +jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_TIME);
        String begin_new = "";
        String room_new = "";
        if(!jsonObject.isNull( Define.PARSER_ALTERNATIVE )){
            begin_new = jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_DATE) + ' ' + jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_TIME);
            room_new = jsonObject.optJSONObject(Define.PARSER_ALTERNATIVE).optString(Define.PARSER_ROOM);
        }
        final String room_old = jsonObject.optJSONObject(Define.PARSER_ORIGNAL).optString(Define.PARSER_ROOM);
        final String lecturer = jsonObject.optString(Define.PARSER_DOCENT);

        return new Changes(label, comment, group, reason, begin_old, begin_new, room_old, room_new, lecturer);
    }
}
