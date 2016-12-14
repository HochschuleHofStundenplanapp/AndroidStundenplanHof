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
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Lukas on 07.07.2016.
 */
public class MyScheduleParser extends ScheduleParser {


    @Override
    public final ArrayList<LectureItem> parse(String[] params) {
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
            jsonArray = jsonObject.optJSONArray("myschedule");
        } catch (final JSONException e) {
            if ( BuildConfig.DEBUG) e.printStackTrace();
            return result;
        }
        if (jsonArray == null)
        {
            return result;
        }
        for (int i = 0; i < jsonArray.length(); ++i) {
            final LectureItem lectureItem = convertJsonObject(jsonArray.optJSONObject(i));
            if ( lectureItem != null) {
                // schauen ob diese Vorlesung bereits enthalten ist
                boolean contains = false;
                for (LectureItem s : result) {
                    if (s.toString().equals(lectureItem.toString())) {
                        contains = true;
                    }
                }
                if ( !contains ) {
                    result.add(lectureItem);
                }
            }
        }

        return result;
    }

}
